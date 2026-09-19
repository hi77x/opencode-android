# opencode for Android

Android host for the existing opencode application. The APK ships the
unmodified opencode server (built with `bun build --compile
--target=bun-linux-arm64-android`) together with the production bundle of
`packages/app`, starts it on loopback and hosts the existing web UI in a
WebView. No frontend code is duplicated or changed.

```
Android Activity
  -> EmbeddedServer (libopencode.so serve --hostname=127.0.0.1)
  -> WebView loads http://127.0.0.1:4096/ (packages/app bundle served by the server)
```

## Layout

```
app/src/main/java/ai/opencode/android/
  MainActivity.kt      WebView host + mobile stylesheet + usage panel injection
  EmbeddedServer.kt    starts the server, prepares HOME/XDG, tools and git
app/src/main/assets/
  usage-panel.js       host affordance for context usage (localized via the app)
  usage-i18n.json      generated: app's `context.*` translations (62 locales)
  git-runtime.zip      generated: Termux aarch64 git + dependencies
app/src/main/jniLibs/arm64-v8a/
  libopencode.so       generated: opencode server (Bun Android target)
  libbun.so            generated: bare Bun Android runtime exposed as bun/node
  librg.so             generated: ripgrep for aarch64-linux-android
  libgit.so            generated: Termux git ELF (executed from nativeLibraryDir)
  libgit-remote-http.so generated: git https remote helper
```

Generated binaries are not committed (see `.gitignore`). Build everything with:

```sh
./script/android/build-apk.sh
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk` and can be
installed with `adb install -r`.

## Runtime decisions

- **Executable code ships in the APK.** `extractNativeLibs`/legacy packaging
  extracts jniLibs into `nativeLibraryDir`, the only app-writable location
  where Android still allows `execve` on targetSdk 29+. Nothing executable is
  downloaded at runtime.
- **git** comes from pinned Termux aarch64 packages, extracted into
  `files/usr` and executed from `nativeLibraryDir` symlinks with
  `LD_LIBRARY_PATH` including `files/usr/lib`. `GIT_CONFIG_NOSYSTEM=1`
  because the Termux build references its own prefix.
- **ripgrep** replaces the fff native search library (no Android build);
  `OPENCODE_DISABLE_FFF=true`.
- **bun/node** are the bare Bun Android runtime, so the agent can execute
  JS/TS files inside the workspace.
- **HOME/XDG** live under `files/home`; the default workspace is
  `files/home/workspace`.
- **Session history** is grouped by directory. The host canonicalizes HOME and
  workspace paths (`/data/data/...`) and migrates older UI state that stored
  the `/data/user/0/...` alias, so the home list matches the server records.
- **UI adaptation** happens only through the host: a narrow-screen stylesheet
  injected by the WebView (dialogs full-screen, settings tabs stacked,
  titlebar session tabs keep a fixed width and scroll instead of squeezing,
  home keeps Settings/Help pinned with a scrollable session canvas, prompt
  editor is taller and the toolbar truncates the model name, main tab row
  scrolls) plus `usage-panel.js` host affordances:
  a localized context-usage panel (matching the repository's context tab,
  including the context breakdown) opened from the header usage circle or the
  "Usage" tab, and a read-only "Files" tab for browsing the workspace. File
  attachments from the phone work through a WebView `onShowFileChooser`
  implementation that opens the system document picker. The app bundle itself
  is untouched (`git diff -- packages/app` stays empty).

## Security posture

- Server binds `127.0.0.1` only; cleartext traffic is limited to loopback.
- No Termux, no root, no external server; provider traffic originates from the
  device.
- `docs/android/EXECUTION_POLICY.md` documents the W^X constraints.
