// Rein kosmetische Hervorhebung des eigenen Systems auf der Download-Seite
// (PW-203-Konzept: "nichts wird blockiert"). Läuft nur im Browser, da SSG
// beim Prerendern kein User-Agent zur Verfügung hat. Erkennt nur das
// Betriebssystem, nicht die Architektur — Linux amd64 vs. arm64 lässt sich
// aus dem User-Agent ohnehin nicht zuverlässig ableiten, und beide werden
// jetzt gemeinsam in einer Spalte angeboten.
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
