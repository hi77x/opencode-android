<p align="center">
  <a href="https://opencode.ai">
    <picture>
      <source srcset="packages/console/app/src/asset/logo-ornate-dark.svg" media="(prefers-color-scheme: dark)">
      <source srcset="packages/console/app/src/asset/logo-ornate-light.svg" media="(prefers-color-scheme: light)">
      <img src="packages/console/app/src/asset/logo-ornate-light.svg" alt="OpenCode logo">
    </picture>
  </a>
</p>
<p align="center">Ο πράκτορας τεχνητής νοημοσύνης ανοικτού κώδικα για προγραμματισμό.</p>
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

## opencode για Android

Αυτό το fork εκτελεί το υπάρχον opencode εγγενώς στο Android: το τηλέφωνο τρέχει τον πραγματικό server, τον agent, τις συνεδρίες και το **αμετάβλητο** web UI του `packages/app` (μέσω loopback σε WebView). Χωρίς PC, χωρίς Termux, χωρίς απομακρυσμένο server και χωρίς αλλαγές στο πρωτόκολλο.

Τι προσθέσαμε εμείς (όλα τα υπόλοιπα είναι upstream opencode):

| Διαδρομή | Περιεχόμενο |
| --- | --- |
| [`packages/android`](packages/android) | Android host: project Gradle, Activity WebView, ενσωματωμένος server, προσαρμογές mobile UI |
| [`script/android`](script/android) | Αναπαραγώγιμο build: [`build-apk.sh`](script/android/build-apk.sh) και scripts για server, ripgrep, runtime git και μεταφράσεις |
| [`docs/android`](docs/android) | Τεχνικά έγγραφα: [build](docs/android/BUILD.md), [πολιτική εκτέλεσης](docs/android/EXECUTION_POLICY.md), [native εξαρτήσεις](docs/android/NATIVE_DEPENDENCIES.md), [patches upstream](docs/android/UPSTREAM_PATCHES.md) |

Τροποποιούνται μόνο πέντε αρχεία upstream (στόχος build Android και συμβατότητα runtime); καθένα τεκμηριώνεται με την αιτία του στο [`docs/android/UPSTREAM_PATCHES.md`](docs/android/UPSTREAM_PATCHES.md).

Πώς λειτουργεί: η Activity ξεκινά τον ενσωματωμένο server (`libopencode.so serve --hostname=127.0.0.1`) και φορτώνει το `http://127.0.0.1:4096/` σε WebView. Τα εργαλεία εκτελούνται από τον nativeLibraryDir της εφαρμογής: `/system/bin/sh`, ενσωματωμένα `git`, `rg` και runtime Bun ως `bun`/`node`.

