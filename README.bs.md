<p align="center">
  <a href="https://opencode.ai">
    <picture>
      <source srcset="packages/console/app/src/asset/logo-ornate-dark.svg" media="(prefers-color-scheme: dark)">
      <source srcset="packages/console/app/src/asset/logo-ornate-light.svg" media="(prefers-color-scheme: light)">
      <img src="packages/console/app/src/asset/logo-ornate-light.svg" alt="OpenCode logo">
    </picture>
  </a>
</p>
<p align="center">OpenCode je open source AI agent za programiranje.</p>
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

## opencode za Android

Ovaj fork pokreće postojeći opencode nativno na Androidu: telefon izvršava pravi server, agenta, sesije i **nepromijenjeni** `packages/app` web interfejs (preko loopback-a u WebView-u). Bez PC-a, bez Termux-a, bez udaljenog servera i bez promjena protokola.

Šta smo mi dodali (sve ostalo je upstream opencode):

| Putanja | Sadržaj |
| --- | --- |
| [`packages/android`](packages/android) | Android host: Gradle projekat, WebView activity, ugrađeni server, prilagođavanje mobilnog UI-a |
| [`script/android`](script/android) | Ponovljiv build: [`build-apk.sh`](script/android/build-apk.sh) i skripte za server, ripgrep, git runtime i prijevode |
| [`docs/android`](docs/android) | Tehnička dokumentacija: [build](docs/android/BUILD.md), [pravila izvršavanja](docs/android/EXECUTION_POLICY.md), [nativne zavisnosti](docs/android/NATIVE_DEPENDENCIES.md), [upstream zakrpe](docs/android/UPSTREAM_PATCHES.md) |

Mijenja se samo pet upstream datoteka (Android build cilj i kompatibilnost runtime-a); svaka je dokumentovana s razlogom u [`docs/android/UPSTREAM_PATCHES.md`](docs/android/UPSTREAM_PATCHES.md).

Kako radi: activity pokreće ugrađeni server (`libopencode.so serve --hostname=127.0.0.1`) i učitava `http://127.0.0.1:4096/` u WebView. Alati se izvršavaju iz nativeLibraryDir-a aplikacije: `/system/bin/sh`, ugrađeni `git`, `rg` i Bun runtime izložen kao `bun`/`node`.

