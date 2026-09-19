<p align="center">
  <a href="https://opencode.ai">
    <picture>
      <source srcset="packages/console/app/src/asset/logo-ornate-dark.svg" media="(prefers-color-scheme: dark)">
      <source srcset="packages/console/app/src/asset/logo-ornate-light.svg" media="(prefers-color-scheme: light)">
      <img src="packages/console/app/src/asset/logo-ornate-light.svg" alt="OpenCode logo">
    </picture>
  </a>
</p>
<p align="center">開源的 AI Coding Agent。</p>
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

## opencode Android 版

此分支讓現有的 opencode 在 Android 上原生執行：手機本機執行真正的伺服器、Agent、工作階段，以及**未經修改**的 `packages/app` 網頁介面（透過 WebView 中的 loopback）。無需電腦、Termux、遠端伺服器，也不更動通訊協定。

我們在上游之上新增的內容（其餘皆為上游 opencode）：

| 路徑 | 內容 |
| --- | --- |
| [`packages/android`](packages/android) | Android 宿主：Gradle 專案、WebView Activity、內嵌伺服器、行動介面調適 |
| [`script/android`](script/android) | 可重現建置：[`build-apk.sh`](script/android/build-apk.sh)，以及伺服器、ripgrep、git 執行環境與翻譯腳本 |
| [`docs/android`](docs/android) | 工程文件：[建置](docs/android/BUILD.md)、[執行政策](docs/android/EXECUTION_POLICY.md)、[原生相依](docs/android/NATIVE_DEPENDENCIES.md)、[上游修補](docs/android/UPSTREAM_PATCHES.md) |

僅修改五個上游檔案（Android 建置目標與執行環境相容性）；每個變更與原因都記錄在 [`docs/android/UPSTREAM_PATCHES.md`](docs/android/UPSTREAM_PATCHES.md)。

運作方式：Activity 啟動內嵌伺服器（`libopencode.so serve --hostname=127.0.0.1`），並在 WebView 載入 `http://127.0.0.1:4096/`。工具從應用程式的 nativeLibraryDir 執行：`/system/bin/sh`、內附 `git`、`rg`，以及以 `bun`/`node` 提供的 Bun 執行環境。

