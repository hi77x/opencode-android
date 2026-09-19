<p align="center">
  <a href="https://opencode.ai">
    <picture>
      <source srcset="packages/console/app/src/asset/logo-ornate-dark.svg" media="(prefers-color-scheme: dark)">
      <source srcset="packages/console/app/src/asset/logo-ornate-light.svg" media="(prefers-color-scheme: light)">
      <img src="packages/console/app/src/asset/logo-ornate-light.svg" alt="OpenCode logo">
    </picture>
  </a>
</p>
<p align="center">Открытый AI-агент для программирования.</p>
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

Этот форк запускает существующий opencode нативно на Android: на телефоне работают настоящий сервер, агент, сессии и веб-интерфейс `packages/app` через loopback в WebView. Android-хост добавляет мобильные стили, просмотр файлов и панель Usage, не меняя исходники `packages/app`. ПК, Termux, root и удалённый сервер OpenCode не нужны; для облачных моделей нужны интернет и учётные данные провайдера.

Что добавили мы (всё остальное — upstream opencode):

| Путь | Что это |
| --- | --- |
| [`packages/android`](packages/android) | Android-хост: Gradle-проект, WebView-активити, запуск встроенного сервера, адаптация мобильного UI |
| [`script/android`](script/android) | Воспроизводимая сборка: [`build-apk.sh`](script/android/build-apk.sh) и скрипты для сервера, ripgrep, git-рантайма и переводов |
| [`docs/android`](docs/android) | Инженерные документы: [сборка](docs/android/BUILD.md), [политика выполнения](docs/android/EXECUTION_POLICY.md), [нативные зависимости](docs/android/NATIVE_DEPENDENCIES.md), [патчи upstream](docs/android/UPSTREAM_PATCHES.md) |

Изменены всего пять файлов upstream (цель сборки под Android и совместимость рантайма); каждый описан с причиной в [`docs/android/UPSTREAM_PATCHES.md`](docs/android/UPSTREAM_PATCHES.md).

Как это работает: активити запускает встроенный сервер (`libopencode.so serve --hostname=127.0.0.1`) и загружает `http://127.0.0.1:<port>/` в WebView (порт выбирается при запуске). Инструменты запускаются из nativeLibraryDir приложения: `/system/bin/sh`, встроенные `git`, `rg` и рантайм Bun под именами `bun`/`node`.

