<script setup lang="ts">
const props = defineProps<{
  code: string
}>()

const {t} = useI18n()

const copied = ref(false)
let resetTimer: ReturnType<typeof setTimeout> | undefined

async function copy() {
  try {
    await navigator.clipboard.writeText(props.code)
    copied.value = true
    clearTimeout(resetTimer)
    resetTimer = setTimeout(() => (copied.value = false), 2000)
  } catch {
    // Clipboard unavailable (insecure context or denied): the command stays selectable in the page
  }
}

onBeforeUnmount(() => clearTimeout(resetTimer))
</script>

<template>
  <!-- Code and button are separate columns: long commands scroll horizontally inside their own area and are clipped
       at its edge, so they never show through or under the button -->
  <div class="flex items-start gap-2 rounded-md bg-pw-panel-dark p-3">
    <pre class="min-w-0 flex-1 overflow-x-auto whitespace-pre font-mono text-xs text-pw-text"><code>{{ code }}</code></pre>
    <button
        type="button"
        class="flex-none rounded border border-white/30 px-2 py-1 text-xs text-pw-text-hint transition-colors hover:bg-white/10"
        @click="copy"
    >
      {{ copied ? t('download.guide.copied') : t('download.guide.copy') }}
    </button>
  </div>
</template>
