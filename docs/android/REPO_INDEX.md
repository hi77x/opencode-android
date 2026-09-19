# Repository index (pinned b02acc1e)

Semantic index of the subsystems that matter for the Android port. File
references are relative to the repository root at the pinned commit; line
numbers point at the entry symbol. "Android status" was verified on the test
device unless marked unverified.

Inventory: `git ls-files` = 6626 tracked files. Boundary search over
`packages/**` for the runtime keywords listed in the task produced 1204 hits;
subsystem search over `packages/{core,opencode,server,desktop}` produced 2684
hits. Outputs were used to build this index.

## frontend

- Package: `packages/app`
- Entry: `src/app.tsx`; platform context `src/context/platform.tsx`;
  server context `src/context/server.tsx`; SDK context `src/context/sdk.tsx`
- Build: `bun run build` (Vite) -> `packages/app/dist`
- Dependencies: SolidJS, Vite, Tailwind, Playwright (dev)
- Platform assumptions: DOM/browser; `window.api` optional desktop bridge
- Android status: **read-only, embedded in the server binary and served over
  loopback; loaded in a WebView unchanged**

## desktop host / renderer bootstrap / preload bridge / sidecar startup

- Package: `packages/desktop` (Electron)
- Host: `src/main/index.ts` (`spawnLocalServer(...)` at line 379),
  `src/main/server.ts` (`spawnLocalServer` line 57 -> `utilityProcess.fork`
  of `sidecar.js`), `src/main/sidecar.ts` (line 57
  `import("virtual:opencode-server")` -> `Server.listen` line 59)
- Bridge: `src/main/ipc.ts`, `src/preload/index.ts`, `src/preload/types.ts`;
  renderer bootstrap `src/renderer/index.tsx` (`window.api.awaitInitialization()`)
- Virtual alias: `electron.vite.config.ts:68` resolves
  `virtual:opencode-server` to `OPENCODE_SERVER_DIST/node.js`
- Health check: `server.ts:186` polls `/api/health` and `/global/health`
- Platform assumptions: Electron, `utilityProcess`, `process.parentPort`,
  system certificates, env proxy
- Android status: **not copied**; `packages/android` reproduces only the host
  responsibilities (start server, wait for health, host UI). No sidecar fork is
  needed because the server runs as its own process.

## HTTP server

- Package: `packages/opencode`
- Entry: `src/server/server.ts` `listen` (line 73) / `Server.listen` effect
  (line 83); `@effect/platform-node` `NodeHttpServer` + `node:http`
  `createServer` (line 200)
- CLI entrypoints: `src/cli/cmd/serve.ts:19`, `src/cli/cmd/web.ts:44`
- UI serving: `src/server/shared/ui.ts` (`embeddedUI` line 44 reads the
  generated `opencode-web-ui.gen.ts`; `serveUIEffect` line 78)
- Routes: `src/server/routes/instance/httpapi/*`
- Platform assumptions: Node HTTP; loopback bind; no OS-specific calls
- Android status: **works** (`serve --hostname=127.0.0.1`; health 200; `/`
  serves the embedded UI)

## agent execution

- Package: `packages/opencode` (+ `packages/core`)
- Entry: `src/session/*` (V2 session core; runner under
  `src/session/runner`); prompt loop `src/session/prompt.ts`
- Dependencies: provider SDKs (`packages/llm`, ai-sdk providers), core services
- Platform assumptions: network only
- Android status: **works** (acceptance test drove a real model turn)

## tool execution

- Packages: `packages/core/src/tool/*` (bash, edit, read, write, glob, grep,
  apply-patch, read-filesystem, webfetch, websearch, todowrite, skill),
  `packages/opencode/src/tool/*` (shell, registry wiring)
- Entry: `packages/core/src/tool/registry.ts`, `packages/core/src/tool/bash.ts`
  (spawn at line 158), `packages/opencode/src/tool/shell.ts` (`cmd()` line 293)
- Platform assumptions: shell available; filesystem paths real
- Android status: **works** (`/system/bin/sh`, real files, rg-backed search)

## shell execution

- Entry: `packages/opencode/src/tool/shell.ts` (`cmd()` line 293 ->
  `ChildProcess.make(command, [], { shell })`), shell from
  `packages/core/src/config.ts:33` (`shell` config key)
- Android status: **configured to `/system/bin/sh`** by the Android host;
  non-interactive, pipe-based

## process spawning

- Entry: `packages/core/src/process.ts` (`AppProcess`, `Layer` line 142,
  `run` line 199, `node` line 259) and
  `packages/core/src/cross-spawn-spawner.ts` (`ChildProcessSpawner` layer
  line 500, `node` line 505)
- Dependencies: `cross-spawn`, `effect/unstable/process`
- Platform assumptions: Node `child_process` semantics: cwd, env, pipes,
  exit codes, signals, detached, timeouts
- Android status: **works through Bun's Node compatibility layer**; no
  competing abstraction was added

## PTY

- Entry: `packages/core/src/pty.ts` (lazy `import("#pty")` line 18);
  implementations `src/pty/pty.bun.ts` (bun-pty) and `src/pty/pty.node.ts`
  (@lydell/node-pty)
- Android status: **unavailable**; `bun-pty` ships no Android `.so`. Lazy
  import keeps the server healthy. Terminal endpoints degrade. Documented in
  `NATIVE_DEPENDENCIES.md`.

