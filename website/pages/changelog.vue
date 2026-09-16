<script setup lang="ts">
const {t} = useI18n()
useHead({title: t('changelog.title')})
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
        <h2 class="font-mono text-xl font-semibold text-pw-text">{{ entry.version }}</h2>

        <div v-if="entry.features.length" class="mt-4">
          <p class="font-mono text-xs uppercase tracking-wide text-pw-text-muted">{{ t('changelog.featuresLabel') }}</p>
          <ul class="mt-2 flex flex-col gap-1.5">
            <li v-for="item in entry.features" :key="item" class="flex items-start gap-2.5 text-sm text-pw-text-hint">
              <span class="mt-[7px] h-1.5 w-1.5 flex-none rounded-full bg-pw-success"/>
              <span>{{ item }}</span>
            </li>
          </ul>
        </div>

        <div v-if="entry.bugfixes.length" class="mt-4">
          <p class="font-mono text-xs uppercase tracking-wide text-pw-text-muted">{{ t('changelog.bugfixesLabel') }}</p>
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
