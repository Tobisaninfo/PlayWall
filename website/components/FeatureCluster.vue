<script setup lang="ts">
const props = defineProps<{
  id: string
  title: string
  description: string
  features: string[]
  screenshot: string
  index: number
}>()

const dimensions = screenshotDimensions[props.screenshot] ?? [1680, 1152]
const reversed = props.index % 2 === 1

const target = ref<HTMLElement | null>(null)
const isVisible = ref(false)
const {stop} = useIntersectionObserver(
    target,
    ([entry]) => {
      if (entry?.isIntersecting) {
        isVisible.value = true
        stop()
      }
    },
    {threshold: 0.2},
)

const {t} = useI18n()
const {open: openLightbox} = useLightbox()

function onImageClick() {
  openLightbox(`/screenshots/${props.screenshot}`, props.title, dimensions[0], dimensions[1])
}
</script>

<template>
  <div
      :id="id"
      ref="target"
      class="grid scroll-mt-24 items-center gap-10 py-14 transition-[opacity,transform] duration-700 ease-out motion-reduce:transition-none lg:grid-cols-2 lg:gap-16"
      :class="isVisible ? 'translate-y-0 opacity-100' : 'translate-y-6 opacity-0'"
  >
    <div :class="reversed ? 'lg:order-last' : ''">
      <button
          type="button"
          class="group relative block w-full cursor-zoom-in overflow-hidden rounded-xl border border-white/10 shadow-xl"
          :aria-label="t('features.lightboxOpen', {title})"
          @click="onImageClick"
      >
        <img
            :src="`/screenshots/${screenshot}`"
            :alt="title"
            :width="dimensions[0]"
            :height="dimensions[1]"
            loading="lazy"
            class="w-full transition-transform duration-300 group-hover:scale-[1.02] motion-reduce:transition-none motion-reduce:group-hover:scale-100"
        >
        <span class="pointer-events-none absolute inset-0 bg-black/0 transition-colors duration-300 group-hover:bg-black/10"/>
      </button>
    </div>

    <div>
      <h2 class="font-display text-2xl font-semibold text-pw-text sm:text-3xl">{{ title }}</h2>
      <p class="mt-2 text-pw-text-hint">{{ description }}</p>
      <ul class="mt-5 flex flex-col gap-2.5">
        <li v-for="feature in features" :key="feature" class="flex items-start gap-2.5 text-sm text-pw-text-hint">
          <span class="mt-[7px] h-1.5 w-1.5 flex-none rounded-full bg-pw-blue"/>
          <span>{{ feature }}</span>
        </li>
      </ul>
    </div>
  </div>
</template>
