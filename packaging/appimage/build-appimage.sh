#!/usr/bin/env bash
#
# Wraps the jpackage Linux app image (PlayWall-<ver>.<arch>.tar.gz) into an AppImage.
#
# System libraries (GTK3, Mesa, ALSA/PipeWire, X11, ...) are NOT bundled; they come from the host. This is not tied to
# a specific distribution. Only the self-contained jpackage app image (own JDK, JavaFX, Rust audio lib) is packaged.
#
# Usage:  build-appimage.sh <app-image-tarball> <output.AppImage> [x86_64|aarch64]
#         The architecture defaults to the one of the build machine. appimagetool is a native binary, so the build
#         machine must match the target architecture (use an arm64 runner for aarch64).
# Needs:  bash, curl, tar, sha256sum
set -euo pipefail

TARBALL="${1:?usage: $0 <app-image-tarball> <output.AppImage> [x86_64|aarch64]}"
OUTPUT="${2:?usage: $0 <app-image-tarball> <output.AppImage> [x86_64|aarch64]}"
ARCH="${3:-$(uname -m)}"

# Pinned tool versions and checksums (update together).
APPIMAGETOOL_VERSION="1.9.1"
RUNTIME_VERSION="20251108"
case "${ARCH}" in
  x86_64)
    APPIMAGETOOL_SHA256="ed4ce84f0d9caff66f50bcca6ff6f35aae54ce8135408b3fa33abfc3cb384eb0"
    RUNTIME_SHA256="2fca8b443c92510f1483a883f60061ad09b46b978b2631c807cd873a47ec260d"
    ;;
  aarch64)
    APPIMAGETOOL_SHA256="f0837e7448a0c1e4e650a93bb3e85802546e60654ef287576f46c71c126a9158"
    RUNTIME_SHA256="00cbdfcf917cc6c0ff6d3347d59e0ca1f7f45a6df1a428a0d6d8a78664d87444"
    ;;
  *)
    echo "Unsupported architecture: ${ARCH} (expected x86_64 or aarch64)" >&2
    exit 1
    ;;
esac

if [ "$(uname -m)" != "${ARCH}" ]; then
  echo "Build machine is $(uname -m) but the target is ${ARCH}; appimagetool is a native binary." >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
ICON="${REPO_ROOT}/PlayWallClient/src/main/resources/de/tobias/playwall/client/logo/icon_large.png"

WORK_DIR="$(mktemp -d)"
trap 'rm -rf "${WORK_DIR}"' EXIT
APPDIR="${WORK_DIR}/PlayWall.AppDir"

fetch() { # <url> <sha256> <dest>
  curl -fsSL --retry 3 -o "$3" "$1"
  echo "$2  $3" | sha256sum -c -
}

echo "==> Downloading pinned appimagetool ${APPIMAGETOOL_VERSION} and type2-runtime ${RUNTIME_VERSION} (${ARCH})"
fetch "https://github.com/AppImage/appimagetool/releases/download/${APPIMAGETOOL_VERSION}/appimagetool-${ARCH}.AppImage" \
  "${APPIMAGETOOL_SHA256}" "${WORK_DIR}/appimagetool"
fetch "https://github.com/AppImage/type2-runtime/releases/download/${RUNTIME_VERSION}/runtime-${ARCH}" \
  "${RUNTIME_SHA256}" "${WORK_DIR}/runtime-${ARCH}"
chmod +x "${WORK_DIR}/appimagetool"

echo "==> Assembling AppDir"
mkdir -p "${APPDIR}/usr"
tar -xzf "${TARBALL}" -C "${APPDIR}/usr"        # contains a single top-level PlayWall/ directory
test -x "${APPDIR}/usr/PlayWall/bin/PlayWall"

install -m 755 "${SCRIPT_DIR}/AppRun" "${APPDIR}/AppRun"
install -m 644 "${SCRIPT_DIR}/playwall.desktop" "${APPDIR}/playwall.desktop"
install -m 644 "${ICON}" "${APPDIR}/playwall.png"
cp "${APPDIR}/playwall.png" "${APPDIR}/.DirIcon"

echo "==> Building AppImage"
# APPIMAGE_EXTRACT_AND_RUN: appimagetool is itself an AppImage; avoid needing FUSE on the build machine.
mkdir -p "$(dirname "${OUTPUT}")"
ARCH="${ARCH}" APPIMAGE_EXTRACT_AND_RUN=1 "${WORK_DIR}/appimagetool" \
  --runtime-file "${WORK_DIR}/runtime-${ARCH}" \
  --no-appstream \
  "${APPDIR}" "${OUTPUT}"

chmod +x "${OUTPUT}"
ls -l "${OUTPUT}"
