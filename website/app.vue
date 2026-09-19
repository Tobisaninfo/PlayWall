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

useSeoMeta({
  ogSiteName: 'PlayWall',
  ogType: 'website',
  ogImage: 'https://playwall.thecodelabs.de/og-image.png',
  ogImageWidth: 1200,
  ogImageHeight: 630,
  ogImageAlt: 'PlayWall',
  twitterCard: 'summary_large_image',
  twitterImage: 'https://playwall.thecodelabs.de/og-image.png',
})
</script>

<template>
  <NuxtLayout>
    <NuxtPage/>
  </NuxtLayout>
</template>
