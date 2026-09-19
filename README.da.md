<p align="center">
  <a href="https://opencode.ai">
    <picture>
      <source srcset="packages/console/app/src/asset/logo-ornate-dark.svg" media="(prefers-color-scheme: dark)">
      <source srcset="packages/console/app/src/asset/logo-ornate-light.svg" media="(prefers-color-scheme: light)">
      <img src="packages/console/app/src/asset/logo-ornate-light.svg" alt="OpenCode logo">
    </picture>
  </a>
</p>
<p align="center">Den open source AI-kodeagent.</p>
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

## opencode til Android

Dette fork kører det eksisterende opencode nativt på Android: telefonen kører den rigtige server, agenten, sessionerne og den **uændrede** `packages/app`-webflade (via loopback i en WebView). Ingen PC, ingen Termux, ingen ekstern server og ingen protokolændringer.

Det vi selv har tilføjet (alt andet er upstream opencode):

| Sti | Indhold |
| --- | --- |
| [`packages/android`](packages/android) | Android-host: Gradle-projekt, WebView-activity, indlejret server, mobile UI-tilpasninger |
| [`script/android`](script/android) | Reproducerbart build: [`build-apk.sh`](script/android/build-apk.sh) samt hjælpere til server, ripgrep, git-runtime og oversættelser |
| [`docs/android`](docs/android) | Teknisk dokumentation: [build](docs/android/BUILD.md), [execution policy](docs/android/EXECUTION_POLICY.md), [native afhængigheder](docs/android/NATIVE_DEPENDENCIES.md), [upstream-patches](docs/android/UPSTREAM_PATCHES.md) |

Kun fem upstream-filer ændres (Android-buildtarget og runtime-kompatibilitet); hver ændring er begrundet i [`docs/android/UPSTREAM_PATCHES.md`](docs/android/UPSTREAM_PATCHES.md).

Sådan virker det: activity'en starter den indlejrede server (`libopencode.so serve --hostname=127.0.0.1`) og indlæser `http://127.0.0.1:4096/` i en WebView. Værktøjer kører fra appens native biblioteksmappe: `/system/bin/sh`, medfølgende `git`, `rg` og en Bun-runtime som `bun`/`node`.

- **Download**: [releases](https://github.com/hi77x/opencode-android/releases) (`app-release.apk`, arm64, Android 8.0+)
- **Build**: `./script/android/build-apk.sh` — se [`docs/android/BUILD.md`](docs/android/BUILD.md)
- **Første start**: giv filadgang (sessioner ligger i `/sdcard/OpenCode` og overlever geninstallation), gå derefter til Indstillinger → Udbyder → forbind en udbyder og tilføj et projekt (`~/workspace`)
- **Virker**: server og webflade, sessioner, git med diffs og syntaksfremhævning (Changes), projektfilbrowser (Files), kontekstforbrugspanel (Usage), kommando- og JS/TS-kørsel
- **Begrænsninger**: ingen PTY-terminal, LSP/formattere eller lokale MCP-processer; den native file watcher mangler (søgning bruger `rg`)
- **Licens**: MIT, som originalen

[![OpenCode Terminal UI](packages/web/src/assets/lander/screenshot.png)](https://opencode.ai)

---

### Installation

```bash
# YOLO
curl -fsSL https://opencode.ai/install | bash

# Pakkehåndteringer
npm i -g opencode-ai@latest        # eller bun/pnpm/yarn
scoop install opencode             # Windows
choco install opencode             # Windows
brew install anomalyco/tap/opencode # macOS og Linux (anbefalet, altid up to date)
brew install opencode              # macOS og Linux (officiel brew formula, opdateres sjældnere)
sudo pacman -S opencode            # Arch Linux (Stable)
paru -S opencode-bin               # Arch Linux (Latest from AUR)
mise use -g opencode               # alle OS
nix run nixpkgs#opencode           # eller github:anomalyco/opencode for nyeste dev-branch
```

> [!TIP]
> Fjern versioner ældre end 0.1.x før installation.

### Desktop-app (BETA)

OpenCode findes også som desktop-app. Download direkte fra [releases-siden](https://github.com/anomalyco/opencode/releases) eller [opencode.ai/download](https://opencode.ai/download).

| Platform              | Download                           |
| --------------------- | ---------------------------------- |
| macOS (Apple Silicon) | `opencode-desktop-mac-arm64.dmg`   |
| macOS (Intel)         | `opencode-desktop-mac-x64.dmg`     |
| Windows               | `opencode-desktop-windows-x64.exe` |
| Linux                 | `.deb`, `.rpm`, eller AppImage     |

### Mobilapp (BETA)

OpenCode kører også på Android som en native APK bygget fra dette fork. Appen indeholder den rigtige server og samme webflade; sessioner, udbydernøgler og projekter ligger i `/sdcard/OpenCode` og overlever geninstallation.

| Platform | Download | Bemærkninger |
| --- | --- | --- |
| Android 8.0+ (arm64) | [`app-release.apk`](https://github.com/hi77x/opencode-android/releases/latest) | BETA — giv filadgang ved første start |
| Byg fra kildekode | `./script/android/build-apk.sh` | se [`packages/android/README.md`](packages/android/README.md) |

```bash
# macOS (Homebrew)
brew install --cask opencode-desktop
# Windows (Scoop)
scoop bucket add extras; scoop install extras/opencode-desktop
```

#### Installationsmappe

Installationsscriptet bruger følgende prioriteringsrækkefølge for installationsstien:

1. `$OPENCODE_INSTALL_DIR` - Tilpasset installationsmappe
2. `$XDG_BIN_DIR` - Sti der følger XDG Base Directory Specification
3. `$HOME/bin` - Standard bruger-bin-mappe (hvis den findes eller kan oprettes)
4. `$HOME/.opencode/bin` - Standard fallback

```bash
# Eksempler
OPENCODE_INSTALL_DIR=/usr/local/bin curl -fsSL https://opencode.ai/install | bash
XDG_BIN_DIR=$HOME/.local/bin curl -fsSL https://opencode.ai/install | bash
```

### Agents

OpenCode har to indbyggede agents, som du kan skifte mellem med `Tab`-tasten.

- **build** - Standard, agent med fuld adgang til udviklingsarbejde
- **plan** - Skrivebeskyttet agent til analyse og kodeudforskning
  - Afviser filredigering som standard
  - Spørger om tilladelse før bash-kommandoer
  - Ideel til at udforske ukendte kodebaser eller planlægge ændringer

Derudover findes der en **general**-subagent til komplekse søgninger og flertrinsopgaver.
Den bruges internt og kan kaldes via `@general` i beskeder.

Læs mere om [agents](https://opencode.ai/docs/agents).

### Dokumentation

For mere info om konfiguration af OpenCode, [**se vores docs**](https://opencode.ai/docs).

### Bidrag

Hvis du vil bidrage til OpenCode, så læs vores [contributing docs](./CONTRIBUTING.md) før du sender en pull request.

### Bygget på OpenCode

Hvis du arbejder på et projekt der er relateret til OpenCode og bruger "opencode" som en del af navnet; f.eks. "opencode-dashboard" eller "opencode-mobile", så tilføj en note i din README, der tydeliggør at projektet ikke er bygget af OpenCode-teamet og ikke er tilknyttet os på nogen måde.

---

**Bliv en del af vores community** [Discord](https://discord.gg/opencode) | [X.com](https://x.com/opencode)
