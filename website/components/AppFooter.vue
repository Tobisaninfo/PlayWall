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

        <p class="font-mono text-xs text-pw-text-muted">
          &copy; {{ year }} {{ t('footer.copyright') }}
        </p>
      </div>
    </div>
  </footer>
</template>
