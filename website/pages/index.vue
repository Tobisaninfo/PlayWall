<script setup lang="ts">
const {t} = useI18n()
const localePath = useLocalePath()

usePageSeo('home.title', 'home.subtitle')
</script>

<template>
  <section class="relative isolate flex min-h-[92vh] flex-col overflow-hidden">
    <!-- Übergangsweise doc/screenshots/pad-grid.png (ausgeliefert über
         nitro.publicAssets, siehe nuxt.config.ts — keine Kopie hier). Wird
         durch ein eigens für die Website erstelltes /hero.png ersetzt,
         sobald verfügbar; das liegt dann in website/public/, nicht in doc/. -->
    <img
        src="/screenshots/pad-grid.png"
        :alt="t('home.heroAlt')"
        width="1680"
        height="1152"
        loading="eager"
        fetchpriority="high"
        class="absolute inset-0 h-full w-full object-cover object-top"
    >

    <!-- Glossy Lichtreflexe, dezent -->
    <div class="pointer-events-none absolute -top-40 left-[8%] h-[46rem] w-[46rem] rounded-full bg-white/10 blur-3xl"></div>
    <div class="pointer-events-none absolute top-1/3 right-[4%] h-[34rem] w-[34rem] rounded-full bg-pw-blue/25 blur-3xl"></div>

    <!-- Lesbarkeits-Verlauf: dunkel oben (unter der Nav) und unten, klar in der Mitte -->
    <div class="absolute inset-0 bg-gradient-to-b from-pw-bg/85 via-pw-bg/25 to-pw-bg"></div>

    <!-- Inhalt -->
    <div class="relative z-10 flex flex-1 flex-col items-center justify-center px-6 pb-16 pt-24 text-center">
      <div
          class="flex flex-col items-center gap-6 rounded-2xl border border-white/15 bg-pw-bg/80 px-8 py-10 shadow-2xl ring-1 ring-white/5 backdrop-blur-xl sm:px-14 sm:py-12"
          style="box-shadow: inset 0 1px 0 rgba(255,255,255,0.15), 0 30px 60px -12px rgba(0,0,0,0.65)"
      >
        <img src="/logo-mark.png" alt="" width="256" height="256" class="h-16 w-16 drop-shadow-lg">

        <div class="flex flex-col gap-2">
          <h1 class="text-balance font-display text-5xl font-bold tracking-tight text-white sm:text-6xl">
            PlayWall
          </h1>
          <p class="text-balance font-display text-lg font-medium text-white/90 sm:text-xl">
            {{ t('home.title') }}
          </p>
        </div>

        <p class="max-w-md text-pretty text-pw-text-hint">
          {{ t('home.subtitle') }}
        </p>

        <div class="mt-2 flex flex-col items-center justify-center gap-3 sm:flex-row">
          <NuxtLink
              :to="localePath('download')"
              class="inline-flex items-center gap-2 rounded-md bg-pw-blue-strong px-5 py-2.5 text-sm font-medium text-white transition-opacity hover:opacity-90"
          >
            <DownloadIcon class="h-4 w-4 flex-none"/>
            {{ t('home.cta_download') }}
          </NuxtLink>
          <a
              :href="GITHUB_URL"
              target="_blank"
              rel="noopener noreferrer"
              class="inline-flex items-center gap-2 rounded-md border border-white/20 bg-white/5 px-5 py-2.5 text-sm font-medium text-white transition-colors hover:border-pw-blue hover:bg-white/10"
          >
            <GithubIcon class="h-4 w-4 flex-none"/>
            {{ t('home.cta_github') }}
          </a>
        </div>
      </div>
    </div>
  </section>
</template>
