<p align="center">
  <a href="https://opencode.ai">
    <picture>
      <source srcset="packages/console/app/src/asset/logo-ornate-dark.svg" media="(prefers-color-scheme: dark)">
      <source srcset="packages/console/app/src/asset/logo-ornate-light.svg" media="(prefers-color-scheme: light)">
      <img src="packages/console/app/src/asset/logo-ornate-light.svg" alt="OpenCode logo">
    </picture>
  </a>
</p>
<p align="center">ওপেন সোর্স এআই কোডিং এজেন্ট।</p>
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

## Android-এর জন্য opencode

> [!IMPORTANT]
> Android data survives reinstall only if all-files access is granted: server data is stored under `/sdcard/OpenCode/home`; otherwise app-private data is removed on uninstall. You may need to reselect a project after reinstalling. The `v0.1.1` source tag predates the Android module; build from `main`. The uploaded APK has not been verified as a byte-for-byte build of `main`. See the [current Android notes](README.md#opencode-for-android).

এই ফর্কটি বিদ্যমান opencode-কে Android-এ নেটিভভাবে চালায়: ফোনে আসল সার্ভার, এজেন্ট, সেশন এবং **অপরিবর্তিত** `packages/app` ওয়েব ইন্টারফেস (WebView-এ loopback-এর মাধ্যমে) চলে। পিসি, Termux বা দূরবর্তী সার্ভার লাগে না এবং প্রোটোকল বদলানো হয় না।

আমরা upstream-এর উপরে যা যোগ করেছি (বাকি সব upstream opencode):

| পাথ | বিষয়বস্তু |
| --- | --- |
| [`packages/android`](packages/android) | Android হোস্ট: Gradle প্রকল্প, WebView Activity, এমবেডেড সার্ভার, মোবাইল UI অ্যাডাপ্টেশন |
| [`script/android`](script/android) | পুনরুৎপাদনযোগ্য বিল্ড: [`build-apk.sh`](script/android/build-apk.sh) এবং সার্ভার, ripgrep, git রানটাইম ও অনুবাদের স্ক্রিপ্ট |
| [`docs/android`](docs/android) | কারিগরি ডকুমেন্ট: [বিল্ড](docs/android/BUILD.md), [এক্সিকিউশন পলিসি](docs/android/EXECUTION_POLICY.md), [নেটিভ নির্ভরতা](docs/android/NATIVE_DEPENDENCIES.md), [upstream প্যাচ](docs/android/UPSTREAM_PATCHES.md) |

শুধু পাঁচটি upstream ফাইল পরিবর্তন করা হয়েছে (Android বিল্ড টার্গেট ও রানটাইম সামঞ্জস্য); প্রতিটির কারণ [`docs/android/UPSTREAM_PATCHES.md`](docs/android/UPSTREAM_PATCHES.md)-এ লেখা আছে।

কীভাবে কাজ করে: Activity এমবেডেড সার্ভার চালু করে (`libopencode.so serve --hostname=127.0.0.1`) এবং WebView-এ `http://127.0.0.1:<port>/` লোড করে। টুলগুলো অ্যাপের nativeLibraryDir থেকে চলে: `/system/bin/sh`, বান্ডল করা `git`, `rg` এবং `bun`/`node` নামে Bun রানটাইম।

- **ডাউনলোড**: [releases](https://github.com/hi77x/opencode-android/releases) (`app-release.apk`, arm64, Android 8.0+)
- **বিল্ড**: `./script/android/build-apk.sh` — দেখুন [`docs/android/BUILD.md`](docs/android/BUILD.md)
- **প্রথম চালু**: ফাইল অ্যাক্সেস দিন (সেশন `/sdcard/OpenCode/home`-এ থাকে এবং পুনঃইনস্টলেও টিকে থাকে), তারপর সেটিংস → প্রোভাইডার → একটি প্রোভাইডার যুক্ত করুন এবং একটি প্রকল্প (`~/workspace`) যোগ করুন
- **কাজ করে**: সার্ভার ও ওয়েব UI, সেশন, diff ও সিনট্যাক্স হাইলাইটসহ git (Changes), প্রকল্প ফাইল ব্রাউজার (Files), কনটেক্সট ব্যবহার প্যানেল (Usage), কমান্ড ও JS/TS চালানো
- **সীমাবদ্ধতা**: PTY টার্মিনাল, LSP/ফরম্যাটার ও লোকাল MCP প্রসেস নেই; নেটিভ file watcher নেই (সার্চ `rg` ব্যবহার করে)
- **লাইসেন্স**: MIT, মূল প্রকল্পের মতোই

[![OpenCode Terminal UI](packages/web/src/assets/lander/screenshot.png)](https://opencode.ai)

---

### ইনস্টলেশন (Installation)

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
> ইনস্টল করার আগে ০.১.x এর চেয়ে পুরোনো ভার্সনগুলো মুছে ফেলুন।

### ডেস্কটপ অ্যাপ (BETA)

OpenCode ডেস্কটপ অ্যাপ্লিকেশন হিসেবেও উপলব্ধ। সরাসরি [রিলিজ পেজ](https://github.com/anomalyco/opencode/releases) অথবা [opencode.ai/download](https://opencode.ai/download) থেকে ডাউনলোড করুন।

| প্ল্যাটফর্ম           | ডাউনলোড                            |
| --------------------- | ---------------------------------- |
| macOS (Apple Silicon) | `opencode-desktop-mac-arm64.dmg`   |
| macOS (Intel)         | `opencode-desktop-mac-x64.dmg`     |
| Windows               | `opencode-desktop-windows-x64.exe` |
| Linux                 | `.deb`, `.rpm`, or `.AppImage`     |

### মোবাইল অ্যাপ (BETA)

OpenCode এই ফর্ক থেকে বিল্ড করা নেটিভ APK হিসেবে Android-এও চলে। অ্যাপে আসল সার্ভার ও একই ওয়েব UI আছে; সেশন, প্রোভাইডার কী এবং প্রকল্প `/sdcard/OpenCode/home`-এ থাকে এবং পুনঃইনস্টলেও টিকে থাকে।

| প্ল্যাটফর্ম | ডাউনলোড | নোট |
| --- | --- | --- |
| Android 8.0+ (arm64) | [`app-release.apk`](https://github.com/hi77x/opencode-android/releases/latest) | BETA — প্রথম চালুতে ফাইল অ্যাক্সেস দিন |
| সোর্স থেকে বিল্ড | `./script/android/build-apk.sh` | দেখুন [`packages/android/README.md`](packages/android/README.md) |

```bash
# macOS (Homebrew)
brew install --cask opencode-desktop
# Windows (Scoop)
scoop bucket add extras; scoop install extras/opencode-desktop
```

#### ইনস্টলেশন ডিরেক্টরি (Installation Directory)

ইনস্টল স্ক্রিপ্টটি ইনস্টলেশন পাতের জন্য নিম্নলিখিত অগ্রাধিকার ক্রম মেনে চলে:

1. `$OPENCODE_INSTALL_DIR` - কাস্টম ইনস্টলেশন ডিরেক্টরি
2. `$XDG_BIN_DIR` - XDG বেস ডিরেক্টরি স্পেসিফিকেশন সমর্থিত পাথ
3. `$HOME/bin` - সাধারণ ব্যবহারকারী বাইনারি ডিরেক্টরি (যদি বিদ্যমান থাকে বা তৈরি করা যায়)
4. `$HOME/.opencode/bin` - ডিফল্ট ফলব্যাক

```bash
# উদাহরণ
OPENCODE_INSTALL_DIR=/usr/local/bin curl -fsSL https://opencode.ai/install | bash
XDG_BIN_DIR=$HOME/.local/bin curl -fsSL https://opencode.ai/install | bash
```

### এজেন্টস (Agents)

OpenCode এ দুটি বিল্ট-ইন এজেন্ট রয়েছে যা আপনি `Tab` কি(key) দিয়ে পরিবর্তন করতে পারবেন।

- **build** - ডিফল্ট, ডেভেলপমেন্টের কাজের জন্য সম্পূর্ণ অ্যাক্সেসযুক্ত এজেন্ট
- **plan** - বিশ্লেষণ এবং কোড এক্সপ্লোরেশনের জন্য রিড-ওনলি এজেন্ট
  - ডিফল্টভাবে ফাইল এডিট করতে দেয় না
  - ব্যাশ কমান্ড চালানোর আগে অনুমতি চায়
  - অপরিচিত কোডবেস এক্সপ্লোর করা বা পরিবর্তনের পরিকল্পনা করার জন্য আদর্শ

এছাড়াও জটিল অনুসন্ধান এবং মাল্টিস্টেপ টাস্কের জন্য একটি **general** সাবএজেন্ট অন্তর্ভুক্ত রয়েছে।
এটি অভ্যন্তরীণভাবে ব্যবহৃত হয় এবং মেসেজে `@general` লিখে ব্যবহার করা যেতে পারে।

এজেন্টদের সম্পর্কে আরও জানুন: [docs](https://opencode.ai/docs/agents)।

### ডকুমেন্টেশন (Documentation)

কিভাবে OpenCode কনফিগার করবেন সে সম্পর্কে আরও তথ্যের জন্য, [**আমাদের ডকস দেখুন**](https://opencode.ai/docs)।

### অবদান (Contributing)

আপনি যদি OpenCode এ অবদান রাখতে চান, অনুগ্রহ করে একটি পুল রিকোয়েস্ট সাবমিট করার আগে আমাদের [কন্ট্রিবিউটিং ডকস](./CONTRIBUTING.md) পড়ে নিন।

### OpenCode এর উপর বিল্ডিং (Building on OpenCode)

আপনি যদি এমন প্রজেক্টে কাজ করেন যা OpenCode এর সাথে সম্পর্কিত এবং প্রজেক্টের নামের অংশ হিসেবে "opencode" ব্যবহার করেন, উদাহরণস্বরূপ "opencode-dashboard" বা "opencode-mobile", তবে দয়া করে আপনার README তে একটি নোট যোগ করে স্পষ্ট করুন যে এই প্রজেক্টটি OpenCode দল দ্বারা তৈরি হয়নি এবং আমাদের সাথে এর কোনো সরাসরি সম্পর্ক নেই।

---

**আমাদের কমিউনিটিতে যুক্ত হোন** [Discord](https://discord.gg/opencode) | [X.com](https://x.com/opencode)
