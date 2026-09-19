import {fileURLToPath} from 'node:url'
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

    app: {
        head: {
            link: [
                {rel: 'icon', type: 'image/x-icon', href: '/favicon.ico'},
                {rel: 'icon', type: 'image/png', sizes: '32x32', href: '/favicon.png'},
                {rel: 'apple-touch-icon', sizes: '180x180', href: '/apple-touch-icon.png'},
            ],
        },
    },

    vite: {
        plugins: [tailwindcss()],
    },

    // Betrieb ausschließlich als vorgerendertes statisches Image (siehe Konzept
    // "Warum Nuxt": SPA-Verhalten im Browser, echtes HTML für Crawler/Social-Previews).
    ssr: true,
    nitro: {
        // Screenshots werden direkt aus doc/screenshots ausgeliefert statt nach
        // website/public/ kopiert zu werden — eine Quelle statt Doppelpflege.
        // Betrifft nur den Dev-Server/Build; das generierte .output/public/
        // enthält die Dateien danach wie gewohnt als reine statische Assets.
        publicAssets: [
            {
                baseURL: '/screenshots',
                dir: fileURLToPath(new URL('../doc/screenshots', import.meta.url)),
                maxAge: 60 * 60 * 24 * 7,
            },
        ],
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
