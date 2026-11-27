export type LinuxDistroId = 'arch' | 'apt' | 'fedora'

export const LINUX_DISTRO_ORDER: LinuxDistroId[] = ['arch', 'apt', 'fedora']

/**
 * Commands that install the system libraries PlayWall needs on Linux (GTK 3, OpenGL, ALSA, X11, fonts). They are not
 * bundled in the AppImage or the tarball. Keep in sync with the package lists in README.md.
 * Package names can differ between releases (see the distribution notes in the i18n files).
 */
export const LINUX_LIBRARY_INSTALL_COMMANDS: Record<LinuxDistroId, string> = {
    arch: 'sudo pacman -S gtk3 alsa-lib pipewire-alsa freetype2 fontconfig mesa libxtst',
    apt: 'sudo apt install libgtk-3-0t64 libasound2t64 libfreetype6 libfontconfig1 libgl1 libxtst6 libxi6 libxrender1',
    fedora: 'sudo dnf install gtk3 alsa-lib freetype fontconfig mesa-libGL libXtst libXi libXrender',
}

export type LinuxPackageKind = 'appimage' | 'archive'

export function linuxPackageKind(platformId: PlatformId): LinuxPackageKind {
    return platformId.endsWith('-appimage') ? 'appimage' : 'archive'
}

/** File name of a download URL, e.g. "PlayWallClient-8.4.0-installer.x86_64.AppImage". */
export function downloadFileName(url: string): string {
    return url.split('/').pop() ?? url
}

/** Commands to start PlayWall after downloading, depending on the package kind. */
export function linuxRunCommands(kind: LinuxPackageKind, fileName: string): string {
    return kind === 'appimage'
        ? `chmod +x ${fileName}\n./${fileName}`
        : `tar xzf ${fileName}\n./PlayWall/bin/PlayWall`
}

/** Fallback for AppImages on systems without FUSE. */
export function linuxAppImageNoFuseCommand(fileName: string): string {
    return `./${fileName} --appimage-extract-and-run`
}