## filesystem

- Entry: `packages/core/src/fs-util.ts` (`FSUtil`, `readDirectoryEntries`
  line 79), `src/file.ts`, `src/filesystem/*` (search, fff, watcher, ignore,
  protected)
- Platform assumptions: real POSIX paths, `node:fs`
- Android status: **works** in app-private storage; `readdir` undefined-entry
  crash guarded (patch 4)

## database

- Entry: `packages/core/src/database/sqlite.bun.ts` / `sqlite.node.ts`
  selected via `#sqlite` import condition (`packages/core/package.json`)
- Dependencies: `bun:sqlite` (Bun) or node sqlite layer; drizzle ORM
- Android status: **works** (Bun built-in SQLite; stores under
  `$XDG_DATA_HOME/opencode`)

## file watcher

- Entry: `packages/core/src/filesystem/watcher.ts` (`watcher` lazy require of
  `@parcel/watcher-<platform>-<arch>[-libc]`, `getBackend` line 38); consumed
  via `Service` layer; flag `OPENCODE_EXPERIMENTAL_DISABLE_FILEWATCHER`,
  `OPENCODE_EXPERIMENTAL_FILEWATCHER`
- Android status: **degrades to no-op** (no binding inside the standalone
  binary); backend mapping patched for `android`

## git

- Entry: `packages/core/src/git.ts` (`run` line 954 / `execute` line 959
  spawn `git`), consumers `packages/opencode/src/git/index.ts:113`,
  `snapshot/index.ts:84,605`, `worktree/index.ts:157`,
  `project/project.ts:120`
- Platform assumptions: `git` found on PATH (via shell `extendEnv`)
- Android status: **bundled git** exposed on PATH (see final report for what
  was verified)

## ripgrep / search

- Entry: `packages/core/src/ripgrep.ts` (`layer` line 92, spawn line 110),
  binary resolution `src/ripgrep/binary.ts` (`which("rg")` line 94; download
  fallback lines 104-120)
- Fallback selection: `src/filesystem/search.ts:235`
  (`OPENCODE_DISABLE_FFF || !Fff.available() ? ripgrepLayer : fffLayer`)
- Android status: **works**; bundled PIE ripgrep; fff disabled by env and by
  the `available()` guard

## LSP spawning

- Entry: `packages/opencode/src/lsp/lsp.ts`, `server.ts`, `launch.ts`
  (`spawn(bin, ...)` for typescript-language-server, gopls, pyright, biome,
  oxc, zls, ... discovered through `which()` or `Npm.which`)
- Android status: **disabled**; no servers and no node/bun runtime on PATH for
  downloaded JS servers. Server keeps running; diagnostics simply absent

## formatter spawning

- Entry: `packages/opencode/src/format/index.ts:86`,
  `formatter.ts` built-ins (gofmt, prettier, biome, clang-format, rustfmt, ...)
- Android status: **disabled unless a formatter is bundled**; no built-ins
  present in Phase 1

## plugin spawning

- Package: `packages/core/src/config/plugin/*`, `packages/opencode/src/plugin/*`
- Android status: plugins are user-provided; JS plugins run in-process, local
  process plugins follow the process rules (must live in `jniLibs` to exec)

## MCP local process spawning

- Entry: `packages/opencode/src/mcp/index.ts` (local servers spawned through
  the spawner; `pgrep` scan line 425)
- Android status: **unavailable in Phase 1** (no MCP configured; `pgrep`
  missing from toybox)

## networking / TLS

- Entry: Bun `fetch`/`node:http`/`node:tls`; desktop sidecar sets system CAs
  (`packages/desktop/src/main/sidecar.ts:111`) and loopback `NO_PROXY`
- Android status: **works**; provider HTTPS verified in the acceptance run;
  host also sets loopback `NO_PROXY`; server binds 127.0.0.1

## workspace management

- Entry: `packages/core/src/location*`, `packages/opencode/src/worktree/index.ts`,
  `packages/opencode/src/project/project.ts`
- Android status: **works**; workspace root is `<app files>/workspaces/default`

## configuration directories

- Entry: `packages/core/src/global.ts` (`xdg-basedir`; `XDG_*`, `HOME`,
  `OPENCODE_CONFIG_DIR`), `packages/core/src/flag/flag.ts`
- Android status: **works**; host sets `HOME` and all `XDG_*` under app files

## workers

- Symbols: `OPENCODE_WORKER_PATH` (build define; `src/cli/tui/worker.ts`),
  `OTUI_TREE_SITTER_WORKER_PATH` (build define)
- Android status: **not exercised** (TUI not used). Unverified.

## native Node/Bun modules

- See `NATIVE_DEPENDENCIES.md`. Summary: only `bun:sqlite` and WASM modules
  are guaranteed inside the standalone Android binary.

## build pipeline

- `packages/opencode/script/build.ts`: builds the web UI bundle, embeds it via
  `opencode-web-ui.gen.ts`, compiles standalone binaries per target
  (`Bun.build({ compile: { target: "bun-<os>-<arch>[-<abi>]" } })`),
  writes per-target `package.json`
- `packages/opencode/script/build-node.ts`: node-target build path (not used
  by the Android port)
- `packages/cli/script/build.ts`, `packages/desktop` electron-vite builds:
  other distribution channels
- Android additions: `script/android/*` (new), target flag in `build.ts`
