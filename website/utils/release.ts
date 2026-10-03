import releaseData from '../assets/release.json'

export type PlatformId = 'windows-amd64' | 'macos-arm64' | 'linux-amd64' | 'linux-arm64'

export interface ReleaseInfo {
    version: string
    platforms: Record<PlatformId, string>
    companion: string | null
}

export const release: ReleaseInfo = releaseData as ReleaseInfo

export const PLATFORM_ORDER: PlatformId[] = ['windows-amd64', 'macos-arm64', 'linux-amd64', 'linux-arm64']

export type SystemId = 'windows' | 'macos' | 'linux'

export const SYSTEM_PLATFORMS: Record<SystemId, [PlatformId, ...PlatformId[]]> = {
    windows: ['windows-amd64'],
    macos: ['macos-arm64'],
    linux: ['linux-amd64', 'linux-arm64'],
}

export const SYSTEM_ORDER: SystemId[] = ['windows', 'macos', 'linux']
