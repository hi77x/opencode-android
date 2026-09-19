<p align="center">
  <a href="https://opencode.ai">
    <picture>
      <source srcset="packages/console/app/src/asset/logo-ornate-dark.svg" media="(prefers-color-scheme: dark)">
      <source srcset="packages/console/app/src/asset/logo-ornate-light.svg" media="(prefers-color-scheme: light)">
      <img src="packages/console/app/src/asset/logo-ornate-light.svg" alt="OpenCode logo">
    </picture>
  </a>
</p>
<p align="center">The open source AI coding agent.</p>
<p align="center">
  <a href="https://opencode.ai/discord"><img alt="Discord" src="https://img.shields.io/discord/1391832426048651334?style=flat-square&label=discord" /></a>
  <a href="https://www.npmjs.com/package/opencode-ai"><img alt="npm" src="https://img.shields.io/npm/v/opencode-ai?style=flat-square" /></a>
  <a href="https://github.com/anomalyco/opencode/actions/workflows/publish.yml"><img alt="Build status" src="https://img.shields.io/github/actions/workflow/status/anomalyco/opencode/publish.yml?style=flat-square&branch=dev" /></a>
</p>

<p align="center">
  <a href="README.md">English</a> |
  <a href="README.zh.md">简体中文</a> |
  <a href="README.zht.md">繁體中文</a> |
  <a href="README.ko.md">한국어</a> |
  <a href="README.de.md">Deutsch</a> |
  <a href="README.es.md">Español</a> |
  <a href="README.fr.md">Français</a> |
  <a href="README.it.md">Italiano</a> |
  <a href="README.da.md">Dansk</a> |
  <a href="README.ja.md">日本語</a> |
  <a href="README.pl.md">Polski</a> |
  <a href="README.ru.md">Русский</a> |
  <a href="README.bs.md">Bosanski</a> |
  <a href="README.ar.md">العربية</a> |
  <a href="README.no.md">Norsk</a> |
  <a href="README.br.md">Português (Brasil)</a> |
  <a href="README.th.md">ไทย</a> |
  <a href="README.tr.md">Türkçe</a> |
  <a href="README.uk.md">Українська</a> |
  <a href="README.bn.md">বাংলা</a> |
  <a href="README.gr.md">Ελληνικά</a> |
  <a href="README.vi.md">Tiếng Việt</a>
</p>


---

## opencode for Android

This fork makes the existing opencode run natively on Android: the phone runs the real server, the agent, sessions and the `packages/app` web UI, served over loopback in a WebView. The Android host adds mobile styles, a read-only Files browser and a Usage panel without modifying the `packages/app` source. No PC, Termux, root or remote OpenCode server is required; cloud models still need a network connection and provider credentials.

What we added on top of upstream (everything else is upstream opencode):

| Path | Contents |
| --- | --- |
| [`packages/android`](packages/android) | Android host: Gradle project, WebView activity, embedded server manager, mobile UI adaptations |
| [`script/android`](script/android) | Reproducible build: [`build-apk.sh`](script/android/build-apk.sh) plus helpers for the server, ripgrep, git runtime and translations |
| [`docs/android`](docs/android) | Engineering docs: [build](docs/android/BUILD.md), [execution policy](docs/android/EXECUTION_POLICY.md), [native dependencies](docs/android/NATIVE_DEPENDENCIES.md), [upstream patches](docs/android/UPSTREAM_PATCHES.md) |

Only five upstream files are modified (Android build target plus runtime compatibility); each change is documented with its reason in [`docs/android/UPSTREAM_PATCHES.md`](docs/android/UPSTREAM_PATCHES.md).

How it works: the Android activity starts the bundled server (`libopencode.so serve --hostname=127.0.0.1`) on a port selected at startup and loads `http://127.0.0.1:<port>/` in a WebView. Tools run from the app's native library directory: `/system/bin/sh`, bundled `git`, `rg`, and a Bun runtime exposed as `bun`/`node`.

