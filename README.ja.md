<p align="center">
  <a href="https://opencode.ai">
    <picture>
      <source srcset="packages/console/app/src/asset/logo-ornate-dark.svg" media="(prefers-color-scheme: dark)">
      <source srcset="packages/console/app/src/asset/logo-ornate-light.svg" media="(prefers-color-scheme: light)">
      <img src="packages/console/app/src/asset/logo-ornate-light.svg" alt="OpenCode logo">
    </picture>
  </a>
</p>
<p align="center">オープンソースのAIコーディングエージェント。</p>
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

## Android 版 opencode

> [!IMPORTANT]
> Android data survives reinstall only if all-files access is granted: server data is stored under `/sdcard/OpenCode/home`; otherwise app-private data is removed on uninstall. You may need to reselect a project after reinstalling. The `v0.1.1` source tag predates the Android module; build from `main`. The uploaded APK has not been verified as a byte-for-byte build of `main`. See the [current Android notes](README.md#opencode-for-android).

このフォークは既存の opencode を Android 上でネイティブに動かします。スマートフォン上で本物のサーバー、エージェント、セッション、そして**変更していない** `packages/app` の Web UI（WebView 内の loopback 経由）が動作します。PC も Termux もリモートサーバーも不要で、プロトコルの変更もありません。

私たちが upstream に追加したもの（それ以外はすべて upstream の opencode です）:

| パス | 内容 |
| --- | --- |
| [`packages/android`](packages/android) | Android ホスト: Gradle プロジェクト、WebView Activity、内蔵サーバー、モバイル UI 調整 |
| [`script/android`](script/android) | 再現可能なビルド: [`build-apk.sh`](script/android/build-apk.sh) と、サーバー、ripgrep、git ランタイム、翻訳のスクリプト |
| [`docs/android`](docs/android) | 技術ドキュメント: [ビルド](docs/android/BUILD.md)、[実行ポリシー](docs/android/EXECUTION_POLICY.md)、[ネイティブ依存](docs/android/NATIVE_DEPENDENCIES.md)、[upstream パッチ](docs/android/UPSTREAM_PATCHES.md) |

変更する upstream ファイルは 5 つだけです（Android ビルドターゲットとランタイム互換性）。それぞれ理由を [`docs/android/UPSTREAM_PATCHES.md`](docs/android/UPSTREAM_PATCHES.md) に記載しています。

仕組み: Activity が内蔵サーバーを起動し（`libopencode.so serve --hostname=127.0.0.1`）、WebView で `http://127.0.0.1:<port>/` を読み込みます。ツールはアプリの nativeLibraryDir から実行されます: `/system/bin/sh`、同梱の `git`、`rg`、そして `bun`/`node` として公開される Bun ランタイム。

- **ダウンロード**: [releases](https://github.com/hi77x/opencode-android/releases)（`app-release.apk`、arm64、Android 8.0+）
- **ビルド**: `./script/android/build-apk.sh` — [`docs/android/BUILD.md`](docs/android/BUILD.md) を参照
- **初回起動**: ファイルアクセスを許可（セッションは `/sdcard/OpenCode/home` に保存され、再インストール後も残ります）。次に 設定 → プロバイダー でプロバイダーを接続し、プロジェクト（`~/workspace`）を追加します
- **動作する**: サーバーと Web UI、セッション、差分とシンタックスハイライト付きの git（Changes）、プロジェクトファイルブラウザ（Files）、コンテキスト使用量パネル（Usage）、コマンドと JS/TS の実行
- **制限**: PTY ターミナル、LSP／フォーマッター、ローカル MCP プロセスはなし。ネイティブ file watcher は利用不可（検索は `rg` を使用）
- **ライセンス**: MIT（オリジナルと同じ）

[![OpenCode Terminal UI](packages/web/src/assets/lander/screenshot.png)](https://opencode.ai)

---

### インストール

```bash
# YOLO
curl -fsSL https://opencode.ai/install | bash

# パッケージマネージャー
npm i -g opencode-ai@latest        # bun/pnpm/yarn でもOK
scoop install opencode             # Windows
choco install opencode             # Windows
brew install anomalyco/tap/opencode # macOS と Linux（推奨。常に最新）
brew install opencode              # macOS と Linux（公式 brew formula。更新頻度は低め）
sudo pacman -S opencode            # Arch Linux (Stable)
paru -S opencode-bin               # Arch Linux (Latest from AUR)
mise use -g opencode               # どのOSでも
nix run nixpkgs#opencode           # または github:anomalyco/opencode で最新 dev ブランチ
```

> [!TIP]
> インストール前に 0.1.x より古いバージョンを削除してください。

### デスクトップアプリ (BETA)

OpenCode はデスクトップアプリとしても利用できます。[releases page](https://github.com/anomalyco/opencode/releases) から直接ダウンロードするか、[opencode.ai/download](https://opencode.ai/download) を利用してください。

| プラットフォーム      | ダウンロード                       |
| --------------------- | ---------------------------------- |
| macOS (Apple Silicon) | `opencode-desktop-mac-arm64.dmg`   |
| macOS (Intel)         | `opencode-desktop-mac-x64.dmg`     |
| Windows               | `opencode-desktop-windows-x64.exe` |
| Linux                 | `.deb`、`.rpm`、または AppImage    |

### モバイルアプリ (BETA)

OpenCode はこのフォークからビルドしたネイティブ APK として Android でも動作します。本物のサーバーと同じ Web UI を同梱し、セッション、プロバイダーキー、プロジェクトは `/sdcard/OpenCode/home` に保存されて再インストール後も残ります。

| プラットフォーム | ダウンロード | 備考 |
| --- | --- | --- |
| Android 8.0+ (arm64) | [`app-release.apk`](https://github.com/hi77x/opencode-android/releases/latest) | BETA — 初回起動時にファイルアクセスを許可 |
| ソースからビルド | `./script/android/build-apk.sh` | [`packages/android/README.md`](packages/android/README.md) を参照 |

```bash
# macOS (Homebrew)
brew install --cask opencode-desktop
# Windows (Scoop)
scoop bucket add extras; scoop install extras/opencode-desktop
```

#### インストールディレクトリ

インストールスクリプトは、インストール先パスを次の優先順位で決定します。

1. `$OPENCODE_INSTALL_DIR` - カスタムのインストールディレクトリ
2. `$XDG_BIN_DIR` - XDG Base Directory Specification に準拠したパス
3. `$HOME/bin` - 標準のユーザー用バイナリディレクトリ（存在する場合、または作成できる場合）
4. `$HOME/.opencode/bin` - デフォルトのフォールバック

```bash
# 例
OPENCODE_INSTALL_DIR=/usr/local/bin curl -fsSL https://opencode.ai/install | bash
XDG_BIN_DIR=$HOME/.local/bin curl -fsSL https://opencode.ai/install | bash
```

### Agents

OpenCode には組み込みの Agent が2つあり、`Tab` キーで切り替えられます。

- **build** - デフォルト。開発向けのフルアクセス Agent
- **plan** - 分析とコード探索向けの読み取り専用 Agent
  - デフォルトでファイル編集を拒否
  - bash コマンド実行前に確認
  - 未知のコードベース探索や変更計画に最適

また、複雑な検索やマルチステップのタスク向けに **general** サブ Agent も含まれています。
内部的に使用されており、メッセージで `@general` と入力して呼び出せます。

[agents](https://opencode.ai/docs/agents) の詳細はこちら。

### ドキュメント

OpenCode の設定については [**ドキュメント**](https://opencode.ai/docs) を参照してください。

### コントリビュート

OpenCode に貢献したい場合は、Pull Request を送る前に [contributing docs](./CONTRIBUTING.md) を読んでください。

### OpenCode の上に構築する

OpenCode に関連するプロジェクトで、名前に "opencode"（例: "opencode-dashboard" や "opencode-mobile"）を含める場合は、そのプロジェクトが OpenCode チームによって作られたものではなく、いかなる形でも関係がないことを README に明記してください。

---

**コミュニティに参加** [Discord](https://discord.gg/opencode) | [X.com](https://x.com/opencode)
