#!/usr/bin/env bash
#
# Wraps the jpackage Linux app image (PlayWall-<ver>.amd64.tar.gz) into an AppImage.
#
# System libraries (GTK3, Mesa, ALSA/PipeWire, X11, ...) are NOT bundled; they come from the host.
# Only the self-contained jpackage app image (own JDK, JavaFX, Rust audio lib) is packaged.
#
# Usage:  build-appimage.sh <PlayWall-x.y.z.amd64.tar.gz> <output.AppImage>
# Needs:  bash, curl, tar, sha256sum (Linux x86_64)
set -euo pipefail

TARBALL="${1:?usage: $0 <app-image-tarball> <output.AppImage>}"
OUTPUT="${2:?usage: $0 <app-image-tarball> <output.AppImage>}"

# Pinned tool versions (update together with the checksums).
APPIMAGETOOL_VERSION="1.9.1"
APPIMAGETOOL_SHA256="ed4ce84f0d9caff66f50bcca6ff6f35aae54ce8135408b3fa33abfc3cb384eb0"
RUNTIME_VERSION="20251108"
RUNTIME_SHA256="2fca8b443c92510f1483a883f60061ad09b46b978b2631c807cd873a47ec260d"

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

echo "==> Downloading pinned appimagetool ${APPIMAGETOOL_VERSION} and type2-runtime ${RUNTIME_VERSION}"
fetch "https://github.com/AppImage/appimagetool/releases/download/${APPIMAGETOOL_VERSION}/appimagetool-x86_64.AppImage" \
  "${APPIMAGETOOL_SHA256}" "${WORK_DIR}/appimagetool"
fetch "https://github.com/AppImage/type2-runtime/releases/download/${RUNTIME_VERSION}/runtime-x86_64" \
  "${RUNTIME_SHA256}" "${WORK_DIR}/runtime-x86_64"
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
ARCH=x86_64 APPIMAGE_EXTRACT_AND_RUN=1 "${WORK_DIR}/appimagetool" \
  --runtime-file "${WORK_DIR}/runtime-x86_64" \
  --no-appstream \
  "${APPDIR}" "${OUTPUT}"

chmod +x "${OUTPUT}"
ls -l "${OUTPUT}"
