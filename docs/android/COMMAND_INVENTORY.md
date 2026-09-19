# External command inventory

Inventory of external executables that the unmodified opencode server may
invoke, gathered from the pinned tree (`ChildProcess.make`, `which(...)`,
`Npm.which`, spawn sites). Commands are grouped by whether Phase 1 needs them.

## Required for the acceptance workflow

| Command | Calling source | Args used | Interactive | PTY | Android implementation |
| --- | --- | --- | --- | --- | --- |
| shell (`Config.shell`, default `/bin/sh`) | `packages/opencode/src/tool/shell.ts:293` `cmd()` -> `ChildProcess.make(command, [], {shell})`; `packages/core/src/tool/bash.ts:158`; `packages/opencode/src/session/prompt.ts:559` | `-c <user command>`-style via shell option | no (pipes) | no | `/system/bin/sh` (mksh + toybox) shipped by Android; configured in the host's generated `opencode.json` |
| `git` | `packages/core/src/git.ts:332,498,796,863,963`; `packages/opencode/src/git/index.ts:113`; `snapshot/index.ts:84,605`; `worktree/index.ts:157`; `project/project.ts:120` | `status`, `diff`, `rev-parse`, `ls-files`, `check-ignore`, `apply`, `cat-file`, `checkout`, `clean`, `init`, `clone`, `fetch`, ... | no | no | bundled PIE `aarch64-linux-android` git exposed as `git` through the app's symlink farm |
| `rg` | `packages/core/src/ripgrep.ts:110` (via `ripgrep/binary.ts` `which("rg")` first) | `--json`, `--files`, `--glob`, ... | no | no | bundled PIE ripgrep (`librg.so`) symlinked as `rg`; `OPENCODE_DISABLE_FFF=true` forces this path |
| `sh` for git hooks / helper scripts | git internals | internal | no | no | `/system/bin/sh` |

## Present but optional / not on the Phase 1 path

| Command | Calling source | Notes / Android status |
| --- | --- | --- |
| `pgrep` | `packages/opencode/src/mcp/index.ts:425` | used to scan local MCP child processes; absent from Android toybox -> local MCP process discovery degrades. No MCP servers configured in Phase 1 |
| `tar` | `packages/core/src/ripgrep/binary.ts:73` | only on the ripgrep download path, which is never taken because `rg` is on PATH |
| `powershell.exe`/`pwsh.exe` | `packages/core/src/ripgrep/binary.ts:59`, `packages/opencode/src/tool/shell.ts:295` | win32 only |
| `cygpath` | `packages/opencode/src/tool/shell.ts:351` | win32 only |
| `gofmt` | `packages/opencode/src/format/formatter.ts:18` | formatter, only if configured and present |
| `prettier` | `formatter.ts:38` | resolved via project `node_modules` or `Npm.which` (downloads); not installed on Android |
| `biome` | `formatter.ts:110`, `lsp/server.ts:331` | same as prettier |
| `clang-format` | `formatter.ts:167` | system binary; not present |
| `rustfmt` | `formatter.ts:350` | system binary; not present |
| `deno` | `lsp/server.ts:103` | LSP; not present |
| `typescript-language-server`, `@vue/language-server`, `pyright`, `oxlint`, `oxc_language_server`, `biome` | `lsp/server.ts` via `Npm.which` | LSP installs packages into the opencode cache and spawns them with node; no node runtime on Android -> LSP disabled in Phase 1 |
| `gopls`, `go`, `ruby`, `gem`, `ty`, `elixir-ls`, `elixir`, `zls`, `zig`, `roslyn-language-server`, `dotnet`, `fsautocomplete` | `lsp/server.ts` | discovered via `which`; absent -> LSP disabled per language |
| `git` LSP/formatter subprocesses | various | covered by bundled git where applicable |
| `opencode` installer helpers | `packages/opencode/src/installation/index.ts` | update/install detection; irrelevant for the embedded build |
| MCP local servers | user configuration | any user-configured local MCP command runs through the same `ChildProcessSpawner`; binaries must be packaged in `jniLibs` to be executable |

## Interactive / PTY consumers

- Terminal (PTY) endpoints: `#pty` -> `bun-pty` on Bun. No Android native
  library exists in the package, so interactive terminal sessions are not
  available in Phase 1 (lazily imported; the rest of the server is
  unaffected).
- The shell tool used by the agent runs non-interactively over pipes and does
  not require a PTY.
