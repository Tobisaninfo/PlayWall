/**
 * Suggests the Linux download variant that most likely fits the visitor's machine: the AppImage (a single file that
 * works on any distribution) for the detected CPU architecture. The tar.gz stays available in the dropdown.
 *
 * The architecture comes from the user agent or, for Chromium (which freezes the platform to "x86_64"), from Client
 * Hints. A browser cannot tell the distribution, so there is no distribution specific suggestion.
 */
type UserAgentDataLike = {
    getHighEntropyValues(hints: string[]): Promise<{ architecture?: string }>
}

export function useDetectedLinuxVariant() {
    const variant = ref<PlatformId | null>(null)

    onMounted(async () => {
        const ua = navigator.userAgent
        if (!/Linux/i.test(ua) || /Android/i.test(ua)) {
            return
        }

        variant.value = /aarch64|arm64|armv8/i.test(ua) ? 'linux-arm64-appimage' : 'linux-amd64-appimage'

        const uaData = (navigator as Navigator & { userAgentData?: UserAgentDataLike }).userAgentData
        try {
            const {architecture} = (await uaData?.getHighEntropyValues(['architecture'])) ?? {}
            if (architecture === 'arm') {
                variant.value = 'linux-arm64-appimage'
            } else if (architecture === 'x86') {
                variant.value = 'linux-amd64-appimage'
            }
        } catch {
            // Client Hints unavailable (insecure context, denied by policy): keep the user agent based guess
        }
    })

    return variant
}
