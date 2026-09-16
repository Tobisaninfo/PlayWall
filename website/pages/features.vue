<script setup lang="ts">
const {t} = useI18n()
useHead({title: t('features.title')})
</script>

<template>
  <section class="mx-auto max-w-3xl px-6 pb-16 pt-28">
    <h1 class="font-display text-3xl font-semibold text-pw-text">{{ t('features.title') }}</h1>
    <p class="mt-2 text-pw-text-hint">{{ t('features.subtitle') }}</p>
    <p class="mt-8 rounded-md border border-dashed border-pw-panel-hover px-4 py-3 font-mono text-sm text-pw-text-muted">
      {{ t('features.placeholder') }}
    </p>
  </section>
</template>