- **Скачать**: [releases](https://github.com/hi77x/opencode-android/releases) (`app-release.apk`, arm64, Android 8.0+)
- **Сборка**: `./script/android/build-apk.sh` — см. [`docs/android/BUILD.md`](docs/android/BUILD.md)
- **Первый запуск**: разрешите доступ ко всем файлам для хранения данных в `/sdcard/OpenCode/home`, затем Настройки → Провайдеры → подключите провайдера и добавьте проект (`~/workspace`). Без разрешения данные остаются в закрытом хранилище приложения и удаляются при деинсталляции. После переустановки проект может потребоваться выбрать заново.
- **Работает**: сервер и веб-интерфейс, сессии, git с диффами и подсветкой (Changes), браузер файлов проекта (Files), панель использования контекста (Usage), выполнение команд и JS/TS
- **Ограничения**: нет PTY-терминала, LSP и форматтеров, локальных MCP-процессов; нативный file watcher недоступен (поиск через `rg`)
- **Лицензия**: MIT, как у оригинала

[![OpenCode Terminal UI](packages/web/src/assets/lander/screenshot.png)](https://opencode.ai)

> [!IMPORTANT]
> Тег релиза `v0.1.1` указывает на старый коммит upstream без Android-модуля. Исходники Android находятся в ветке `main`; для [сборки](docs/android/BUILD.md) используйте её. Точное соответствие опубликованного APK содержимому `main` не подтверждено.

---

### Установка

```bash
# YOLO
curl -fsSL https://opencode.ai/install | bash

# Менеджеры пакетов
npm i -g opencode-ai@latest        # или bun/pnpm/yarn
scoop install opencode             # Windows
choco install opencode             # Windows
brew install anomalyco/tap/opencode # macOS и Linux (рекомендуем, всегда актуально)
brew install opencode              # macOS и Linux (официальная формула brew, обновляется реже)
sudo pacman -S opencode            # Arch Linux (Stable)
paru -S opencode-bin               # Arch Linux (Latest from AUR)
mise use -g opencode               # любая ОС
nix run nixpkgs#opencode           # или github:anomalyco/opencode для самой свежей ветки dev
```

> [!TIP]
> Перед установкой удалите версии старше 0.1.x.

### Десктопное приложение (BETA)

OpenCode также доступен как десктопное приложение. Скачайте его со [страницы релизов](https://github.com/anomalyco/opencode/releases) или с [opencode.ai/download](https://opencode.ai/download).

| Платформа             | Загрузка                           |
| --------------------- | ---------------------------------- |
| macOS (Apple Silicon) | `opencode-desktop-mac-arm64.dmg`   |
| macOS (Intel)         | `opencode-desktop-mac-x64.dmg`     |
| Windows               | `opencode-desktop-windows-x64.exe` |
| Linux                 | `.deb`, `.rpm` или AppImage        |

### Мобильное приложение (BETA)

OpenCode также работает на Android: нативная APK собирается из этого форка. Приложение содержит настоящий сервер и тот же веб-интерфейс; при разрешённом доступе ко всем файлам данные сервера хранятся в `/sdcard/OpenCode/home` и сохраняются после удаления приложения. Без разрешения они удаляются при деинсталляции.

| Платформа | Загрузка | Примечания |
| --- | --- | --- |
| Android 8.0+ (arm64) | [`app-release.apk`](https://github.com/hi77x/opencode-android/releases/latest) | BETA — разрешите доступ ко всем файлам для сохранения данных |
| Сборка из исходников | `./script/android/build-apk.sh` | см. [`packages/android/README.md`](packages/android/README.md) |

```bash
# macOS (Homebrew)
brew install --cask opencode-desktop
# Windows (Scoop)
scoop bucket add extras; scoop install extras/opencode-desktop
```

#### Каталог установки

Скрипт установки выбирает путь установки в следующем порядке приоритета:

1. `$OPENCODE_INSTALL_DIR` - Пользовательский каталог установки
2. `$XDG_BIN_DIR` - Путь, совместимый со спецификацией XDG Base Directory
3. `$HOME/bin` - Стандартный каталог пользовательских бинарников (если существует или можно создать)
4. `$HOME/.opencode/bin` - Fallback по умолчанию

```bash
# Примеры
OPENCODE_INSTALL_DIR=/usr/local/bin curl -fsSL https://opencode.ai/install | bash
XDG_BIN_DIR=$HOME/.local/bin curl -fsSL https://opencode.ai/install | bash
```

### Agents

В OpenCode есть два встроенных агента, между которыми можно переключаться клавишей `Tab`.

- **build** - По умолчанию, агент с полным доступом для разработки
- **plan** - Агент только для чтения для анализа и изучения кода
  - По умолчанию запрещает редактирование файлов
  - Запрашивает разрешение перед выполнением bash-команд
  - Идеален для изучения незнакомых кодовых баз или планирования изменений

Также включен сабагент **general** для сложных поисков и многошаговых задач.
Он используется внутренне и может быть вызван в сообщениях через `@general`.

Подробнее об [agents](https://opencode.ai/docs/agents).

### Документация

Больше информации о том, как настроить OpenCode: [**наши docs**](https://opencode.ai/docs).

### Вклад

Если вы хотите внести вклад в OpenCode, прочитайте [contributing docs](./CONTRIBUTING.md) перед тем, как отправлять pull request.

### Разработка на базе OpenCode

Если вы делаете проект, связанный с OpenCode, и используете "opencode" как часть имени (например, "opencode-dashboard" или "opencode-mobile"), добавьте примечание в README, чтобы уточнить, что проект не создан командой OpenCode и не аффилирован с нами.

---

**Присоединяйтесь к нашему сообществу** [Discord](https://discord.gg/opencode) | [X.com](https://x.com/opencode)
