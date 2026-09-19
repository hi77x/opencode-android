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

## opencode для Android

Это форк официального [opencode](https://github.com/anomalyco/opencode) с полноценной сборкой под Android: приложение запускает **настоящий opencode локально на телефоне** — со встроенным сервером, сессиями, агентом, инструментами и тем же самым веб-интерфейсом, что и на компьютере.

Никакого ПК, VPS, Termux и удалённых серверов: всё работает на устройстве.

### Что внутри

- Тот же UI `packages/app` (не изменялся) открывается в WebView с локального сервера.
- Настоящий opencode-сервер собран как `libopencode.so` (Bun 1.4.2, target `bun-linux-arm64-android`).
- Git прямо в приложении: вкладка **Changes** показывает реальные диффы с подсветкой синтаксиса, работает создание репозитория.
- Вкладка **Files** — браузер файлов проекта.
- Вкладка **Usage** и кружок контекста — расход токенов и разбивка контекста, как в desktop-версии.
- Выполнение команд и JS/TS: shell (`/system/bin/sh`), `git`, `rg`, `bun`/`node` (рантайм Bun для Android).
- Всё исполняемое лежит внутри APK (jniLibs) и не докачивается — это требование Android 10+ (W^X).

### Скачать

Готовый APK — в [releases](https://github.com/hi77x/opencode-android/releases). Подпись: release-ключ проекта.

```
adb install -r app-release.apk
```

### Первый запуск

1. Откройте приложение — сервер поднимется сам и откроется UI.
2. **Настройки → Провайдеры** — подключите провайдера и введите API-ключ (OpenAI-совместимый endpoint тоже подойдёт).
3. Создайте проект (кнопка «Добавить проект» → папка `~/workspace` или любая папка внутри хранилища приложения) и начинайте сессию.

### Сборка из исходников

```sh
./script/android/build-apk.sh
# результат: packages/android/app/build/outputs/apk/debug/app-debug.apk
./packages/android/gradlew -p packages/android :app:assembleRelease
```

Требования: Bun ≥ 1.4.2, Python 3, curl, JDK 17/21, Android SDK 35. Для сборки `rg` нужен NDK и Rust (`script/android/build-ripgrep.sh`). Релизная подпись читается из `packages/android/local.properties` (`RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`) и `packages/android/keystore/`; без них release собирается debug-ключом.

### Ограничения

- Терминал (PTY) пока недоступен: у `bun-pty` нет сборки под Android.
- LSP и форматтеры отключены (нет соответствующих бинарников на устройстве).
- Локальные MCP-процессы и fuzzy-поиск `fff` недоступны; поиск работает через `rg`.
- Нативный файловый watcher без бинаря — обновления файлов подхватываются операциями агента, а не мгновенным слежением.

### Лицензия

MIT — та же, что и у оригинала (см. [LICENSE](LICENSE)). Третьи стороны, попадающие в APK при сборке (git — GPL-2.0, ripgrep — MIT/Unlicense, Bun — MIT), загружаются скриптами из официальных источников.

<details>
<summary>English</summary>

### opencode for Android (English)

A fork of [opencode](https://github.com/anomalyco/opencode) with a complete Android build. The app runs the real opencode locally on the phone: the same server, sessions, agent, tools and the unmodified `packages/app` web UI (served from loopback inside a WebView).

Includes git with syntax-highlighted diffs, a project file browser, the context-usage panel, and JS/TS execution via a bundled Bun runtime. No PC, no Termux, no remote server.

Download the APK from [releases](https://github.com/hi77x/opencode-android/releases). Build with `./script/android/build-apk.sh`.

Known limitations: no PTY terminal, no LSP/formatters, no local MCP processes; search uses `rg`.

MIT license, same as upstream.

</details>

---

[![OpenCode Terminal UI](packages/web/src/assets/lander/screenshot.png)](https://opencode.ai)

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
