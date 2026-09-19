<p align="center">
  <a href="https://opencode.ai">
    <picture>
      <source srcset="packages/console/app/src/asset/logo-ornate-dark.svg" media="(prefers-color-scheme: dark)">
      <source srcset="packages/console/app/src/asset/logo-ornate-light.svg" media="(prefers-color-scheme: light)">
      <img src="packages/console/app/src/asset/logo-ornate-light.svg" alt="OpenCode logo">
    </picture>
  </a>
</p>
<p align="center">Otwartoźródłowy agent kodujący AI.</p>
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

## opencode dla Androida

Ten fork uruchamia istniejący opencode natywnie na Androidzie: telefon uruchamia prawdziwy serwer, agenta, sesje i **niezmieniony** interfejs `packages/app` (przez loopback w WebView). Bez komputera, bez Termuksa, bez zdalnego serwera i bez zmian protokołu.

Co dodaliśmy od siebie (reszta to upstream opencode):

| Ścieżka | Zawartość |
| --- | --- |
| [`packages/android`](packages/android) | Host Androida: projekt Gradle, activity z WebView, wbudowany serwer, adaptacje mobilnego UI |
| [`script/android`](script/android) | Powtarzalna kompilacja: [`build-apk.sh`](script/android/build-apk.sh) oraz skrypty serwera, ripgrep, środowiska git i tłumaczeń |
| [`docs/android`](docs/android) | Dokumentacja techniczna: [kompilacja](docs/android/BUILD.md), [polityka wykonywania](docs/android/EXECUTION_POLICY.md), [zależności natywne](docs/android/NATIVE_DEPENDENCIES.md), [patch-e upstream](docs/android/UPSTREAM_PATCHES.md) |

Zmieniono tylko pięć plików upstream (cel kompilacji Androida i zgodność środowiska); każdy opisano w [`docs/android/UPSTREAM_PATCHES.md`](docs/android/UPSTREAM_PATCHES.md).

Jak to działa: activity uruchamia wbudowany serwer (`libopencode.so serve --hostname=127.0.0.1`) i ładuje `http://127.0.0.1:4096/` w WebView. Narzędzia działają z katalogu bibliotek natywnych: `/system/bin/sh`, dołączone `git`, `rg` oraz środowisko Bun jako `bun`/`node`.

