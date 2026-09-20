<script setup lang="ts">
const {t} = useI18n()
usePageSeo('privacy.title', 'privacy.description')
</script>

<template>
  <section class="mx-auto max-w-3xl px-6 pb-16 pt-28">
    <h1 class="font-display text-3xl font-semibold text-pw-text">{{ t('privacy.title') }}</h1>
    <p class="mt-8 rounded-md border border-dashed border-pw-panel-hover px-4 py-3 font-mono text-sm text-pw-text-muted">
      {{ t('privacy.placeholder') }}
    </p>
  </section>
</template>
