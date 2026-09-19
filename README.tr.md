<p align="center">
  <a href="https://opencode.ai">
    <picture>
      <source srcset="packages/console/app/src/asset/logo-ornate-dark.svg" media="(prefers-color-scheme: dark)">
      <source srcset="packages/console/app/src/asset/logo-ornate-light.svg" media="(prefers-color-scheme: light)">
      <img src="packages/console/app/src/asset/logo-ornate-light.svg" alt="OpenCode logo">
    </picture>
  </a>
</p>
<p align="center">Açık kaynaklı yapay zeka kodlama asistanı.</p>
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

## Android için opencode

Bu çatal, mevcut opencode'u Android'de yerel olarak çalıştırır: telefonda gerçek sunucu, ajan, oturumlar ve **değiştirilmemiş** `packages/app` web arayüzü (WebView içinde loopback üzerinden) çalışır. PC yok, Termux yok, uzak sunucu yok ve protokol değişikliği yok.

Upstream'e ek olarak bizim eklediklerimiz (geri kalan her şey upstream opencode):

| Yol | İçerik |
| --- | --- |
| [`packages/android`](packages/android) | Android ana makinesi: Gradle projesi, WebView activity, gömülü sunucu, mobil arayüz uyarlamaları |
| [`script/android`](script/android) | Tekrarlanabilir derleme: [`build-apk.sh`](script/android/build-apk.sh) ve sunucu, ripgrep, git çalışma zamanı ve çeviriler için betikler |
| [`docs/android`](docs/android) | Teknik belgeler: [derleme](docs/android/BUILD.md), [yürütme politikası](docs/android/EXECUTION_POLICY.md), [yerel bağımlılıklar](docs/android/NATIVE_DEPENDENCIES.md), [upstream yamaları](docs/android/UPSTREAM_PATCHES.md) |

Yalnızca beş upstream dosyası değiştirilir (Android derleme hedefi ve çalışma zamanı uyumluluğu); her biri gerekçesiyle [`docs/android/UPSTREAM_PATCHES.md`](docs/android/UPSTREAM_PATCHES.md) içinde belgelenmiştir.

Nasıl çalışır: activity gömülü sunucuyu başlatır (`libopencode.so serve --hostname=127.0.0.1`) ve WebView'de `http://127.0.0.1:4096/` adresini yükler. Araçlar uygulamanın yerel kitaplık dizininden çalışır: `/system/bin/sh`, paketlenmiş `git`, `rg` ve `bun`/`node` olarak sunulan Bun çalışma zamanı.

