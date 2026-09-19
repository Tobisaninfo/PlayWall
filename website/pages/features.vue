<script setup lang="ts">
const {t, locale} = useI18n()
useHead({title: t('features.title')})
useSeoMeta({
  description: t('features.subtitle'),
  ogTitle: t('features.title'),
  ogDescription: t('features.subtitle'),
  twitterTitle: t('features.title'),
  twitterDescription: t('features.subtitle'),
})

const clusters = computed(() => localizedFeatureClusters(locale.value as FeatureLocale))
</script>

<template>
  <div>
    <section class="mx-auto max-w-3xl px-6 pb-4 pt-28 text-center">
      <h1 class="font-display text-4xl font-semibold text-pw-text sm:text-5xl">{{ t('features.title') }}</h1>
      <p class="mt-3 text-lg text-pw-text-hint">{{ t('features.subtitle') }}</p>
    </section>

    <section class="mx-auto max-w-5xl divide-y divide-white/10 px-6 pb-24">
      <FeatureCluster
          v-for="(cluster, index) in clusters"
          :id="cluster.id"
          :key="cluster.id"
          :title="cluster.title"
          :description="cluster.description"
          :features="cluster.features"
          :screenshot="cluster.screenshot"
          :index="index"
      />
    </section>
  </div>
</template>
