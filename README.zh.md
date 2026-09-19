<p align="center">
  <a href="https://opencode.ai">
    <picture>
      <source srcset="packages/console/app/src/asset/logo-ornate-dark.svg" media="(prefers-color-scheme: dark)">
      <source srcset="packages/console/app/src/asset/logo-ornate-light.svg" media="(prefers-color-scheme: light)">
      <img src="packages/console/app/src/asset/logo-ornate-light.svg" alt="OpenCode logo">
    </picture>
  </a>
</p>
<p align="center">开源的 AI Coding Agent。</p>
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

> [!IMPORTANT]
> Android data survives reinstall only if all-files access is granted: server data is stored under `/sdcard/OpenCode/home`; otherwise app-private data is removed on uninstall. You may need to reselect a project after reinstalling. The `v0.1.1` source tag predates the Android module; build from `main`. The uploaded APK has not been verified as a byte-for-byte build of `main`. See the [current Android notes](README.md#opencode-for-android).

本分支让现有的 opencode 在 Android 上原生运行：手机本地运行真正的服务器、Agent、会话以及**未经修改**的 `packages/app` Web 界面（通过 WebView 里的 loopback 访问）。无需电脑、Termux、远程服务器，也不改动协议。

我们在上游之上新增的内容（其余都是上游 opencode）：

| 路径 | 内容 |
| --- | --- |
| [`packages/android`](packages/android) | Android 宿主：Gradle 工程、WebView Activity、内嵌服务器、移动端界面适配 |
| [`script/android`](script/android) | 可复现构建：[`build-apk.sh`](script/android/build-apk.sh) 以及服务器、ripgrep、git 运行时和翻译脚本 |
| [`docs/android`](docs/android) | 工程文档：[构建](docs/android/BUILD.md)、[执行策略](docs/android/EXECUTION_POLICY.md)、[原生依赖](docs/android/NATIVE_DEPENDENCIES.md)、[上游补丁](docs/android/UPSTREAM_PATCHES.md) |

只修改了五个上游文件（Android 构建目标和运行时兼容性）；每个改动及原因都记录在 [`docs/android/UPSTREAM_PATCHES.md`](docs/android/UPSTREAM_PATCHES.md)。

工作原理：Activity 启动内嵌服务器（`libopencode.so serve --hostname=127.0.0.1`），并在 WebView 中加载 `http://127.0.0.1:<port>/`。工具从应用的 nativeLibraryDir 运行：`/system/bin/sh`、内置 `git`、`rg`，以及以 `bun`/`node` 暴露的 Bun 运行时。

- **下载**：[releases](https://github.com/hi77x/opencode-android/releases)（`app-release.apk`，arm64，Android 8.0+）
- **构建**：`./script/android/build-apk.sh` — 参见 [`docs/android/BUILD.md`](docs/android/BUILD.md)
- **首次启动**：授予文件访问权限（会话保存在 `/sdcard/OpenCode/home`，重装后仍保留），然后在 设置 → 提供商 中连接提供商并添加项目（`~/workspace`）
- **可用**：服务器与 Web 界面、会话、带语法高亮差异的 git（Changes）、项目文件浏览器（Files）、上下文用量面板（Usage）、命令与 JS/TS 执行
- **限制**：没有 PTY 终端、LSP/格式化工具和本地 MCP 进程；原生 file watcher 不可用（搜索使用 `rg`）
- **许可证**：MIT，与原项目相同

[![OpenCode Terminal UI](packages/web/src/assets/lander/screenshot.png)](https://opencode.ai)

---

### 安装

```bash
# 直接安装 (YOLO)
curl -fsSL https://opencode.ai/install | bash

# 软件包管理器
npm i -g opencode-ai@latest        # 也可使用 bun/pnpm/yarn
scoop install opencode             # Windows
choco install opencode             # Windows
brew install anomalyco/tap/opencode # macOS 和 Linux（推荐，始终保持最新）
brew install opencode              # macOS 和 Linux（官方 brew formula，更新频率较低）
sudo pacman -S opencode            # Arch Linux (Stable)
paru -S opencode-bin               # Arch Linux (Latest from AUR)
mise use -g opencode               # 任意系统
nix run nixpkgs#opencode           # 或用 github:anomalyco/opencode 获取最新 dev 分支
```

> [!TIP]
> 安装前请先移除 0.1.x 之前的旧版本。

### 桌面应用程序 (BETA)

OpenCode 也提供桌面版应用。可直接从 [发布页 (releases page)](https://github.com/anomalyco/opencode/releases) 或 [opencode.ai/download](https://opencode.ai/download) 下载。

| 平台                  | 下载文件                           |
| --------------------- | ---------------------------------- |
| macOS (Apple Silicon) | `opencode-desktop-mac-arm64.dmg`   |
| macOS (Intel)         | `opencode-desktop-mac-x64.dmg`     |
| Windows               | `opencode-desktop-windows-x64.exe` |
| Linux                 | `.deb`、`.rpm` 或 AppImage         |

### 移动应用（BETA）

OpenCode 也能在 Android 上以本分支构建的原生 APK 运行。应用内含真正的服务器和同一个 Web 界面；会话、提供商密钥和项目保存在 `/sdcard/OpenCode/home`，重装后仍然保留。

| 平台 | 下载 | 说明 |
| --- | --- | --- |
| Android 8.0+ (arm64) | [`app-release.apk`](https://github.com/hi77x/opencode-android/releases/latest) | BETA — 首次启动时授予文件访问权限 |
| 从源码构建 | `./script/android/build-apk.sh` | 参见 [`packages/android/README.md`](packages/android/README.md) |

```bash
# macOS (Homebrew Cask)
brew install --cask opencode-desktop
# Windows (Scoop)
scoop bucket add extras; scoop install extras/opencode-desktop
```

#### 安装目录

安装脚本按照以下优先级决定安装路径：

1. `$OPENCODE_INSTALL_DIR` - 自定义安装目录
2. `$XDG_BIN_DIR` - 符合 XDG 基础目录规范的路径
3. `$HOME/bin` - 如果存在或可创建的用户二进制目录
4. `$HOME/.opencode/bin` - 默认备用路径

```bash
# 示例
OPENCODE_INSTALL_DIR=/usr/local/bin curl -fsSL https://opencode.ai/install | bash
XDG_BIN_DIR=$HOME/.local/bin curl -fsSL https://opencode.ai/install | bash
```

### Agents

OpenCode 内置两种 Agent，可用 `Tab` 键快速切换：

- **build** - 默认模式，具备完整权限，适合开发工作
- **plan** - 只读模式，适合代码分析与探索
  - 默认拒绝修改文件
  - 运行 bash 命令前会询问
  - 便于探索未知代码库或规划改动

另外还包含一个 **general** 子 Agent，用于复杂搜索和多步任务，内部使用，也可在消息中输入 `@general` 调用。

了解更多 [Agents](https://opencode.ai/docs/agents) 相关信息。

### 文档

更多配置说明请查看我们的 [**官方文档**](https://opencode.ai/docs)。

### 参与贡献

如有兴趣贡献代码，请在提交 PR 前阅读 [贡献指南 (Contributing Docs)](./CONTRIBUTING.md)。

### 基于 OpenCode 进行开发

如果你在项目名中使用了 “opencode”（如 “opencode-dashboard” 或 “opencode-mobile”），请在 README 里注明该项目不是 OpenCode 团队官方开发，且不存在隶属关系。

---

**加入我们的社区** [飞书](https://applink.feishu.cn/client/chat/chatter/add_by_link?link_token=52ao9352-5623-4fa0-b7dd-3407c392c1af&qr_code=true) | [X.com](https://x.com/opencode)
