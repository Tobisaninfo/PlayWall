<script setup lang="ts">
const {t} = useI18n()
const localePath = useLocalePath()

const GITHUB_URL = 'https://github.com/Tobisaninfo/PlayWall'
const year = new Date().getFullYear()
</script>

<template>
  <footer class="border-t border-pw-panel-hover/40 bg-pw-bg">
    <div class="mx-auto max-w-5xl px-6 py-8">
      <div class="flex flex-col gap-6 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <p class="font-display text-sm font-semibold text-pw-text">PlayWall</p>
          <p class="mt-1 max-w-sm text-sm text-pw-text-muted">{{ t('footer.tagline') }}</p>
        </div>

        <div class="flex flex-col gap-2 text-sm">
          <span class="font-mono text-xs uppercase tracking-wide text-pw-text-muted">{{ t('footer.legal') }}</span>
          <NuxtLink :to="localePath('impressum')" class="text-pw-text-hint hover:text-pw-text">
            {{ t('footer.imprint') }}
          </NuxtLink>
          <NuxtLink :to="localePath('datenschutz')" class="text-pw-text-hint hover:text-pw-text">
            {{ t('footer.privacy') }}
          </NuxtLink>
          <a
              :href="GITHUB_URL"
              target="_blank"
              rel="noopener noreferrer"
              class="text-pw-text-hint hover:text-pw-text"
          >
            GitHub
          </a>
        </div>
      </div>

      <p class="mt-8 font-mono text-xs text-pw-text-muted">
        &copy; {{ year }} {{ t('footer.copyright') }}
      </p>
    </div>
  </footer>
</template>
