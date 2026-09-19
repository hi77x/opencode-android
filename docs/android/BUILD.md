# Building the Android APK

## One command

```sh
./script/android/build-apk.sh
```

Output: `packages/android/app/build/outputs/apk/debug/app-debug.apk`

Install:

```sh
adb install -r packages/android/app/build/outputs/apk/debug/app-debug.apk
```

## Requirements

| Tool | Version | Used for |
| --- | --- | --- |
| Bun | >= 1.4.2 (1.3.14 crashes on Android, see `BUN_ANDROID_AUDIT.md`) | building the server and workspace tooling |
| Python | 3.8+ | fetching/extracting the Termux runtime, i18n sync, ZIP extraction |
| curl | any | downloading pinned artifacts |
| JDK | 17 or 21 | Gradle |
| Android SDK | platform 35, build-tools | Gradle (`local.properties` or `ANDROID_HOME`) |
| Android NDK | r26+ (only when `librg.so` is missing) | building ripgrep |
| Rust | stable + `aarch64-linux-android` target (only when `librg.so` is missing) | building ripgrep |

The script caches downloads in `${OPENCODE_ANDROID_CACHE:-$HOME/.cache/opencode-android}`.
`script/android/termux-manifest.json` records the pinned Termux package versions
and SHA-256 hashes used for the git runtime.

## What the script does

1. `packages/opencode/script/build.ts --target=linux-arm64-android` builds the
   server, embedding the `packages/app` production bundle, and copies it to
   `jniLibs/arm64-v8a/libopencode.so`.
2. Downloads `bun-linux-aarch64-android.zip` (pinned `1.4.2`) and installs it as
   `libbun.so` (exposed as `bun` and `node` on the device).
3. Builds ripgrep for `aarch64-linux-android` (PIE) as `librg.so` when missing.
4. `fetch-termux-runtime.py` resolves the Termux git dependency closure,
   downloads the pinned `.deb` packages and writes `assets/git-runtime.zip`.
5. `extract-usage-i18n.py` syncs the app's `context.*` translations for the
   host's usage panel into `assets/usage-i18n.json`.
6. `./gradlew :app:assembleDebug` builds the APK.

## Manual pieces

Individual steps can be run separately:

```sh
python script/android/fetch-termux-runtime.py
python script/android/extract-usage-i18n.py
bash script/android/build-ripgrep.sh
cd packages/opencode && bun run script/build.ts --target=linux-arm64-android
cd packages/android && ./gradlew :app:assembleDebug
```

## Runtime layout on device

```
files/home/                    HOME, XDG_* directories, server.log
files/home/workspace/          default workspace (the UI's project picker shows it)
files/home/.gitconfig          default committer identity
files/usr/                     extracted Termux git runtime (bin, lib, libexec, etc)
files/bin/                     git wrapper + symlinks to nativeLibraryDir tools
```

## Troubleshooting

- **The UI shows an old server / ports keep changing:** a previous server child
  survived a force-stop. The host kills same-uid stale servers on launch; if an
  external process holds port 4096 the app falls back to an ephemeral port.
- **`git` reports a permission warning about the Termux prefix:** make sure
  `GIT_CONFIG_NOSYSTEM=1` is set; the host sets it for the whole server.
- **Search returns nothing:** `rg` must exist at `files/bin/rg` (symlink to
  `librg.so`); run `./script/android/build-ripgrep.sh` before building the APK.
- **Terminal panel does not open:** PTY support is unavailable (see
  `NATIVE_DEPENDENCIES.md`).
