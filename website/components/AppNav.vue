<script setup lang="ts">
const {t, locale} = useI18n()
const localePath = useLocalePath()
const switchLocalePath = useSwitchLocalePath()
const route = useRoute()

const otherLocaleCode = computed(() => (locale.value === 'de' ? 'en' : 'de'))

const navLinks = computed(() => [
  {to: localePath('index'), label: t('nav.home')},
  {to: localePath('features'), label: t('nav.features')},
  {to: localePath('download'), label: t('nav.download')},
  {to: localePath('companion'), label: t('nav.companion')},
  {to: localePath('changelog'), label: t('nav.changelog')},
])

const isMobileMenuOpen = ref(false)
watch(() => route.fullPath, () => {
  isMobileMenuOpen.value = false
})
</script>

<template>
  <header class="fixed inset-x-0 top-0 z-50 border-b border-white/10 bg-pw-bg/70 backdrop-blur-md">
    <div class="mx-auto flex h-16 max-w-5xl items-center gap-6 px-6">
      <NuxtLink :to="localePath('index')" class="flex items-center gap-2">
        <img src="/logo.png" alt="" width="256" height="256" class="h-7 w-7">
        <span class="font-display text-lg font-semibold tracking-tight text-pw-text">PlayWall</span>
      </NuxtLink>

      <nav class="hidden flex-1 items-center gap-5 sm:flex">
        <NuxtLink
            v-for="link in navLinks"
            :key="link.to"
            :to="link.to"
            class="text-sm text-pw-text-hint transition-colors hover:text-pw-text"
            active-class="text-pw-text font-medium"
        >
          {{ link.label }}
        </NuxtLink>
      </nav>

      <a
          :href="GITHUB_URL"
          target="_blank"
          rel="noopener noreferrer"
          class="hidden items-center gap-1.5 text-sm text-pw-text-hint transition-colors hover:text-pw-text sm:flex"
      >
        <GithubIcon class="h-4 w-4 flex-none"/>
        {{ t('nav.github') }}
      </a>

      <NuxtLink
          :to="switchLocalePath(otherLocaleCode)"
          class="hidden rounded-md border border-pw-panel-hover px-2.5 py-1 font-mono text-xs uppercase tracking-wide text-pw-text-hint transition-colors hover:border-pw-blue hover:text-pw-text sm:block"
      >
        {{ t('nav.languageSwitch') }}
      </NuxtLink>

      <button
          type="button"
          class="ml-auto flex h-9 w-9 flex-col items-center justify-center gap-1.5 rounded-md sm:hidden"
          :aria-expanded="isMobileMenuOpen"
          :aria-label="t('nav.menuToggle')"
          @click="isMobileMenuOpen = !isMobileMenuOpen"
      >
        <span
            class="h-0.5 w-5 rounded-full bg-pw-text transition-transform"
            :class="isMobileMenuOpen ? 'translate-y-2 rotate-45' : ''"
        />
        <span
            class="h-0.5 w-5 rounded-full bg-pw-text transition-opacity"
            :class="isMobileMenuOpen ? 'opacity-0' : ''"
        />
        <span
            class="h-0.5 w-5 rounded-full bg-pw-text transition-transform"
            :class="isMobileMenuOpen ? '-translate-y-2 -rotate-45' : ''"
        />
      </button>
    </div>

    <Transition
        enter-active-class="transition-[opacity,transform] duration-150 ease-out"
        enter-from-class="opacity-0 -translate-y-1"
        leave-active-class="transition-[opacity,transform] duration-100 ease-in"
        leave-to-class="opacity-0 -translate-y-1"
    >
      <nav
          v-if="isMobileMenuOpen"
          class="flex flex-col gap-1 border-t border-white/10 bg-pw-bg/90 px-6 py-4 sm:hidden"
      >
        <NuxtLink
            v-for="link in navLinks"
            :key="link.to"
            :to="link.to"
            class="rounded-md px-2 py-2 text-sm text-pw-text-hint transition-colors hover:bg-white/5 hover:text-pw-text"
            active-class="text-pw-text font-medium"
        >
          {{ link.label }}
        </NuxtLink>
        <a
            :href="GITHUB_URL"
            target="_blank"
            rel="noopener noreferrer"
            class="flex items-center gap-2 rounded-md px-2 py-2 text-sm text-pw-text-hint transition-colors hover:bg-white/5 hover:text-pw-text"
        >
          <GithubIcon class="h-4 w-4 flex-none"/>
          {{ t('nav.github') }}
        </a>
        <NuxtLink
            :to="switchLocalePath(otherLocaleCode)"
            class="mt-1 rounded-md px-2 py-2 font-mono text-xs uppercase tracking-wide text-pw-text-hint transition-colors hover:bg-white/5 hover:text-pw-text"
        >
          {{ t('nav.languageSwitch') }}
        </NuxtLink>
      </nav>
    </Transition>
  </header>
</template>
