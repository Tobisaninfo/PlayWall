<script setup lang="ts">
import type {Directions} from '@nuxtjs/i18n'

// Setzt <html lang>, hreflang-Alternates und SEO-Meta-Tags pro Sprache
// automatisch — lang/dir/seo sind alle standardmäßig aktiv.
// Teil der im Konzept begründeten Nuxt-Entscheidung ("Warum Nuxt").
const i18nHead = useLocaleHead()

useHead(() => ({
  htmlAttrs: {
    lang: i18nHead.value.htmlAttrs?.lang,
    dir: i18nHead.value.htmlAttrs?.dir as Directions | undefined,
  },
  link: [...(i18nHead.value.link || [])],
  meta: [...(i18nHead.value.meta || [])],
  titleTemplate: (title) => (title ? `${title} · PlayWall` : 'PlayWall'),
}))

// Seitenweite OG/Twitter-Defaults, damit geteilte Links (Social-Preview,
// Messenger) immer ein Bild und eine Beschreibung zeigen. og:title/
// og:description/twitter:title/twitter:description werden pro Seite
// überschrieben (siehe usePageSeo in den Pages).
const ogImageUrl = `${useRuntimeConfig().public.siteUrl}/og-image.png`

useSeoMeta({
  ogSiteName: 'PlayWall',
  ogType: 'website',
  ogImage: ogImageUrl,
  ogImageWidth: 1200,
  ogImageHeight: 630,
  ogImageAlt: 'PlayWall',
  twitterCard: 'summary_large_image',
  twitterImage: ogImageUrl,
})
</script>

<template>
  <NuxtLayout>
    <NuxtPage/>
  </NuxtLayout>
</template>
