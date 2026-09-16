<script setup lang="ts">
const {t} = useI18n()
useHead({title: t('download.title')})

const detected = useDetectedPlatform()
</script>

<template>
  <div>
    <section class="mx-auto max-w-3xl px-6 pb-4 pt-28 text-center">
      <h1 class="font-display text-4xl font-semibold text-pw-text sm:text-5xl">{{ t('download.title') }}</h1>
      <p class="mt-3 text-lg text-pw-text-hint">{{ t('download.subtitle') }}</p>
      <p class="mt-4 font-mono text-sm text-pw-text-muted">
        {{ t('download.currentVersion') }}: <span class="text-pw-text">{{ release.version }}</span>
      </p>
    </section>

    <section class="mx-auto max-w-4xl px-6 pb-24">
      <div class="grid gap-4 sm:grid-cols-2">
        <PlatformCard
            v-for="platformId in PLATFORM_ORDER"
            :id="platformId"
            :key="platformId"
            :url="release.platforms[platformId]"
            :highlighted="detected === platformId"
        />
      </div>
      <p class="mt-8 text-center text-sm text-pw-text-muted">{{ t('download.linuxInstructions') }}</p>
    </section>
  </div>
</template>
