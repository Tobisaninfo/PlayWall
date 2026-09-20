<script setup lang="ts">
const {t} = useI18n()

const year = new Date().getFullYear()
</script>

<template>
  <footer class="border-t border-pw-panel-hover/40 bg-pw-bg">
    <div class="mx-auto max-w-5xl px-6 py-8">
      <div class="flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p class="font-display text-sm font-semibold text-pw-text">PlayWall</p>
          <p class="mt-1 max-w-sm text-sm text-pw-text-muted">{{ t('footer.tagline') }}</p>
        </div>

        <a
            :href="GITHUB_URL"
            target="_blank"
            rel="noopener noreferrer"
            class="flex items-center gap-1.5 text-sm text-pw-text-hint hover:text-pw-text"
        >
          <GithubIcon class="h-4 w-4 flex-none"/>
          GitHub
        </a>
      </div>

      <p class="mt-8 font-mono text-xs text-pw-text-muted">
        &copy; {{ year }} {{ t('footer.copyright') }}
      </p>
    </div>
  </footer>
</template>
