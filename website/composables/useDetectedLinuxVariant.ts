/**
 * Suggests the Linux download variant that most likely fits the visitor's machine.
 *
 * The browser only reveals so much: the CPU architecture (user agent, or Client Hints in Chromium) and, for some
 * browsers, a distribution token such as "Ubuntu" or "Fedora". It does NOT identify Arch-based systems (e.g. CachyOS),
 * which show up as plain "Linux x86_64". So: an x86_64 user agent without a known non-Arch distribution token is
 * assumed to be Arch-based and gets the AppImage. This is only a preselection; the visitor can always change it.
 */
const NON_ARCH_DISTRO = /Ubuntu|Debian|Fedora|SUSE|Mint|Red Hat|CentOS|Rocky|Alma/i

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

        const x86Variant: PlatformId = NON_ARCH_DISTRO.test(ua) ? 'linux-amd64' : 'linux-amd64-appimage'
        variant.value = /aarch64|arm64|armv8/i.test(ua) ? 'linux-arm64' : x86Variant

        // Chromium freezes the platform in its user agent to "x86_64", Client Hints still report the real architecture.
        const uaData = (navigator as Navigator & { userAgentData?: UserAgentDataLike }).userAgentData
        try {
            const {architecture} = (await uaData?.getHighEntropyValues(['architecture'])) ?? {}
            if (architecture === 'arm') {
                variant.value = 'linux-arm64'
            } else if (architecture === 'x86') {
                variant.value = x86Variant
            }
        } catch {
            // Client Hints unavailable (insecure context, denied by policy): keep the user agent based guess
        }
    })

    return variant
}
