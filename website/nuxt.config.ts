import {fileURLToPath} from 'node:url'
import tailwindcss from '@tailwindcss/vite'

const SITE_URL = 'https://playwall.thecodelabs.de'

export default defineNuxtConfig({
    compatibilityDate: '2026-01-01',
    srcDir: '.',

    runtimeConfig: {
        public: {
            siteUrl: SITE_URL,
        },
    },

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

    ssr: true,
    nitro: {
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
        baseUrl: SITE_URL,
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