- **Λήψη**: [releases](https://github.com/hi77x/opencode-android/releases) (`app-release.apk`, arm64, Android 8.0+)
- **Build**: `./script/android/build-apk.sh` — δείτε [`docs/android/BUILD.md`](docs/android/BUILD.md)
- **Πρώτη εκκίνηση**: δώστε πρόσβαση στα αρχεία (οι συνεδρίες βρίσκονται στο `/sdcard/OpenCode` και επιβιώνουν από επανεγκατάσταση), μετά Ρυθμίσεις → Πάροχοι → συνδέστε πάροχο και προσθέστε έργο (`~/workspace`)
- **Λειτουργεί**: server και web UI, συνεδρίες, git με diffs και επισήμανση σύνταξης (Changes), περιηγητής αρχείων (Files), πίνακας χρήσης περιβάλλοντος (Usage), εκτέλεση εντολών και JS/TS
- **Περιορισμοί**: χωρίς τερματικό PTY, LSP/formatters και τοπικές διεργασίες MCP· ο native file watcher δεν είναι διαθέσιμος (η αναζήτηση χρησιμοποιεί `rg`)
- **Άδεια**: MIT, όπως στο αρχικό

[![OpenCode Terminal UI](packages/web/src/assets/lander/screenshot.png)](https://opencode.ai)

---

### Εγκατάσταση

```bash
# YOLO
curl -fsSL https://opencode.ai/install | bash

# Διαχειριστές πακέτων
npm i -g opencode-ai@latest        # ή bun/pnpm/yarn
scoop install opencode             # Windows
choco install opencode             # Windows
brew install anomalyco/tap/opencode # macOS και Linux (προτείνεται, πάντα ενημερωμένο)
brew install opencode              # macOS και Linux (επίσημος τύπος brew, λιγότερο συχνές ενημερώσεις)
sudo pacman -S opencode            # Arch Linux (Σταθερό)
paru -S opencode-bin               # Arch Linux (Τελευταία έκδοση από AUR)
mise use -g opencode               # Οποιοδήποτε λειτουργικό σύστημα
nix run nixpkgs#opencode           # ή github:anomalyco/opencode με βάση την πιο πρόσφατη αλλαγή από το dev branch
```

> [!TIP]
> Αφαίρεσε παλαιότερες εκδόσεις από τη 0.1.x πριν από την εγκατάσταση.

### Εφαρμογή Desktop (BETA)

Το OpenCode είναι επίσης διαθέσιμο ως εφαρμογή. Κατέβασε το απευθείας από τη [σελίδα εκδόσεων](https://github.com/anomalyco/opencode/releases) ή το [opencode.ai/download](https://opencode.ai/download).

| Πλατφόρμα             | Λήψη                               |
| --------------------- | ---------------------------------- |
| macOS (Apple Silicon) | `opencode-desktop-mac-arm64.dmg`   |
| macOS (Intel)         | `opencode-desktop-mac-x64.dmg`     |
| Windows               | `opencode-desktop-windows-x64.exe` |
| Linux                 | `.deb`, `.rpm`, ή AppImage         |

### Εφαρμογή για κινητά (BETA)

Το OpenCode λειτουργεί επίσης σε Android ως εγγενές APK που χτίζεται από αυτό το fork. Η εφαρμογή περιλαμβάνει τον πραγματικό server και το ίδιο web UI· οι συνεδρίες, τα κλειδιά παρόχων και τα έργα βρίσκονται στο `/sdcard/OpenCode` και επιβιώνουν από επανεγκατάσταση.

| Πλατφόρμα | Λήψη | Σημειώσεις |
| --- | --- | --- |
| Android 8.0+ (arm64) | [`app-release.apk`](https://github.com/hi77x/opencode-android/releases/latest) | BETA — δώστε πρόσβαση στα αρχεία στην πρώτη εκκίνηση |
| Build από πηγαίο κώδικα | `./script/android/build-apk.sh` | δείτε [`packages/android/README.md`](packages/android/README.md) |

```bash
# macOS (Homebrew)
brew install --cask opencode-desktop
# Windows (Scoop)
scoop bucket add extras; scoop install extras/opencode-desktop
```

#### Κατάλογος Εγκατάστασης

Το script εγκατάστασης τηρεί την ακόλουθη σειρά προτεραιότητας για τη διαδρομή εγκατάστασης:

1. `$OPENCODE_INSTALL_DIR` - Προσαρμοσμένος κατάλογος εγκατάστασης
2. `$XDG_BIN_DIR` - Διαδρομή συμβατή με τις προδιαγραφές XDG Base Directory
3. `$HOME/bin` - Τυπικός κατάλογος εκτελέσιμων αρχείων χρήστη (εάν υπάρχει ή μπορεί να δημιουργηθεί)
4. `$HOME/.opencode/bin` - Προεπιλεγμένη εφεδρική διαδρομή

```bash
# Παραδείγματα
OPENCODE_INSTALL_DIR=/usr/local/bin curl -fsSL https://opencode.ai/install | bash
XDG_BIN_DIR=$HOME/.local/bin curl -fsSL https://opencode.ai/install | bash
```

### Πράκτορες

Το OpenCode περιλαμβάνει δύο ενσωματωμένους πράκτορες μεταξύ των οποίων μπορείτε να εναλλάσσεστε με το πλήκτρο `Tab`.

- **build** - Προεπιλεγμένος πράκτορας με πλήρη πρόσβαση για εργασία πάνω σε κώδικα
- **plan** - Πράκτορας μόνο ανάγνωσης για ανάλυση και εξερεύνηση κώδικα
  - Αρνείται την επεξεργασία αρχείων από προεπιλογή
  - Ζητά άδεια πριν εκτελέσει εντολές bash
  - Ιδανικός για εξερεύνηση άγνωστων αρχείων πηγαίου κώδικα ή σχεδιασμό αλλαγών

Περιλαμβάνεται επίσης ένας **general** υποπράκτορας για σύνθετες αναζητήσεις και πολυβηματικές διεργασίες.
Χρησιμοποιείται εσωτερικά και μπορεί να κληθεί χρησιμοποιώντας `@general` στα μηνύματα.

Μάθετε περισσότερα για τους [πράκτορες](https://opencode.ai/docs/agents).

### Οδηγός Χρήσης

Για περισσότερες πληροφορίες σχετικά με τη ρύθμιση του OpenCode, [**πλοηγήσου στον οδηγό χρήσης μας**](https://opencode.ai/docs).

### Συνεισφορά

Εάν ενδιαφέρεσαι να συνεισφέρεις στο OpenCode, διαβάστε τα [οδηγό χρήσης συνεισφοράς](./CONTRIBUTING.md) πριν υποβάλεις ένα pull request.

### Δημιουργία πάνω στο OpenCode

Εάν εργάζεσαι σε ένα έργο σχετικό με το OpenCode και χρησιμοποιείτε το "opencode" ως μέρος του ονόματός του, για παράδειγμα "opencode-dashboard" ή "opencode-mobile", πρόσθεσε μια σημείωση στο README σας για να διευκρινίσεις ότι δεν είναι κατασκευασμένο από την ομάδα του OpenCode και δεν έχει καμία σχέση με εμάς.

---

**Γίνε μέλος της κοινότητάς μας** [Discord](https://discord.gg/opencode) | [X.com](https://x.com/opencode)
