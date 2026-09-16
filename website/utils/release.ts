import releaseData from '../assets/release.json'

// assets/release.json wird von scripts/fetch-release-info.mjs erzeugt (siehe
// package.json "pre*"-Hooks) und nicht eingecheckt. Existiert die Datei
// nicht, ist noch keiner der "pre*"-Hooks gelaufen — siehe README/Konzept.

export type PlatformId = 'windows-amd64' | 'macos-arm64' | 'linux-amd64' | 'linux-arm64'

export interface ReleaseInfo {
    version: string
    platforms: Record<PlatformId, string>
}

export const release: ReleaseInfo = releaseData as ReleaseInfo

export const PLATFORM_ORDER: PlatformId[] = ['windows-amd64', 'macos-arm64', 'linux-amd64', 'linux-arm64']
