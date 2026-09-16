// Rein kosmetische Hervorhebung der eigenen Plattform auf der Download-Seite
// (PW-203-Konzept: "nichts wird blockiert"). Läuft nur im Browser, da SSG
// beim Prerendern kein User-Agent zur Verfügung hat.
export function useDetectedPlatform() {
    const detected = ref<PlatformId | null>(null)

    onMounted(() => {
        const ua = navigator.userAgent

        if (/Windows/i.test(ua)) {
            detected.value = 'windows-amd64'
        } else if (/Macintosh|Mac OS X/i.test(ua)) {
            // Architektur (Apple Silicon vs. Intel) lässt sich aus dem UA nicht
            // zuverlässig ableiten — Apple Silicon ist der einzig unterstützte Mac.
            detected.value = 'macos-arm64'
        } else if (/Linux/i.test(ua) && !/Android/i.test(ua)) {
            // arm64 vs. amd64 ebenfalls nicht zuverlässig erkennbar; amd64 ist
            // der deutlich häufigere Fall auf Desktop-Linux.
            detected.value = 'linux-amd64'
        }
    })

    return detected
}
