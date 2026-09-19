<p align="center">
  <a href="https://opencode.ai">
    <picture>
      <source srcset="packages/console/app/src/asset/logo-ornate-dark.svg" media="(prefers-color-scheme: dark)">
      <source srcset="packages/console/app/src/asset/logo-ornate-light.svg" media="(prefers-color-scheme: light)">
      <img src="packages/console/app/src/asset/logo-ornate-light.svg" alt="OpenCode logo">
    </picture>
  </a>
</p>
<p align="center">El agente de programación con IA de código abierto.</p>
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

## opencode para Android

Este fork ejecuta el opencode existente de forma nativa en Android: el teléfono ejecuta el servidor real, el agente, las sesiones y la interfaz **sin cambios** de `packages/app` (por loopback en un WebView). Sin PC, sin Termux, sin servidor remoto y sin cambios de protocolo.

Lo que añadimos nosotros (todo lo demás es opencode upstream):

| Ruta | Contenido |
| --- | --- |
| [`packages/android`](packages/android) | Host Android: proyecto Gradle, actividad WebView, servidor embebido, adaptaciones móviles de la interfaz |
| [`script/android`](script/android) | Compilación reproducible: [`build-apk.sh`](script/android/build-apk.sh) y scripts para el servidor, ripgrep, runtime de git y traducciones |
| [`docs/android`](docs/android) | Documentos técnicos: [compilación](docs/android/BUILD.md), [política de ejecución](docs/android/EXECUTION_POLICY.md), [dependencias nativas](docs/android/NATIVE_DEPENDENCIES.md), [parches upstream](docs/android/UPSTREAM_PATCHES.md) |

Solo se modifican cinco archivos upstream (objetivo de compilación Android y compatibilidad del runtime); cada uno está justificado en [`docs/android/UPSTREAM_PATCHES.md`](docs/android/UPSTREAM_PATCHES.md).

Cómo funciona: la actividad inicia el servidor embebido (`libopencode.so serve --hostname=127.0.0.1`) y carga `http://127.0.0.1:4096/` en un WebView. Las herramientas se ejecutan desde el directorio de bibliotecas nativas: `/system/bin/sh`, `git`, `rg` incluidos y un runtime Bun expuesto como `bun`/`node`.

- **Descarga**: [releases](https://github.com/hi77x/opencode-android/releases) (`app-release.apk`, arm64, Android 8.0+)
- **Compilación**: `./script/android/build-apk.sh` — ver [`docs/android/BUILD.md`](docs/android/BUILD.md)
- **Primer inicio**: concede acceso a archivos (las sesiones viven en `/sdcard/OpenCode` y sobreviven a las reinstalaciones), luego Ajustes → Proveedores → conecta un proveedor y añade un proyecto (`~/workspace`)
- **Funciona**: servidor e interfaz web, sesiones, git con diffs y resaltado (Changes), explorador de archivos (Files), panel de uso del contexto (Usage), ejecución de comandos y JS/TS
- **Limitaciones**: sin terminal PTY, sin LSP/formateadores, sin procesos MCP locales; el file watcher nativo no está disponible (la búsqueda usa `rg`)
- **Licencia**: MIT, igual que el original

[![OpenCode Terminal UI](packages/web/src/assets/lander/screenshot.png)](https://opencode.ai)

---

### Instalación

```bash
# YOLO
curl -fsSL https://opencode.ai/install | bash

# Gestores de paquetes
npm i -g opencode-ai@latest        # o bun/pnpm/yarn
scoop install opencode             # Windows
choco install opencode             # Windows
brew install anomalyco/tap/opencode # macOS y Linux (recomendado, siempre al día)
brew install opencode              # macOS y Linux (fórmula oficial de brew, se actualiza menos)
sudo pacman -S opencode            # Arch Linux (Stable)
paru -S opencode-bin               # Arch Linux (Latest from AUR)
mise use -g opencode               # cualquier sistema
nix run nixpkgs#opencode           # o github:anomalyco/opencode para la rama dev más reciente
```

> [!TIP]
> Elimina versiones anteriores a 0.1.x antes de instalar.

### App de escritorio (BETA)

OpenCode también está disponible como aplicación de escritorio. Descárgala directamente desde la [página de releases](https://github.com/anomalyco/opencode/releases) o desde [opencode.ai/download](https://opencode.ai/download).

| Plataforma            | Descarga                           |
| --------------------- | ---------------------------------- |
| macOS (Apple Silicon) | `opencode-desktop-mac-arm64.dmg`   |
| macOS (Intel)         | `opencode-desktop-mac-x64.dmg`     |
| Windows               | `opencode-desktop-windows-x64.exe` |
| Linux                 | `.deb`, `.rpm`, o AppImage         |

### Aplicación móvil (BETA)

OpenCode también funciona en Android como APK nativa compilada desde este fork. La app incluye el servidor real y la misma interfaz web; las sesiones, claves de proveedores y proyectos están en `/sdcard/OpenCode` y sobreviven a las reinstalaciones.

| Plataforma | Descarga | Notas |
| --- | --- | --- |
| Android 8.0+ (arm64) | [`app-release.apk`](https://github.com/hi77x/opencode-android/releases/latest) | BETA — concede acceso a archivos en el primer inicio |
| Compilar desde el código | `./script/android/build-apk.sh` | ver [`packages/android/README.md`](packages/android/README.md) |

```bash
# macOS (Homebrew)
brew install --cask opencode-desktop
# Windows (Scoop)
scoop bucket add extras; scoop install extras/opencode-desktop
```

#### Directorio de instalación

El script de instalación respeta el siguiente orden de prioridad para la ruta de instalación:

1. `$OPENCODE_INSTALL_DIR` - Directorio de instalación personalizado
2. `$XDG_BIN_DIR` - Ruta compatible con la especificación XDG Base Directory
3. `$HOME/bin` - Directorio binario estándar del usuario (si existe o se puede crear)
4. `$HOME/.opencode/bin` - Alternativa por defecto

```bash
# Ejemplos
OPENCODE_INSTALL_DIR=/usr/local/bin curl -fsSL https://opencode.ai/install | bash
XDG_BIN_DIR=$HOME/.local/bin curl -fsSL https://opencode.ai/install | bash
```

### Agentes

OpenCode incluye dos agentes integrados que puedes alternar con la tecla `Tab`.

- **build** - Por defecto, agente con acceso completo para tareas de desarrollo
- **plan** - Agente de solo lectura para análisis y exploración de código
  - Deniega ediciones de archivos por defecto
  - Pide permiso antes de ejecutar comandos bash
  - Ideal para explorar codebases desconocidas o planificar cambios

Además, incluye un subagente **general** para búsquedas complejas y tareas de varios pasos.
Se usa internamente y se puede invocar con `@general` en los mensajes.

Más información sobre [agentes](https://opencode.ai/docs/agents).

### Documentación

Para más información sobre cómo configurar OpenCode, [**ve a nuestra documentación**](https://opencode.ai/docs).

### Contribuir

Si te interesa contribuir a OpenCode, lee nuestras [docs de contribución](./CONTRIBUTING.md) antes de enviar un pull request.

### Proyectos basados en OpenCode

Si estás trabajando en un proyecto basado en OpenCode y usas "opencode" como parte del nombre, por ejemplo, "opencode-dashboard" u "opencode-mobile", agrega una nota en tu README para aclarar que no está hecho por el equipo de OpenCode y que no está afiliado con nosotros de ninguna manera.

---

**Únete a nuestra comunidad** [Discord](https://discord.gg/opencode) | [X.com](https://x.com/opencode)