- **下載**：[releases](https://github.com/hi77x/opencode-android/releases)（`app-release.apk`，arm64，Android 8.0+）
- **建置**：`./script/android/build-apk.sh` — 參見 [`docs/android/BUILD.md`](docs/android/BUILD.md)
- **首次啟動**：授予檔案存取權（工作階段儲存在 `/sdcard/OpenCode`，重新安裝後仍保留），接著在 設定 → 供應商 中連接供應商並新增專案（`~/workspace`）
- **可用**：伺服器與網頁介面、工作階段、含語法高亮差異的 git（Changes）、專案檔案瀏覽器（Files）、上下文用量面板（Usage）、指令與 JS/TS 執行
- **限制**：沒有 PTY 終端、LSP/格式化工具與本機 MCP 程序；原生 file watcher 無法使用（搜尋使用 `rg`）
- **授權**：MIT，與原專案相同

[![OpenCode Terminal UI](packages/web/src/assets/lander/screenshot.png)](https://opencode.ai)

---

### 安裝

```bash
# 直接安裝 (YOLO)
curl -fsSL https://opencode.ai/install | bash

# 套件管理員
npm i -g opencode-ai@latest        # 也可使用 bun/pnpm/yarn
scoop install opencode             # Windows
choco install opencode             # Windows
brew install anomalyco/tap/opencode # macOS 與 Linux（推薦，始終保持最新）
brew install opencode              # macOS 與 Linux（官方 brew formula，更新頻率較低）
sudo pacman -S opencode            # Arch Linux (Stable)
paru -S opencode-bin               # Arch Linux (Latest from AUR)
mise use -g opencode               # 任何作業系統
nix run nixpkgs#opencode           # 或使用 github:anomalyco/opencode 以取得最新開發分支
```

> [!TIP]
> 安裝前請先移除 0.1.x 以前的舊版本。

### 桌面應用程式 (BETA)

OpenCode 也提供桌面版應用程式。您可以直接從 [發佈頁面 (releases page)](https://github.com/anomalyco/opencode/releases) 或 [opencode.ai/download](https://opencode.ai/download) 下載。

| 平台                  | 下載連結                           |
| --------------------- | ---------------------------------- |
| macOS (Apple Silicon) | `opencode-desktop-mac-arm64.dmg`   |
| macOS (Intel)         | `opencode-desktop-mac-x64.dmg`     |
| Windows               | `opencode-desktop-windows-x64.exe` |
| Linux                 | `.deb`, `.rpm`, 或 AppImage        |

### 行動應用（BETA）

OpenCode 也能在 Android 上以本分支建置的原生 APK 執行。應用程式內含真正的伺服器與同一個網頁介面；工作階段、供應商金鑰與專案儲存在 `/sdcard/OpenCode`，重新安裝後仍保留。

| 平台 | 下載 | 說明 |
| --- | --- | --- |
| Android 8.0+ (arm64) | [`app-release.apk`](https://github.com/hi77x/opencode-android/releases/latest) | BETA — 首次啟動時授予檔案存取權 |
| 從原始碼建置 | `./script/android/build-apk.sh` | 參見 [`packages/android/README.md`](packages/android/README.md) |

```bash
# macOS (Homebrew Cask)
brew install --cask opencode-desktop
# Windows (Scoop)
scoop bucket add extras; scoop install extras/opencode-desktop
```

#### 安裝目錄

安裝腳本會依據以下優先順序決定安裝路徑：

1. `$OPENCODE_INSTALL_DIR` - 自定義安裝目錄
2. `$XDG_BIN_DIR` - 符合 XDG 基礎目錄規範的路徑
3. `$HOME/bin` - 標準使用者執行檔目錄 (若存在或可建立)
4. `$HOME/.opencode/bin` - 預設備用路徑

```bash
# 範例
OPENCODE_INSTALL_DIR=/usr/local/bin curl -fsSL https://opencode.ai/install | bash
XDG_BIN_DIR=$HOME/.local/bin curl -fsSL https://opencode.ai/install | bash
```

### Agents

OpenCode 內建了兩種 Agent，您可以使用 `Tab` 鍵快速切換。

- **build** - 預設模式，具備完整權限的 Agent，適用於開發工作。
- **plan** - 唯讀模式，適用於程式碼分析與探索。
  - 預設禁止修改檔案。
  - 執行 bash 指令前會詢問權限。
  - 非常適合用來探索陌生的程式碼庫或規劃變更。

此外，OpenCode 還包含一個 **general** 子 Agent，用於處理複雜搜尋與多步驟任務。此 Agent 供系統內部使用，亦可透過在訊息中輸入 `@general` 來呼叫。

了解更多關於 [Agents](https://opencode.ai/docs/agents) 的資訊。

### 線上文件

關於如何設定 OpenCode 的詳細資訊，請參閱我們的 [**官方文件**](https://opencode.ai/docs)。

### 參與貢獻

如果您有興趣參與 OpenCode 的開發，請在提交 Pull Request 前先閱讀我們的 [貢獻指南 (Contributing Docs)](./CONTRIBUTING.md)。

### 基於 OpenCode 進行開發

如果您正在開發與 OpenCode 相關的專案，並在名稱中使用了 "opencode"（例如 "opencode-dashboard" 或 "opencode-mobile"），請在您的 README 中加入聲明，說明該專案並非由 OpenCode 團隊開發，且與我們沒有任何隸屬關係。

---

**加入我們的社群** [飞书](https://applink.feishu.cn/client/chat/chatter/add_by_link?link_token=52ao9352-5623-4fa0-b7dd-3407c392c1af&qr_code=true) | [X.com](https://x.com/opencode)
