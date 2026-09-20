<script setup lang="ts">
const {t, locale} = useI18n()
useHead({title: t('changelog.title')})
useSeoMeta({
  description: t('changelog.subtitle'),
  ogTitle: t('changelog.title'),
  ogDescription: t('changelog.subtitle'),
  twitterTitle: t('changelog.title'),
  twitterDescription: t('changelog.subtitle'),
})

function formatDate(dateStr: string | null): string | null {
  if (!dateStr) return null
  // Lokal parsen statt new Date(dateStr) direkt zu übergeben: Ein reines
  // Datum ("2026-09-14") würde sonst als UTC-Mitternacht interpretiert und
  // könnte in Zeitzonen westlich von UTC auf den Vortag zurückfallen.
  const [year, month, day] = dateStr.split('-').map(Number)
  if (!year || !month || !day) return null
  const date = new Date(year, month - 1, day)
  return new Intl.DateTimeFormat(locale.value === 'de' ? 'de-DE' : 'en-US', {dateStyle: 'long'}).format(date)
}
</script>

<template>
  <div>
    <section class="mx-auto max-w-3xl px-6 pb-4 pt-28 text-center">
      <h1 class="font-display text-4xl font-semibold text-pw-text sm:text-5xl">{{ t('changelog.title') }}</h1>
      <p class="mt-3 text-lg text-pw-text-hint">{{ t('changelog.subtitle') }}</p>
    </section>

    <section class="mx-auto max-w-2xl px-6 pb-24">
      <div
          v-for="entry in changelogVersions"
          :key="entry.version"
          class="border-b border-white/10 py-8 first:pt-0 last:border-none"
      >
        <div class="flex flex-wrap items-baseline justify-between gap-x-3 gap-y-1">
          <h2 class="font-mono text-xl font-semibold text-pw-text">{{ entry.version }}</h2>
          <p v-if="formatDate(entry.date)" class="font-mono text-xs text-pw-text-hint">{{ formatDate(entry.date) }}</p>
        </div>

        <div v-if="entry.features.length" class="mt-4">
          <p class="font-mono text-xs uppercase tracking-wide text-pw-text-hint">{{ t('changelog.featuresLabel') }}</p>
          <ul class="mt-2 flex flex-col gap-1.5">
            <li v-for="item in entry.features" :key="item" class="flex items-start gap-2.5 text-sm text-pw-text-hint">
              <span class="mt-[7px] h-1.5 w-1.5 flex-none rounded-full bg-pw-success"/>
              <span>{{ item }}</span>
            </li>
          </ul>
        </div>

        <div v-if="entry.bugfixes.length" class="mt-4">
          <p class="font-mono text-xs uppercase tracking-wide text-pw-text-hint">{{ t('changelog.bugfixesLabel') }}</p>
          <ul class="mt-2 flex flex-col gap-1.5">
            <li v-for="item in entry.bugfixes" :key="item" class="flex items-start gap-2.5 text-sm text-pw-text-hint">
              <span class="mt-[7px] h-1.5 w-1.5 flex-none rounded-full bg-pw-warning"/>
              <span>{{ item }}</span>
            </li>
          </ul>
        </div>
      </div>
    </section>
  </div>
</template>
