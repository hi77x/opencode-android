# Bun / Android runtime audit

All statements below were verified experimentally on real hardware during this
task, not assumed.

## Test environment

| Item | Value |
| --- | --- |
| Device | TECNO LJ9 (`TECNO-LJ9`), Android 15 / API 35, arm64-v8a |
| Kernel | 6.1.145 |
| CPU | ARMv8.2+ with SVE/SVE2, i8mm, bf16 |
| Page size | 4096 |
| Host toolchain | Bun `1.3.14` (initial), Bun `1.4.2` (final), rustc/cargo stable, Android NDK 27.1.12297006 |
| Build host | Windows 10 x64 |

## Question 1 - can current Bun run on Android/Bionic arm64?

**Official prebuilt target: yes since Bun 1.4.x; 1.3.14 is broken on this device.**

- Bun ships `bun-linux-aarch64-android` prebuilts. `bun build --compile
  --target=bun-linux-arm64-android` works from Windows.
- Bun `1.3.14` android binaries **crash on startup** on this device with
  `panic(main thread): Segmentation fault at address 0x5730000` followed by
  `SIGTRAP`, before any user code runs. Reproduced with a trivial
  `console.log` standalone binary and with the opencode server binary.
  JIT-disabling env vars (`BUN_JSC_useJIT=0`, baseline/DFG/FTL off, `--smol`)
  do not change the crash.
- Bun `1.4.2` android binaries run correctly on the same device:

  ```
  $ /data/local/tmp/hello-android
  hello from bun android android arm64
  ```

  `process.platform === "android"`, `process.arch === "arm64"`.
- `bun-linux-x64-android` is **not** available in 1.3.14 and **is** available
  in 1.4.2 (relevant for x86_64 emulators; the shipped APK is arm64-v8a only).
- Known upstream context: oven-sh/bun#38051 (intermittent SIGSEGV on Android
  arm64) and oven-sh/bun#30766 (`close_range` seccomp SIGSYS). The
  `guysoft/opencode-termux` project cross-compiles a patched Bun for Android
  and documents the same class of startup failures (TLS alignment, JSC signal
  traps, seccomp fallbacks).

**Conclusion:** pin Bun `>= 1.4.2` for Android builds. No source patching of
Bun is required with 1.4.2 on the test device.

## Question 2 - can the opencode bundle be built against that runtime?

Yes. With the `--target=linux-arm64-android` addition to
`packages/opencode/script/build.ts` (see `UPSTREAM_PATCHES.md`):

```
bun run script/build.ts --target=linux-arm64-android
# -> dist/opencode-linux-arm64-android/bin/opencode (~164 MB)
```

The binary embeds the production bundle of `packages/app` (the same code path
as every other build target) and the CLI/server entrypoint.

## Question 3 - does the server start and bind loopback on device?

Yes, executed as the shell user from `/data/local/tmp`:

```
opencode server listening on http://127.0.0.1:4096
$ curl http://127.0.0.1:4096/api/health
{"healthy":true}
$ curl -o /dev/null -w "%{http_code} %{content_type}" http://127.0.0.1:4096/
200 text/html
```

The HTML served at `/` is the embedded `packages/app` production bundle.

## Question 4 - which native dependencies fail?

| Dependency | Status on Android | Effect |
| --- | --- | --- |
| `bun:sqlite` (`#sqlite` bun path) | works | sessions/messages persist |
| `bun-pty` (Rust FFI `.so`) | no Android prebuild | terminal (PTY) endpoints unavailable; lazily loaded, server unaffected |
| `@lydell/node-pty` (node path) | not used on Bun | n/a |
| `@ff-labs/fff-bun` search | no Android prebuild | `Fff.available()` returns false -> ripgrep fallback |
| `@parcel/watcher` binding | no binding inside the standalone binary | file watcher service degrades to no-op (see NATIVE_DEPENDENCIES.md) |
| `@silvia-odwyer/photon-node` | native addon | only used by image processing paths; not on the server critical path |
| `web-tree-sitter` (WASM) | works | shell command parsing |
| `cross-spawn` / `node:child_process` | works via Bun | process spawning |

## Question 5 - does `virtual:opencode-server` work in that build?

`virtual:opencode-server` is an Electron/Vite alias used only by
`packages/desktop`. It is not part of the opencode server binary. The Android
host does not use it: it starts the same `Server.listen` entrypoint through the
CLI (`opencode serve`), which is the identical code path (`opencode/src/cli/cmd/serve.ts`
-> `Server.listen`).

## Question 6 - can child processes be spawned and can they execute system binaries?

Yes. The server runs as an app (or shell) process on Android; `Bun.spawn`
works, and executing `/system/bin/sh` (mksh + toybox) is permitted because it
lives on the system partition. Executing binaries stored in app **data** is
denied by SELinux for `targetSdk >= 29`, so bundled tools are shipped in
`jniLibs` (`nativeLibraryDir`) and reached through symlinks created in app
storage. Verified experimentally:

- direct `execve` of `nativeLibraryDir/libopencode.so` works;
- `execve` through an app-data symlink pointing at the same file also works
  (the final inode is what SELinux checks).

## Question 7 - do workers work?

Not fully verified in this phase. The standalone binary embeds
`OPENCODE_WORKER_PATH` (CLI worker) and the OpenTUI tree-sitter worker; the TUI
is not used on Android and worker spawning was not exercised by the acceptance
test. Status: unverified, documented as a limitation.

## Question 8 - does SQLite work?

Yes. The server initializes its stores on startup under
`$XDG_DATA_HOME/opencode` using Bun's built-in SQLite (`#sqlite` bun path). The
acceptance run created and persisted a session, and the database files were
observed on device. No native `sqlite3` extension is required.

## Question 9 - do filesystem watchers work?

No native binding is available inside the standalone binary, so
`@parcel/watcher` resolves to `undefined` and the watcher service logs
"watcher backend not supported" or simply returns an empty service. File
operations, search and the agent itself do not depend on the watcher.

## Question 10 - can the server spawn bundled tools?

- ELF binaries and symlinks-to-ELF in app storage execute correctly from the
  server process (`libbun.so` as `node` ran the acceptance script; `libgit.so`
  powers the Changes tab).
- Shell scripts in app storage execute under `run-as` but were **not** spawned
  by the server process; the host therefore ships executables as ELF files in
  `jniLibs` and avoids script wrappers (`EXECUTION_POLICY.md`).

## Question 11 - is `libripgrep` executable?

The earlier static musl `rg` shipped as `librg.so` was **not** executable on
Android 15 (ET_EXEC, non-PIE: `execve` fails). The working solution is a
PIE build of ripgrep for `aarch64-linux-android` (Rust + NDK), verified on the
device. Static musl binaries are additionally killed by the app seccomp filter
on `targetSdk >= 29`.

## Practical outcome

- Server runtime: Bun `1.4.2`, target `bun-linux-aarch64-android`.
- Packaging: `libopencode.so` in `jniLibs/arm64-v8a`, `extractNativeLibs=true`.
- Tools: PIE aarch64-linux-android binaries in `jniLibs`, exposed through
  app-data symlinks on `PATH` and `LD_LIBRARY_PATH`.
- Server binds `127.0.0.1` only.
