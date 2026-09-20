export function useDetectedPlatform() {
    const detected = ref<SystemId | null>(null)

    onMounted(() => {
        const ua = navigator.userAgent

        if (/Windows/i.test(ua)) {
            detected.value = 'windows'
        } else if (/Macintosh|Mac OS X/i.test(ua)) {
            detected.value = 'macos'
        } else if (/Linux/i.test(ua) && !/Android/i.test(ua)) {
            detected.value = 'linux'
        }
    })

    return detected
}