- **Pobierz**: [releases](https://github.com/hi77x/opencode-android/releases) (`app-release.apk`, arm64, Android 8.0+)
- **Kompilacja**: `./script/android/build-apk.sh` — zob. [`docs/android/BUILD.md`](docs/android/BUILD.md)
- **Pierwsze uruchomienie**: przyznaj dostęp do plików (sesje są w `/sdcard/OpenCode` i przeżywają reinstalację), następnie Ustawienia → Dostawcy → połącz dostawcę i dodaj projekt (`~/workspace`)
- **Działa**: serwer i interfejs webowy, sesje, git z diffami i podświetlaniem (Changes), przeglądarka plików (Files), panel użycia kontekstu (Usage), wykonywanie poleceń i JS/TS
- **Ograniczenia**: brak terminala PTY, LSP/formatterów i lokalnych procesów MCP; natywny file watcher jest niedostępny (wyszukiwanie używa `rg`)
- **Licencja**: MIT, jak w oryginale

[![OpenCode Terminal UI](packages/web/src/assets/lander/screenshot.png)](https://opencode.ai)

---

### Instalacja

```bash
# YOLO
curl -fsSL https://opencode.ai/install | bash

# Menedżery pakietów
npm i -g opencode-ai@latest        # albo bun/pnpm/yarn
scoop install opencode             # Windows
choco install opencode             # Windows
brew install anomalyco/tap/opencode # macOS i Linux (polecane, zawsze aktualne)
brew install opencode              # macOS i Linux (oficjalna formuła brew, rzadziej aktualizowana)
sudo pacman -S opencode            # Arch Linux (Stable)
paru -S opencode-bin               # Arch Linux (Latest from AUR)
mise use -g opencode               # dowolny system
nix run nixpkgs#opencode           # lub github:anomalyco/opencode dla najnowszej gałęzi dev
```

> [!TIP]
> Przed instalacją usuń wersje starsze niż 0.1.x.

### Aplikacja desktopowa (BETA)

OpenCode jest także dostępny jako aplikacja desktopowa. Pobierz ją bezpośrednio ze strony [releases](https://github.com/anomalyco/opencode/releases) lub z [opencode.ai/download](https://opencode.ai/download).

| Platforma             | Pobieranie                         |
| --------------------- | ---------------------------------- |
| macOS (Apple Silicon) | `opencode-desktop-mac-arm64.dmg`   |
| macOS (Intel)         | `opencode-desktop-mac-x64.dmg`     |
| Windows               | `opencode-desktop-windows-x64.exe` |
| Linux                 | `.deb`, `.rpm` lub AppImage        |

### Aplikacja mobilna (BETA)

OpenCode działa też na Androidzie jako natywna APK zbudowana z tego forka. Aplikacja zawiera prawdziwy serwer i ten sam interfejs webowy; sesje, klucze dostawców i projekty są w `/sdcard/OpenCode` i przeżywają reinstalację.

| Platforma | Pobieranie | Uwagi |
| --- | --- | --- |
| Android 8.0+ (arm64) | [`app-release.apk`](https://github.com/hi77x/opencode-android/releases/latest) | BETA — przyznaj dostęp do plików przy pierwszym uruchomieniu |
| Kompilacja ze źródeł | `./script/android/build-apk.sh` | zob. [`packages/android/README.md`](packages/android/README.md) |

```bash
# macOS (Homebrew)
brew install --cask opencode-desktop
# Windows (Scoop)
scoop bucket add extras; scoop install extras/opencode-desktop
```

#### Katalog instalacji

Skrypt instalacyjny stosuje następujący priorytet wyboru ścieżki instalacji:

1. `$OPENCODE_INSTALL_DIR` - Własny katalog instalacji
2. `$XDG_BIN_DIR` - Ścieżka zgodna ze specyfikacją XDG Base Directory
3. `$HOME/bin` - Standardowy katalog binarny użytkownika (jeśli istnieje lub można go utworzyć)
4. `$HOME/.opencode/bin` - Domyślny fallback

```bash
# Przykłady
OPENCODE_INSTALL_DIR=/usr/local/bin curl -fsSL https://opencode.ai/install | bash
XDG_BIN_DIR=$HOME/.local/bin curl -fsSL https://opencode.ai/install | bash
```

### Agents

OpenCode zawiera dwóch wbudowanych agentów, między którymi możesz przełączać się klawiszem `Tab`.

- **build** - Domyślny agent z pełnym dostępem do pracy developerskiej
- **plan** - Agent tylko do odczytu do analizy i eksploracji kodu
  - Domyślnie odmawia edycji plików
  - Pyta o zgodę przed uruchomieniem komend bash
  - Idealny do poznawania nieznanych baz kodu lub planowania zmian

Dodatkowo jest subagent **general** do złożonych wyszukiwań i wieloetapowych zadań.
Jest używany wewnętrznie i można go wywołać w wiadomościach przez `@general`.

Dowiedz się więcej o [agents](https://opencode.ai/docs/agents).

### Dokumentacja

Więcej informacji o konfiguracji OpenCode znajdziesz w [**dokumentacji**](https://opencode.ai/docs).

### Współtworzenie

Jeśli chcesz współtworzyć OpenCode, przeczytaj [contributing docs](./CONTRIBUTING.md) przed wysłaniem pull requesta.

### Budowanie na OpenCode

Jeśli pracujesz nad projektem związanym z OpenCode i używasz "opencode" jako części nazwy (na przykład "opencode-dashboard" lub "opencode-mobile"), dodaj proszę notatkę do swojego README, aby wyjaśnić, że projekt nie jest tworzony przez zespół OpenCode i nie jest z nami w żaden sposób powiązany.

---

**Dołącz do naszej społeczności** [Discord](https://discord.gg/opencode) | [X.com](https://x.com/opencode)