- **Download**: [releases](https://github.com/hi77x/opencode-android/releases) (`app-release.apk`, arm64, Android 8.0+)
- **Build**: `./script/android/build-apk.sh` — see [`docs/android/BUILD.md`](docs/android/BUILD.md)
- **First launch**: grant all-files access for persistent data under `/sdcard/OpenCode/home`, then Settings → Providers → connect a provider and add a project (`~/workspace`). Without that permission, data is in app-private storage and is removed on uninstall. WebView project selection may need to be restored after reinstall.
- **Works**: server and web UI, sessions, git with syntax-highlighted diffs (Changes), project file browser (Files), context-usage panel (Usage), command and JS/TS execution
- **Limitations**: no PTY terminal, no LSP or formatters, no local MCP processes; the native file-watcher binding is unavailable (search uses `rg`)
- **License**: MIT, same as upstream

[![OpenCode Terminal UI](packages/web/src/assets/lander/screenshot.png)](https://opencode.ai)

> [!IMPORTANT]
> The `v0.1.1` release tag points to an older upstream commit without the Android module. The Android source is on `main`; use that branch for the [build instructions](docs/android/BUILD.md). The release APK has not been verified as a byte-for-byte build of `main`.

---

### Installation

```bash
# YOLO
curl -fsSL https://opencode.ai/install | bash

# Package managers
npm i -g opencode-ai@latest        # or bun/pnpm/yarn
scoop install opencode             # Windows
choco install opencode             # Windows
brew install anomalyco/tap/opencode # macOS and Linux (recommended, always up to date)
brew install opencode              # macOS and Linux (official brew formula, updated less)
sudo pacman -S opencode            # Arch Linux (Stable)
paru -S opencode-bin               # Arch Linux (Latest from AUR)
mise use -g opencode               # Any OS
nix run nixpkgs#opencode           # or github:anomalyco/opencode for latest dev branch
```

> [!TIP]
> Remove versions older than 0.1.x before installing.

### Desktop App (BETA)

OpenCode is also available as a desktop application. Download directly from the [releases page](https://github.com/anomalyco/opencode/releases) or [opencode.ai/download](https://opencode.ai/download).

| Platform              | Download                           |
| --------------------- | ---------------------------------- |
| macOS (Apple Silicon) | `opencode-desktop-mac-arm64.dmg`   |
| macOS (Intel)         | `opencode-desktop-mac-x64.dmg`     |
| Windows               | `opencode-desktop-windows-x64.exe` |
| Linux                 | `.deb`, `.rpm`, or `.AppImage`     |

### Mobile App (BETA)

OpenCode also runs on Android as a native APK built from this fork. The app bundles the real server and the same web UI; with all-files access, server data lives under `/sdcard/OpenCode/home` and survives app removal. Without it, data is kept in app-private storage and is removed on uninstall.

| Platform | Download | Notes |
| --- | --- | --- |
| Android 8.0+ (arm64) | [`app-release.apk`](https://github.com/hi77x/opencode-android/releases/latest) | BETA - grant all-files access for persistent data |
| Build from source | `./script/android/build-apk.sh` | see [`packages/android/README.md`](packages/android/README.md) |

```bash
# macOS (Homebrew)
brew install --cask opencode-desktop
# Windows (Scoop)
scoop bucket add extras; scoop install extras/opencode-desktop
```

#### Installation Directory

The install script respects the following priority order for the installation path:

1. `$OPENCODE_INSTALL_DIR` - Custom installation directory
2. `$XDG_BIN_DIR` - XDG Base Directory Specification compliant path
3. `$HOME/bin` - Standard user binary directory (if it exists or can be created)
4. `$HOME/.opencode/bin` - Default fallback

```bash
# Examples
OPENCODE_INSTALL_DIR=/usr/local/bin curl -fsSL https://opencode.ai/install | bash
XDG_BIN_DIR=$HOME/.local/bin curl -fsSL https://opencode.ai/install | bash
```

### Agents

OpenCode includes two built-in agents you can switch between with the `Tab` key.

- **build** - Default, full-access agent for development work
- **plan** - Read-only agent for analysis and code exploration
  - Denies file edits by default
  - Asks permission before running bash commands
  - Ideal for exploring unfamiliar codebases or planning changes

Also included is a **general** subagent for complex searches and multistep tasks.
This is used internally and can be invoked using `@general` in messages.

Learn more about [agents](https://opencode.ai/docs/agents).

### Documentation

For more info on how to configure OpenCode, [**head over to our docs**](https://opencode.ai/docs).

### Contributing

If you're interested in contributing to OpenCode, please read our [contributing docs](./CONTRIBUTING.md) before submitting a pull request.

### Building on OpenCode

If you are working on a project that's related to OpenCode and is using "opencode" as part of its name, for example "opencode-dashboard" or "opencode-mobile", please add a note to your README to clarify that it is not built by the OpenCode team and is not affiliated with us in any way.

---

**Join our community** [Discord](https://discord.gg/opencode) | [X.com](https://x.com/opencode)
