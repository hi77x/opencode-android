<p align="center">
  <a href="https://opencode.ai">
    <picture>
      <source srcset="packages/console/app/src/asset/logo-ornate-dark.svg" media="(prefers-color-scheme: dark)">
      <source srcset="packages/console/app/src/asset/logo-ornate-light.svg" media="(prefers-color-scheme: light)">
      <img src="packages/console/app/src/asset/logo-ornate-light.svg" alt="شعار OpenCode">
    </picture>
  </a>
</p>
<p align="center">وكيل برمجة بالذكاء الاصطناعي مفتوح المصدر.</p>
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

## opencode لنظام Android

> [!IMPORTANT]
> Android data survives reinstall only if all-files access is granted: server data is stored under `/sdcard/OpenCode/home`; otherwise app-private data is removed on uninstall. You may need to reselect a project after reinstalling. The `v0.1.1` source tag predates the Android module; build from `main`. The uploaded APK has not been verified as a byte-for-byte build of `main`. See the [current Android notes](README.md#opencode-for-android).

يشغّل هذا التفرّع نسخة opencode الحالية أصلياً على Android: الهاتف يشغّل الخادم الحقيقي والوكيل والجلسات وواجهة `packages/app` **دون تغيير** (عبر loopback داخل WebView). بلا حاسوب، وبلا Termux، وبلا خادم بعيد، ودون أي تغيير في البروتوكول.

ما أضفناه فوق upstream (وكل ما تبقى هو opencode الأصلي):

| المسار | المحتوى |
| --- | --- |
| [`packages/android`](packages/android) | مضيف Android: مشروع Gradle، نشاط WebView، خادم مضمّن، تكييف الواجهة للجوال |
| [`script/android`](script/android) | بناء قابل لإعادة الإنتاج: [`build-apk.sh`](script/android/build-apk.sh) مع سكربتات الخادم وripgrep وزمن تشغيل git والترجمات |
| [`docs/android`](docs/android) | وثائق هندسية: [البناء](docs/android/BUILD.md)، [سياسة التنفيذ](docs/android/EXECUTION_POLICY.md)، [الاعتماديات الأصلية](docs/android/NATIVE_DEPENDENCIES.md)، [رقع upstream](docs/android/UPSTREAM_PATCHES.md) |

تم تعديل خمسة ملفات upstream فقط (هدف بناء Android وتوافق زمن التشغيل)، وكل تعديل موثّق مع سببه في [`docs/android/UPSTREAM_PATCHES.md`](docs/android/UPSTREAM_PATCHES.md).

كيف يعمل: يبدأ النشاط الخادم المضمّن (`libopencode.so serve --hostname=127.0.0.1`) ويحمّل `http://127.0.0.1:<port>/` في WebView. تعمل الأدوات من مجلد المكتبات الأصلية للتطبيق: `/system/bin/sh` و`git` و`rg` المضمّنة وزمن تشغيل Bun المكشوف باسم `bun`/`node`.

- **التنزيل**: [releases](https://github.com/hi77x/opencode-android/releases) (`app-release.apk`، arm64، Android 8.0+)
- **البناء**: `./script/android/build-apk.sh` — راجع [`docs/android/BUILD.md`](docs/android/BUILD.md)
- **أول تشغيل**: امنح الوصول إلى الملفات (تُحفظ الجلسات في `/sdcard/OpenCode/home` وتبقى بعد إعادة التثبيت)، ثم الإعدادات ← المزوّدون ← اربط مزوّداً وأضف مشروعاً (`~/workspace`)
- **يعمل**: الخادم وواجهة الويب، الجلسات، git مع الفروقات وتلوين الصياغة (Changes)، متصفح ملفات المشروع (Files)، لوحة استخدام السياق (Usage)، تنفيذ الأوامر وJS/TS
- **القيود**: لا طرفية PTY ولا LSP/منسّقات ولا عمليات MCP محلية؛ مراقب الملفات الأصلي غير متاح (البحث يستخدم `rg`)
- **الترخيص**: MIT كما الأصل

[![OpenCode Terminal UI](packages/web/src/assets/lander/screenshot.png)](https://opencode.ai)

---

### التثبيت

```bash
# YOLO
curl -fsSL https://opencode.ai/install | bash

# مديري الحزم
npm i -g opencode-ai@latest        # او bun/pnpm/yarn
scoop install opencode             # Windows
choco install opencode             # Windows
brew install anomalyco/tap/opencode # macOS و Linux (موصى به، دائما محدث)
brew install opencode              # macOS و Linux (صيغة brew الرسمية، تحديث اقل)
sudo pacman -S opencode            # Arch Linux (Stable)
paru -S opencode-bin               # Arch Linux (Latest from AUR)
mise use -g opencode               # اي نظام
nix run nixpkgs#opencode           # او github:anomalyco/opencode لاحدث فرع dev
```

> [!TIP]
> احذف الاصدارات الاقدم من 0.1.x قبل التثبيت.

### تطبيق سطح المكتب (BETA)

يتوفر OpenCode ايضا كتطبيق سطح مكتب. قم بالتنزيل مباشرة من [صفحة الاصدارات](https://github.com/anomalyco/opencode/releases) او من [opencode.ai/download](https://opencode.ai/download).

| المنصة                | التنزيل                            |
| --------------------- | ---------------------------------- |
| macOS (Apple Silicon) | `opencode-desktop-mac-arm64.dmg`   |
| macOS (Intel)         | `opencode-desktop-mac-x64.dmg`     |
| Windows               | `opencode-desktop-windows-x64.exe` |
| Linux                 | `.deb` او `.rpm` او AppImage       |

### تطبيق الجوال (BETA)

يعمل OpenCode أيضاً على Android كملف APK أصلي مبني من هذا التفرّع. يحتوي التطبيق على الخادم الحقيقي ونفس واجهة الويب؛ تُحفظ الجلسات ومفاتيح المزوّدين والمشاريع في `/sdcard/OpenCode/home` وتبقى بعد إعادة التثبيت.

| المنصة | التنزيل | ملاحظات |
| --- | --- | --- |
| Android 8.0+ (arm64) | [`app-release.apk`](https://github.com/hi77x/opencode-android/releases/latest) | BETA — امنح الوصول إلى الملفات عند أول تشغيل |
| البناء من المصدر | `./script/android/build-apk.sh` | راجع [`packages/android/README.md`](packages/android/README.md) |

```bash
# macOS (Homebrew)
brew install --cask opencode-desktop
# Windows (Scoop)
scoop bucket add extras; scoop install extras/opencode-desktop
```

#### مجلد التثبيت

يحترم سكربت التثبيت ترتيب الاولوية التالي لمسار التثبيت:

1. `$OPENCODE_INSTALL_DIR` - مجلد تثبيت مخصص
2. `$XDG_BIN_DIR` - مسار متوافق مع مواصفات XDG Base Directory
3. `$HOME/bin` - مجلد الثنائيات القياسي للمستخدم (ان وجد او امكن انشاؤه)
4. `$HOME/.opencode/bin` - المسار الافتراضي الاحتياطي

```bash
# امثلة
OPENCODE_INSTALL_DIR=/usr/local/bin curl -fsSL https://opencode.ai/install | bash
XDG_BIN_DIR=$HOME/.local/bin curl -fsSL https://opencode.ai/install | bash
```

### Agents

يتضمن OpenCode وكيليْن (Agents) مدمجين يمكنك التبديل بينهما باستخدام زر `Tab`.

- **build** - الافتراضي، وكيل بصلاحيات كاملة لاعمال التطوير
- **plan** - وكيل للقراءة فقط للتحليل واستكشاف الكود
  - يرفض تعديل الملفات افتراضيا
  - يطلب الاذن قبل تشغيل اوامر bash
  - مثالي لاستكشاف قواعد كود غير مألوفة او لتخطيط التغييرات

بالاضافة الى ذلك يوجد وكيل فرعي **general** للبحث المعقد والمهام متعددة الخطوات.
يستخدم داخليا ويمكن استدعاؤه بكتابة `@general` في الرسائل.

تعرف على المزيد حول [agents](https://opencode.ai/docs/agents).

### التوثيق

لمزيد من المعلومات حول كيفية ضبط OpenCode، [**راجع التوثيق**](https://opencode.ai/docs).

### المساهمة

اذا كنت مهتما بالمساهمة في OpenCode، يرجى قراءة [contributing docs](./CONTRIBUTING.md) قبل ارسال pull request.

### البناء فوق OpenCode

اذا كنت تعمل على مشروع مرتبط بـ OpenCode ويستخدم "opencode" كجزء من اسمه (مثل "opencode-dashboard" او "opencode-mobile")، يرجى اضافة ملاحظة في README توضح انه ليس مبنيا بواسطة فريق OpenCode ولا يرتبط بنا بأي شكل.

---

**انضم الى مجتمعنا** [Discord](https://discord.gg/opencode) | [X.com](https://x.com/opencode)
