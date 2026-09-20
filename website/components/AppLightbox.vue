<script setup lang="ts">
const {t} = useI18n()
const {state, close} = useLightbox()

const closeButton = ref<HTMLButtonElement | null>(null)
let lastFocused: HTMLElement | null = null

watch(() => state.src, (src) => {
  if (src) {
    lastFocused = document.activeElement as HTMLElement | null
    nextTick(() => closeButton.value?.focus())
  } else {
    lastFocused?.focus()
  }
})

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape' && state.src) {
    close()
  }
}

onMounted(() => window.addEventListener('keydown', onKeydown))
onUnmounted(() => window.removeEventListener('keydown', onKeydown))
</script>

<template>
  <Transition
      enter-active-class="transition-opacity duration-200 ease-out motion-reduce:transition-none"
      enter-from-class="opacity-0"
      leave-active-class="transition-opacity duration-150 ease-in motion-reduce:transition-none"
      leave-to-class="opacity-0"
  >
    <div
        v-if="state.src"
        class="fixed inset-0 z-[100] flex items-center justify-center bg-black/90 p-6 backdrop-blur-sm"
        role="dialog"
        aria-modal="true"
        :aria-label="state.alt"
        @click.self="close"
    >
      <button
          ref="closeButton"
          type="button"
          :aria-label="t('common.close')"
          class="absolute right-4 top-4 flex h-10 w-10 items-center justify-center rounded-full border border-white/15 bg-white/10 text-pw-text transition-colors hover:bg-white/20"
          @click="close"
      >
        <CloseIcon class="h-5 w-5"/>
      </button>

      <img
          :src="state.src"
          :alt="state.alt"
          :width="state.width"
          :height="state.height"
          class="max-h-[85vh] max-w-[90vw] rounded-lg border border-white/10 object-contain shadow-2xl"
      >
    </div>
  </Transition>
</template>
