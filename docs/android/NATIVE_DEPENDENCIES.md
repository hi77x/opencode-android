# Native dependency audit

Scoped to dependencies that compile or ship native code and are on the server
path. Platform list shows what upstream supports; "Android status" is what was
verified on the test device (Android 15, arm64, Bun 1.4.2 standalone binary).

| Dependency | Used by | Required Phase 1 | Upstream platforms | Android availability | Strategy | License | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `bun:sqlite` (built into Bun; `#sqlite` bun path) | `packages/core/src/database/sqlite.bun.ts` | yes | Bun targets | built into Bun Android | none needed | MIT (Bun) / SQLite public domain | **works** (server boots, session store persists) |
| `@ff-labs/fff-bun` + `@ff-labs/fff-bin-*` | fuzzy file search (`#fff`) | no | linux glibc/musl, darwin, win32 | no Android prebuild; lookup throws | guard in `fff.bun.ts` -> `available()` false -> ripgrep fallback | MIT | **degraded by design** |
| `bun-pty` (Rust FFI `.so`) | PTY/terminal endpoints (`#pty` bun path), lazily imported | no | linux, darwin, win32 | no Android `.so` in package | terminal feature unavailable; import is lazy so server is unaffected. Future: build rust-pty crate for `aarch64-linux-android` and expose it from `nativeLibraryDir` | MIT | **unavailable (documented)** |
| `@lydell/node-pty` (+ 6 platform packages) | PTY on the node variant | no | linux/darwin/win32 | not shipped for Android | not used on the Bun runtime | MIT | n/a |
| `@parcel/watcher` + `@parcel/watcher-<platform>` | filesystem watcher | no | linux (glibc/musl), darwin, win32, **android-arm64 prebuild exists on npm** | binding cannot be bundled inside `bun build --compile` for android | watcher service degrades to no-op; backend selection patched for `android`; no core feature depends on it | MIT | **degraded (documented)** |
| `@silvia-odwyer/photon-node` | image processing helpers | no | native addon, no android build | not loadable | unused on the Phase 1 path | Apache-2.0 | not exercised |
| `web-tree-sitter`, `tree-sitter-bash`, `tree-sitter-powershell` (WASM) | shell command parsing | yes (shell tool) | wasm | wasm runs under Bun | none | MIT | expected to work (wasm path; exercised by shell tool) |
| `@opentui/core` + platform packages | TUI renderer | no | desktop platforms | no android package; `OPENTUI_LIBC` define skipped for android | TUI is not used; server/WebView flow does not load it | MIT | not loaded |
| `cross-spawn` | `CrossSpawnSpawner` | yes | pure JS | uses Bun's `child_process` | none | MIT | **works** |
| ripgrep (`librg.so`, built from source with the NDK) | agent search tools | yes | aarch64-linux-android | PIE binary in `jniLibs`, symlinked as `rg` | MIT/Unlicense | **works** |
| git (`libgit.so` + `libgit-remote-http.so` from pinned Termux packages) | project VCS, snapshots, Changes tab | yes | Android/bionic | ELF in `jniLibs`; libs/templates from `assets/git-runtime.zip` extracted to `files/usr` | GPL-2.0-only | **works** (`init/status/add/commit/diff/log`; https clone via `git-remote-http`) |
| Bun runtime (`libbun.so`) | executing JS/TS in the workspace | yes | aarch64-linux-android | bare Bun binary in `jniLibs`, symlinked as `bun` and `node` | MIT | **works** (`node hello.js` in the acceptance run) |

## Notes

- The standalone server binary cannot load native N-API addons that were not
  compiled for `aarch64-linux-android`. Only `bun:sqlite` (built into Bun) and
  WASM modules are guaranteed.
- Tools shipped in `jniLibs` must be PIE; static musl binaries fail at
  `execve` and are additionally killed by the Android seccomp filter
  (SIGSYS) on targetSdk 29+.
- No dependency is hidden: everything that does not work on Android is either
  bypassed with a documented fallback or reported as unavailable.
