#!/usr/bin/env bash
#
# Builds ripgrep for aarch64-linux-android (PIE, bionic) and installs it as
# jniLibs/arm64-v8a/librg.so. The Android host symlinks it as `rg` on PATH.
#
# Requirements: rustup with the aarch64-linux-android target and an Android NDK.
# Set ANDROID_NDK_HOME, or ANDROID_HOME/ANDROID_SDK_ROOT so the newest NDK is
# picked automatically.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
JNI_DIR="$REPO_ROOT/packages/android/app/src/main/jniLibs/arm64-v8a"
CACHE_DIR="${OPENCODE_ANDROID_CACHE:-$HOME/.cache/opencode-android}"
RIPGREP_VERSION="15.1.0"
API_LEVEL="26"

mkdir -p "$JNI_DIR" "$CACHE_DIR"
SOURCE_DIR="$CACHE_DIR/ripgrep-$RIPGREP_VERSION"

if [ ! -d "$SOURCE_DIR" ]; then
  git clone --depth 1 --branch "$RIPGREP_VERSION" https://github.com/BurntSushi/ripgrep "$SOURCE_DIR"
fi

if [ -z "${ANDROID_NDK_HOME:-}" ]; then
  SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
  if [ -z "$SDK" ] || [ ! -d "$SDK/ndk" ]; then
    echo "ANDROID_NDK_HOME (or ANDROID_HOME/ANDROID_SDK_ROOT) must point at an Android SDK with an NDK" >&2
    exit 1
  fi
  ANDROID_NDK_HOME="$(ls -1d "$SDK"/ndk/* | sort -V | tail -n 1)"
fi

HOST_TAG=""
case "$(uname -s)" in
  Linux*) HOST_TAG=linux-x86_64 ;;
  Darwin*) HOST_TAG=darwin-x86_64 ;;
  MINGW*|MSYS*|CYGWIN*) HOST_TAG=windows-x86_64 ;;
  *) echo "unsupported host: $(uname -s)" >&2; exit 1 ;;
esac
TOOLCHAIN="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/$HOST_TAG/bin"
EXT=""
if [ "$HOST_TAG" = "windows-x86_64" ]; then EXT=".cmd"; fi
LINKER="$TOOLCHAIN/aarch64-linux-android$API_LEVEL-clang$EXT"
STRIP="$TOOLCHAIN/llvm-strip"

rustup target add aarch64-linux-android

export CARGO_TARGET_AARCH64_LINUX_ANDROID_LINKER="$LINKER"
export CC_aarch64_linux_android="$LINKER"
export AR_aarch64_linux_android="$TOOLCHAIN/llvm-ar"$([ -n "$EXT" ] && echo ".exe")

(cd "$SOURCE_DIR" && cargo build --release --target aarch64-linux-android --no-default-features)
"$STRIP" --strip-all "$SOURCE_DIR/target/aarch64-linux-android/release/rg"
cp "$SOURCE_DIR/target/aarch64-linux-android/release/rg" "$JNI_DIR/librg.so"
echo "installed $JNI_DIR/librg.so"
