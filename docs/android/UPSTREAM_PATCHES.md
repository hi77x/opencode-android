# Upstream modifications

Every change to a file that exists upstream at the pinned commit is listed
here. New files under `packages/android`, `script/android` and `docs/android`
are not upstream modifications and are not listed.

## 1. `packages/opencode/script/build.ts`

- **Reason:** add an Android build target so the unmodified server can be
  compiled with `bun build --compile`.
- **Why new-file-only was impossible:** the target list, target selection and
  compile defines are defined exclusively in this file; there is no
  configuration hook.
- **Behavior before:** `--target=` flag did not exist; targets were either the
  full cross-platform matrix or the host platform via `--single`; no
  `android` ABI; `OPENCODE_LIBC` was `glibc`/`musl` only.
- **Behavior after:**
  - new `--target=<os>-<arch>[-<abi>]` flag filters `allTargets`;
  - `linux-arm64-android` and `linux-x64-android` targets added;
  - `OPENCODE_LIBC` is `'bionic'` for android targets, and the
    `process.env.OPENTUI_LIBC` define is skipped for android (no OpenTUI
    Android shared library exists);
  - generated `package.json` uses `os: ["android"]` and omits `libc` for
    android;
  - exits with an error when a `--target=` value matches nothing.

## 2. `packages/core/src/filesystem/fff.bun.ts`

- **Reason:** on Android there is no `@ff-labs/fff-bin-*` package, so
  `FileFinder.isAvailable()` throws during capability detection instead of
  returning `false`.
- **Why new-file-only was impossible:** the capability check is inside the fff
  implementation itself, selected through the `#fff` import condition.
- **Behavior before:** `available()` propagated the native lookup error.
- **Behavior after:** `available()` returns `false` on unsupported platforms;
  search transparently falls back to the ripgrep layer
  (`filesystem/search.ts` already selects `ripgrepLayer` when fff is
  unavailable).

## 3. `packages/core/src/filesystem/watcher.ts`

- **Reason:** on `process.platform === "android"` the backend selector had no
  branch, so the watcher service logged an error and disabled itself even if a
  binding were available.
- **Why new-file-only was impossible:** backend choice is a local function in
  this file.
- **Behavior before:** `getBackend()` returned `undefined` on Android.
- **Behavior after:** Android maps to the `inotify` backend (same mechanism as
  Linux). If the native binding is missing the service still degrades
  gracefully; this patch only removes the platform gap.

## 4. `packages/core/src/fs-util.ts`

- **Reason:** Bun on Android can return `undefined` entries from
  `readdir(..., { withFileTypes: true })` for directories it cannot stat,
  which crashed `readDirectoryEntries` with a null dereference.
- **Why new-file-only was impossible:** the mapping loop is inside the
  filesystem service implementation.
- **Behavior before:** any `undefined` entry threw `TypeError`.
- **Behavior after:** malformed entries are skipped; directory listings for
  otherwise readable directories succeed.

## 5. `packages/core/src/filesystem/search.ts`

- **Reason:** `filesystem.ts` imports `FileSystemSearch` and `search.ts`
  imported the `FileSystem` namespace back from `filesystem.ts`, forming a
  runtime import cycle. In the Android bundle the cycle resolved such that
  `FileSystem.node` was `undefined` while `plugin/internal.ts` was evaluated,
  which crashed every request that built the location services
  (`Undefined layer node in group > plugin-internal > @opencode/v2/FileSystem`).
- **Why new-file-only was impossible:** the cycle lives in the two existing
  modules; the smallest safe fix is to remove the runtime edge.
- **Behavior before:** `search.ts` used `FileSystem.Entry/FindInput/Match`
  values and `FileSystem.GlobInput/GrepInput` types from the cycle.
- **Behavior after:** value classes are imported from their source
  (`@opencode-ai/schema/filesystem`, which `filesystem.ts` re-exports) and the
  two input types use a type-only import, so no runtime cycle remains.
  Behavior is unchanged; the same classes are used.

This was verified by reproducing the crash on device and re-testing
`/file`, `/find/file` and the system prompt after the fix.

## Not modified (deliberately)

- `packages/app/**` - the frontend is frozen (see final report / `git diff`).
- `packages/desktop/**` - Electron host studied but not copied to Android.
- `packages/core/src/process.ts`, `cross-spawn-spawner.ts` - the existing
  process abstraction is used unchanged; Bun provides the Android
  implementation underneath it.
- `Server.listen` and all server routes - started through the existing CLI
  entrypoint.

The prior experimental branch `android-app-snapshot` contained additional
patches (Termux CLI tweaks, native Compose client, `open-url` shim). They were
**not** carried over; the Android host in `packages/android` is new code and
does not require them.
