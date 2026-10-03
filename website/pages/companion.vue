<script setup lang="ts">
const {t} = useI18n()
usePageSeo('companion.title', 'companion.subtitle')

const list = (key: string) => t(key).split('\n')

const sections = computed(() => [
  {heading: t('companion.featuresHeading'), items: list('companion.features')},
  {heading: t('companion.connectionHeading'), items: list('companion.connection')},
  {heading: t('companion.presetsHeading'), items: list('companion.presets')},
])
const steps = computed(() => list('companion.steps'))
const variables = computed(() => list('companion.variables').map((line) => {
  const [name = '', description = ''] = line.split(' … ')
  return {name, description}
}))
</script>

<template>
  <div>
    <section class="mx-auto max-w-3xl px-6 pb-4 pt-28 text-center">
      <h1 class="font-display text-4xl font-semibold text-pw-text sm:text-5xl">{{ t('companion.title') }}</h1>
      <p class="mt-3 text-lg text-pw-text-hint">{{ t('companion.subtitle') }}</p>
    </section>

    <section class="mx-auto max-w-3xl px-6 pb-24">
      <div class="mt-8 flex flex-col gap-4 rounded-xl border border-white/40 bg-white/[0.02] p-5">
        <h2 class="font-display text-lg font-semibold text-pw-text">{{ t('companion.downloadHeading') }}</h2>
        <a
            v-if="release.companion"
            :href="release.companion"
            download
            class="flex items-center gap-2.5 rounded-md bg-pw-blue-strong px-4 py-2.5 text-sm font-medium text-white transition-opacity hover:opacity-90"
        >
          <DownloadIcon class="h-5 w-5 flex-none"/>
          <span class="flex flex-1 flex-col items-start justify-center">
            <span>{{ t('companion.downloadCta') }}</span>
            <span class="text-xs font-normal text-white/75">{{ t('companion.downloadFile', {version: release.version}) }}</span>
          </span>
        </a>
        <p v-else class="text-sm text-pw-text-hint">{{ t('companion.unavailable', {version: release.version}) }}</p>
        <p class="text-sm text-pw-text-muted">{{ t('companion.versionNote') }}</p>
      </div>

      <div class="mt-12">
        <h2 class="font-display text-2xl font-semibold text-pw-text">{{ t('companion.setupHeading') }}</h2>
        <ol class="mt-4 flex flex-col gap-3">
          <li v-for="(step, index) in steps" :key="step" class="flex items-start gap-3 text-pw-text-hint">
            <span class="flex h-6 w-6 flex-none items-center justify-center rounded-full bg-pw-blue-strong font-mono text-xs text-white">{{ index + 1 }}</span>
            <span>{{ step }}</span>
          </li>
        </ol>
      </div>

      <div v-for="section in sections" :key="section.heading" class="mt-12">
        <h2 class="font-display text-2xl font-semibold text-pw-text">{{ section.heading }}</h2>
        <ul class="mt-4 flex flex-col gap-2">
          <li v-for="item in section.items" :key="item" class="flex items-start gap-2 text-pw-text-hint">
            <span class="mt-[9px] h-1.5 w-1.5 flex-none rounded-full bg-pw-blue"/>
            <span>{{ item }}</span>
          </li>
        </ul>
      </div>

      <div class="mt-12">
        <h2 class="font-display text-2xl font-semibold text-pw-text">{{ t('companion.variablesHeading') }}</h2>
        <dl class="mt-4 flex flex-col gap-3">
          <div v-for="variable in variables" :key="variable.name" class="flex flex-col gap-0.5">
            <dt class="font-mono text-sm text-pw-text">{{ variable.name }}</dt>
            <dd class="text-sm text-pw-text-hint">{{ variable.description }}</dd>
          </div>
        </dl>
      </div>
    </section>
  </div>
</template>