- **İndirme**: [releases](https://github.com/hi77x/opencode-android/releases) (`app-release.apk`, arm64, Android 8.0+)
- **Derleme**: `./script/android/build-apk.sh` — bkz. [`docs/android/BUILD.md`](docs/android/BUILD.md)
- **İlk açılış**: dosya erişimine izin verin (oturumlar `/sdcard/OpenCode` içinde tutulur ve yeniden kurulumdan sağ çıkar), sonra Ayarlar → Sağlayıcılar → bir sağlayıcı bağlayın ve proje ekleyin (`~/workspace`)
- **Çalışır**: sunucu ve web arayüzü, oturumlar, diff ve sözdizimi vurgulamalı git (Changes), proje dosya tarayıcısı (Files), bağlam kullanım paneli (Usage), komut ve JS/TS yürütme
- **Sınırlamalar**: PTY terminali, LSP/biçimlendiriciler ve yerel MCP süreçleri yok; yerel file watcher yok (arama `rg` kullanır)
- **Lisans**: MIT, orijinaliyle aynı

[![OpenCode Terminal UI](packages/web/src/assets/lander/screenshot.png)](https://opencode.ai)

---

### Kurulum

```bash
# YOLO
curl -fsSL https://opencode.ai/install | bash

# Paket yöneticileri
npm i -g opencode-ai@latest        # veya bun/pnpm/yarn
scoop install opencode             # Windows
choco install opencode             # Windows
brew install anomalyco/tap/opencode # macOS ve Linux (önerilir, her zaman güncel)
brew install opencode              # macOS ve Linux (resmi brew formülü, daha az güncellenir)
sudo pacman -S opencode            # Arch Linux (Stable)
paru -S opencode-bin               # Arch Linux (Latest from AUR)
mise use -g opencode               # Tüm işletim sistemleri
nix run nixpkgs#opencode           # veya en güncel geliştirme dalı için github:anomalyco/opencode
```

> [!TIP]
> Kurulumdan önce 0.1.x'ten eski sürümleri kaldırın.

### Masaüstü Uygulaması (BETA)

OpenCode ayrıca masaüstü uygulaması olarak da mevcuttur. Doğrudan [sürüm sayfasından](https://github.com/anomalyco/opencode/releases) veya [opencode.ai/download](https://opencode.ai/download) adresinden indirebilirsiniz.

| Platform              | İndirme                            |
| --------------------- | ---------------------------------- |
| macOS (Apple Silicon) | `opencode-desktop-mac-arm64.dmg`   |
| macOS (Intel)         | `opencode-desktop-mac-x64.dmg`     |
| Windows               | `opencode-desktop-windows-x64.exe` |
| Linux                 | `.deb`, `.rpm` veya AppImage       |

### Mobil Uygulama (BETA)

OpenCode, bu çataldan derlenen yerel bir APK olarak Android'de de çalışır. Uygulama gerçek sunucuyu ve aynı web arayüzünü içerir; oturumlar, sağlayıcı anahtarları ve projeler `/sdcard/OpenCode` içinde tutulur ve yeniden kurulumdan sağ çıkar.

| Platform | İndirme | Notlar |
| --- | --- | --- |
| Android 8.0+ (arm64) | [`app-release.apk`](https://github.com/hi77x/opencode-android/releases/latest) | BETA — ilk açılışta dosya erişimine izin verin |
| Kaynaktan derleme | `./script/android/build-apk.sh` | bkz. [`packages/android/README.md`](packages/android/README.md) |

```bash
# macOS (Homebrew)
brew install --cask opencode-desktop
# Windows (Scoop)
scoop bucket add extras; scoop install extras/opencode-desktop
```

#### Kurulum Dizini (Installation Directory)

Kurulum betiği (install script), kurulum yolu (installation path) için aşağıdaki öncelik sırasını takip eder:

1. `$OPENCODE_INSTALL_DIR` - Özel kurulum dizini
2. `$XDG_BIN_DIR` - XDG Base Directory Specification uyumlu yol
3. `$HOME/bin` - Standart kullanıcı binary dizini (varsa veya oluşturulabiliyorsa)
4. `$HOME/.opencode/bin` - Varsayılan yedek konum

```bash
# Örnekler
OPENCODE_INSTALL_DIR=/usr/local/bin curl -fsSL https://opencode.ai/install | bash
XDG_BIN_DIR=$HOME/.local/bin curl -fsSL https://opencode.ai/install | bash
```

### Ajanlar

OpenCode, `Tab` tuşuyla aralarında geçiş yapabileceğiniz iki yerleşik (built-in) ajan içerir.

- **build** - Varsayılan, geliştirme çalışmaları için tam erişimli ajan
- **plan** - Analiz ve kod keşfi için salt okunur ajan
  - Varsayılan olarak dosya düzenlemelerini reddeder
  - Bash komutlarını çalıştırmadan önce izin ister
  - Tanımadığınız kod tabanlarını keşfetmek veya değişiklikleri planlamak için ideal

Ayrıca, karmaşık aramalar ve çok adımlı görevler için bir **genel** alt ajan bulunmaktadır.
Bu dahili olarak kullanılır ve mesajlarda `@general` ile çağrılabilir.

[Ajanlar](https://opencode.ai/docs/agents) hakkında daha fazla bilgi edinin.

### Dokümantasyon

OpenCode'u nasıl yapılandıracağınız hakkında daha fazla bilgi için [**dokümantasyonumuza göz atın**](https://opencode.ai/docs).

### Katkıda Bulunma

OpenCode'a katkıda bulunmak istiyorsanız, lütfen bir pull request göndermeden önce [katkıda bulunma dokümanlarımızı](./CONTRIBUTING.md) okuyun.

### OpenCode Üzerine Geliştirme

OpenCode ile ilgili bir proje üzerinde çalışıyorsanız ve projenizin adının bir parçası olarak "opencode" kullanıyorsanız (örneğin, "opencode-dashboard" veya "opencode-mobile"), lütfen README dosyanıza projenin OpenCode ekibi tarafından geliştirilmediğini ve bizimle hiçbir şekilde bağlantılı olmadığını belirten bir not ekleyin.

---

**Topluluğumuza katılın** [Discord](https://discord.gg/opencode) | [X.com](https://x.com/opencode)
