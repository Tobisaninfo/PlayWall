<script setup lang="ts">
const localePath = useLocalePath()
const route = useRoute()

const isHome = computed(() => route.path === localePath('index'))
</script>

<template>
  <div
      class="flex flex-col bg-pw-bg text-pw-text"
      :class="isHome ? 'h-screen' : 'min-h-screen'"
  >
    <AppNav/>
    <main class="min-h-0 flex-1">
      <slot/>
    </main>
    <AppFooter/>
  </div>
</template>
