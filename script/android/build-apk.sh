#!/usr/bin/env bash
#
# Builds the OpenCode Android debug APK from a clean checkout.
#
# Steps:
#   1. build the opencode server for bun-linux-arm64-android (with the embedded
#      packages/app production bundle) and place it in jniLibs;
#   2. fetch the bare Bun Android runtime used as `bun`/`node` in the shell;
#   3. build ripgrep for aarch64-linux-android when it is not bundled yet;
#   4. fetch the Termux git runtime ZIP into assets;
#   5. sync the app's context-usage translations into assets;
#   6. assemble the debug APK with the Android Gradle wrapper.
#
# Requirements: bun >= 1.4.2, python 3, curl, rustup (only for step 3),
# Android SDK + NDK (for step 3) and a JDK for Gradle.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ANDROID_DIR="$REPO_ROOT/packages/android"
JNI_DIR="$ANDROID_DIR/app/src/main/jniLibs/arm64-v8a"
ASSET_DIR="$ANDROID_DIR/app/src/main/assets"
CACHE_DIR="${OPENCODE_ANDROID_CACHE:-$HOME/.cache/opencode-android}"
BUN_VERSION="1.4.2"
RIPGREP_VERSION="15.1.0"

mkdir -p "$JNI_DIR" "$ASSET_DIR" "$CACHE_DIR"

echo "==> [1/6] building opencode server (linux-arm64-android)"
(cd "$REPO_ROOT/packages/opencode" && bun run script/build.ts --target=linux-arm64-android)
cp "$REPO_ROOT/packages/opencode/dist/opencode-linux-arm64-android/bin/opencode" "$JNI_DIR/libopencode.so"

echo "==> [2/6] fetching Bun $BUN_VERSION Android runtime"
BUN_ZIP="$CACHE_DIR/bun-linux-aarch64-android-$BUN_VERSION.zip"
if [ ! -f "$BUN_ZIP" ]; then
  curl -fsSL -o "$BUN_ZIP" \
    "https://github.com/oven-sh/bun/releases/download/bun-v$BUN_VERSION/bun-linux-aarch64-android.zip"
fi
rm -rf "$CACHE_DIR/bun-android"
mkdir -p "$CACHE_DIR/bun-android"
python -c "import sys, zipfile; zipfile.ZipFile(sys.argv[1]).extractall(sys.argv[2])" "$BUN_ZIP" "$CACHE_DIR/bun-android"
cp "$CACHE_DIR/bun-android/bun-linux-aarch64-android/bun" "$JNI_DIR/libbun.so"

echo "==> [3/6] ripgrep for aarch64-linux-android"
if [ ! -f "$JNI_DIR/librg.so" ]; then
  "$REPO_ROOT/script/android/build-ripgrep.sh"
else
  echo "    librg.so already present, skipping (delete it to rebuild)"
fi

echo "==> [4/6] fetching Termux git runtime"
python "$REPO_ROOT/script/android/fetch-termux-runtime.py"

echo "==> [5/6] syncing usage panel translations"
python "$REPO_ROOT/script/android/extract-usage-i18n.py"

echo "==> [6/6] assembling debug APK"
(cd "$ANDROID_DIR" && ./gradlew :app:assembleDebug)

APK="$ANDROID_DIR/app/build/outputs/apk/debug/app-debug.apk"
echo
echo "APK: $APK"
ls -la "$APK"
