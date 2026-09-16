import tailwindcss from '@tailwindcss/vite'

// Klassische Root-Struktur (pages/, components/ auf website/-Ebene) statt
// Nuxt 4s neuem app/-Unterordner, damit sie zur im Konzept dokumentierten
// Ordnerstruktur passt.
export default defineNuxtConfig({
    compatibilityDate: '2026-01-01',
    srcDir: '.',

    modules: [
        '@nuxtjs/i18n',
        '@nuxt/fonts',
        '@vueuse/nuxt',
    ],

    css: ['~/assets/css/main.css'],

    vite: {
        plugins: [tailwindcss()],
    },

    // Betrieb ausschließlich als vorgerendertes statisches Image (siehe Konzept
    // "Warum Nuxt": SPA-Verhalten im Browser, echtes HTML für Crawler/Social-Previews).
    ssr: true,
    nitro: {
        prerender: {
            crawlLinks: true,
            failOnError: true,
        },
    },

    fonts: {
        families: [
            {name: 'Space Grotesk', provider: 'google', weights: [500, 600, 700]},
            {name: 'IBM Plex Sans', provider: 'google', weights: [400, 500, 600]},
            {name: 'IBM Plex Mono', provider: 'google', weights: [400, 500]},
        ],
    },

    i18n: {
        baseUrl: 'https://playwall.thecodelabs.de',
        bundle: {
            optimizeTranslationDirective: false,
        },
        locales: [
            {code: 'de', language: 'de-DE', name: 'Deutsch', file: 'de.json'},
            {code: 'en', language: 'en-US', name: 'English', file: 'en.json'},
        ],
        defaultLocale: 'de',
        strategy: 'prefix',
        detectBrowserLanguage: {
            useCookie: true,
            cookieKey: 'playwall_i18n',
            redirectOn: 'root',
        },
    },

    devtools: {enabled: true},
})
