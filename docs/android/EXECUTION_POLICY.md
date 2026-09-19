# Android execution policy

## Target SDK / min SDK

| Setting | Value |
| --- | --- |
| `minSdk` | 26 (Android 8.0) |
| `targetSdk` | 35 (Android 15) |
| ABI | `arm64-v8a` |
| `android:extractNativeLibs` | `true` |
| `packaging.jniLibs.useLegacyPackaging` | `true` |
| `android:usesCleartextTraffic` | `true` (loopback HTTP only) |

The app does **not** lower `targetSdk` to bypass modern security policy.

## W^X / SELinux restrictions that drive the design

Since Android 10 (targetSdk 29+), SELinux denies `execve()` on files labelled
`app_data_file`, i.e. anything an app writes into its own data directory
(`files/`, `cache/`, ...). The policy applies to the *final inode* that
`execve` resolves:

- writing an executable into `filesDir` and running it: **denied**;
- putting the same file into `jniLibs` (installed into `nativeLibraryDir`,
  labelled `apk_data_file`): **allowed**;
- a symlink in `filesDir` pointing to a binary in `nativeLibraryDir`: the
  resolved inode is `apk_data_file`, so exec is **allowed** (verified on the
  test device).

Normal project data (source files created/edited by the agent) is not
executable code and is unaffected by this policy.

## Where packaged executable code lives

| File (jniLibs/arm64-v8a) | What it is | How it runs |
| --- | --- | --- |
| `libopencode.so` | the opencode server, built with `bun build --compile --target=bun-linux-arm64-android` at build time | spawned by the Android host via `ProcessBuilder` from `nativeLibraryDir` |
| `librg.so` | ripgrep built for `aarch64-linux-android` (PIE) | exposed as `rg` via an app-data symlink on `PATH` |
| `libgit.so` + dependency `lib*.so` | see `COMMAND_INVENTORY.md` / `NATIVE_DEPENDENCIES.md` | exposed as `git` via symlink; shared libraries resolved with `LD_LIBRARY_PATH=nativeLibraryDir` |

The APK does not download executable code at runtime on first run. The file
watcher's fallback download path (`RipgrepBinary`) is never reached because a
working `rg` is already on `PATH`.

## Tools and how they are executed

| Tool | Storage | Execution |
| --- | --- | --- |
| opencode server | `nativeLibraryDir/libopencode.so` | `ProcessBuilder` (direct `execve`, allowed) |
| ripgrep | `nativeLibraryDir/librg.so` | app-data symlink `files/bin/rg` -> ELF in nativeLibraryDir |
| bun / node | `nativeLibraryDir/libbun.so` | app-data symlinks `files/bin/bun`, `files/bin/node` |
| git | `nativeLibraryDir/libgit.so` | app-data symlink `files/bin/git`; shared libraries and templates come from the extracted Termux runtime (`files/usr`) via `LD_LIBRARY_PATH`, `GIT_EXEC_PATH`, `GIT_TEMPLATE_DIR` |
| git remote helpers | `nativeLibraryDir/libgit-remote-http.so` | symlinks `files/usr/libexec/git-core/git-remote-{http,https}` |
| shell | `/system/bin/sh` | system partition, always executable |

Observation from this port: the server successfully spawns ELF binaries and
symlinks-to-ELF from app storage, but app-data **shell scripts are not
launched** by the server process even though the same script executes under
`run-as`. The host therefore avoids script wrappers entirely and executes ELFs
from `nativeLibraryDir` only; helper scripts that git sources internally are
data files, not executed entrypoints.

## What cannot be dynamically installed and executed

- arbitrary ELF binaries downloaded into app storage and executed with
  `execve` (blocked by SELinux on targetSdk 29+);
- static musl binaries (ET_EXEC/non-PIE is rejected, and Android's seccomp
  filter kills static musl startup with SIGSYS on targetSdk 29+);
- native Node/Bun addons (`.node` / Rust FFI `.so`) that have no Android
  prebuild, if they are dlopen'd from app data. Packages shipped in the APK in
  `nativeLibraryDir` are fine.

Optional tools installed later by the user into app storage follow the same
rule: they must be exposed through `nativeLibraryDir` (packaged in the APK) or
through the build-time tool pipeline (`script/android/`). There is no runtime
"download and exec" path.

## Networking policy

- The server binds `127.0.0.1` only (`serve --hostname=127.0.0.1`).
- No LAN exposure by default.
- Provider and git traffic originates from the device; no proxy is introduced.
- Cleartext HTTP is allowed because the UI is served over loopback; provider
  traffic uses HTTPS with Bun's system CA support (the build bakes in
  `--use-system-ca`).

## Process model

- The server is a child process of the app, inheriting the app's UID and
  SELinux domain (`untrusted_app`). It may spawn `/system/bin/sh` and the
  bundled tools.
- The WebView renders `http://127.0.0.1:<port>/`; the port is chosen at
  runtime (free ephemeral port) and health-checked before loading.
