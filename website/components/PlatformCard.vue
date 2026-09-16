<script setup lang="ts">
defineProps<{
  id: PlatformId
  url: string
  highlighted: boolean
}>()

const {t} = useI18n()
</script>

<template>
  <div
      class="flex flex-col gap-3 rounded-xl border p-5 transition-colors"
      :class="highlighted ? 'border-pw-blue bg-pw-blue/5' : 'border-white/10 bg-white/[0.02]'"
  >
    <div class="flex items-start justify-between gap-3">
      <div>
        <p class="font-display text-lg font-semibold text-pw-text">{{ t(`download.platforms.${id}.name`) }}</p>
        <p class="font-mono text-xs uppercase tracking-wide text-pw-text-muted">{{ t(`download.platforms.${id}.arch`) }}</p>
      </div>
      <span
          v-if="highlighted"
          class="whitespace-nowrap rounded-full bg-pw-blue/15 px-2.5 py-1 font-mono text-[11px] uppercase tracking-wide text-pw-blue"
      >
        {{ t('download.recommended') }}
      </span>
    </div>

    <p class="text-sm text-pw-text-hint">{{ t(`download.platforms.${id}.requirements`) }}</p>
    <p class="font-mono text-xs text-pw-text-muted">{{ t(`download.platforms.${id}.fileType`) }}</p>

    <a
        :href="url"
        class="mt-2 inline-flex items-center justify-center rounded-md bg-pw-blue px-4 py-2.5 text-sm font-medium text-white transition-opacity hover:opacity-90"
    >
      {{ t('download.downloadCta') }}
    </a>
  </div>
</template>
