<template>
  <div class="flex min-h-screen flex-col bg-pw-bg text-pw-text">
    <AppNav/>
    <main class="flex-1">
      <slot/>
    </main>
    <AppFooter/>
  </div>
</template>