- **Preuzmi**: [releases](https://github.com/hi77x/opencode-android/releases) (`app-release.apk`, arm64, Android 8.0+)
- **Build**: `./script/android/build-apk.sh` — vidi [`docs/android/BUILD.md`](docs/android/BUILD.md)
- **Prvo pokretanje**: odobrite pristup datotekama (sesije su u `/sdcard/OpenCode` i preživljavaju reinstalaciju), zatim Postavke → Provajderi → povežite provajdera i dodajte projekat (`~/workspace`)
- **Radi**: server i web UI, sesije, git s diffovima i isticanjem sintakse (Changes), preglednik datoteka projekta (Files), panel potrošnje konteksta (Usage), izvršavanje komandi i JS/TS
- **Ograničenja**: nema PTY terminala, LSP/formatera ni lokalnih MCP procesa; nativni file watcher nije dostupan (pretraga koristi `rg`)
- **Licenca**: MIT, kao i original

[![OpenCode Terminal UI](packages/web/src/assets/lander/screenshot.png)](https://opencode.ai)

---

### Instalacija

```bash
# YOLO
curl -fsSL https://opencode.ai/install | bash

# Package manageri
npm i -g opencode-ai@latest        # ili bun/pnpm/yarn
scoop install opencode             # Windows
choco install opencode             # Windows
brew install anomalyco/tap/opencode # macOS i Linux (preporučeno, uvijek ažurno)
brew install opencode              # macOS i Linux (zvanična brew formula, rjeđe se ažurira)
sudo pacman -S opencode            # Arch Linux (Stable)
paru -S opencode-bin               # Arch Linux (Latest from AUR)
mise use -g opencode               # Bilo koji OS
nix run nixpkgs#opencode           # ili github:anomalyco/opencode za najnoviji dev branch
```

> [!TIP]
> Ukloni verzije starije od 0.1.x prije instalacije.

### Desktop aplikacija (BETA)

OpenCode je dostupan i kao desktop aplikacija. Preuzmi je direktno sa [stranice izdanja](https://github.com/anomalyco/opencode/releases) ili sa [opencode.ai/download](https://opencode.ai/download).

| Platforma             | Preuzimanje                        |
| --------------------- | ---------------------------------- |
| macOS (Apple Silicon) | `opencode-desktop-mac-arm64.dmg`   |
| macOS (Intel)         | `opencode-desktop-mac-x64.dmg`     |
| Windows               | `opencode-desktop-windows-x64.exe` |
| Linux                 | `.deb`, `.rpm`, ili AppImage       |

### Mobilna aplikacija (BETA)

OpenCode radi i na Androidu kao nativni APK izgrađen iz ovog forka. Aplikacija sadrži pravi server i isti web UI; sesije, ključevi provajdera i projekti su u `/sdcard/OpenCode` i preživljavaju reinstalaciju.

| Platforma | Preuzimanje | Napomene |
| --- | --- | --- |
| Android 8.0+ (arm64) | [`app-release.apk`](https://github.com/hi77x/opencode-android/releases/latest) | BETA — odobrite pristup datotekama pri prvom pokretanju |
| Build iz izvornog koda | `./script/android/build-apk.sh` | vidi [`packages/android/README.md`](packages/android/README.md) |

```bash
# macOS (Homebrew)
brew install --cask opencode-desktop
# Windows (Scoop)
scoop bucket add extras; scoop install extras/opencode-desktop
```

#### Instalacijski direktorij

Instalacijska skripta koristi sljedeći redoslijed prioriteta za putanju instalacije:

1. `$OPENCODE_INSTALL_DIR` - Prilagođeni instalacijski direktorij
2. `$XDG_BIN_DIR` - Putanja usklađena sa XDG Base Directory specifikacijom
3. `$HOME/bin` - Standardni korisnički bin direktorij (ako postoji ili se može kreirati)
4. `$HOME/.opencode/bin` - Podrazumijevana rezervna lokacija

```bash
# Primjeri
OPENCODE_INSTALL_DIR=/usr/local/bin curl -fsSL https://opencode.ai/install | bash
XDG_BIN_DIR=$HOME/.local/bin curl -fsSL https://opencode.ai/install | bash
```

### Agenti

OpenCode uključuje dva ugrađena agenta između kojih možeš prebacivati tasterom `Tab`.

- **build** - Podrazumijevani agent sa punim pristupom za razvoj
- **plan** - Agent samo za čitanje za analizu i istraživanje koda
  - Podrazumijevano zabranjuje izmjene datoteka
  - Traži dozvolu prije pokretanja bash komandi
  - Idealan za istraživanje nepoznatih codebase-ova ili planiranje izmjena

Uključen je i **general** pod-agent za složene pretrage i višekoračne zadatke.
Koristi se interno i može se pozvati pomoću `@general` u porukama.

Saznaj više o [agentima](https://opencode.ai/docs/agents).

### Dokumentacija

Za više informacija o konfiguraciji OpenCode-a, [**pogledaj dokumentaciju**](https://opencode.ai/docs).

### Doprinosi

Ako želiš doprinositi OpenCode-u, pročitaj [upute za doprinošenje](./CONTRIBUTING.md) prije slanja pull requesta.

### Gradnja na OpenCode-u

Ako radiš na projektu koji je povezan s OpenCode-om i koristi "opencode" kao dio naziva, npr. "opencode-dashboard" ili "opencode-mobile", dodaj napomenu u svoj README da projekat nije napravio OpenCode tim i da nije povezan s nama.

---

**Pridruži se našoj zajednici** [Discord](https://discord.gg/opencode) | [X.com](https://x.com/opencode)
