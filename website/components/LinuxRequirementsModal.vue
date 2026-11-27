<script setup lang="ts">
const props = defineProps<{
  /** The download currently selected on the Linux card; the steps depend on it. */
  platformId: PlatformId
  url: string
  arch: string
}>()

const {t, te} = useI18n()

const dialog = ref<HTMLDialogElement | null>(null)
const distro = ref<LinuxDistroId>('arch')

const kind = computed(() => linuxPackageKind(props.platformId))
const fileName = computed(() => downloadFileName(props.url))
const isArm = computed(() => props.platformId.startsWith('linux-arm64'))

const libraryCommand = computed(() => LINUX_LIBRARY_INSTALL_COMMANDS[distro.value])
const runCommands = computed(() => linuxRunCommands(kind.value, fileName.value))
const noFuseCommand = computed(() => linuxAppImageNoFuseCommand(fileName.value))
const distroNote = computed(() => {
  const key = `download.guide.distros.${distro.value}.note`
  return te(key) ? t(key) : undefined
})

function open() {
  dialog.value?.showModal()
}

function close() {
  dialog.value?.close()
}

// Clicks on the backdrop hit the <dialog> element itself, as its content fills the whole box.
function onDialogClick(event: MouseEvent) {
  if (event.target === dialog.value) {
    close()
  }
}

defineExpose({open})
</script>

<template>
  <dialog
      ref="dialog"
      aria-labelledby="linux-guide-title"
      class="m-auto w-[min(42rem,calc(100vw-2rem))] max-h-[calc(100dvh-2rem)] overflow-y-auto rounded-xl border border-white/40 bg-pw-bg p-0 text-pw-text backdrop:bg-black/55"
      @click="onDialogClick"
  >
    <div class="flex flex-col gap-5 p-6">
      <div class="flex items-start justify-between gap-4">
        <div>
          <h2 id="linux-guide-title" class="font-display text-xl font-semibold text-pw-text">
            {{ t('download.guide.title') }}
          </h2>
          <p class="mt-1 font-mono text-xs uppercase tracking-wide text-pw-text-muted">
            {{ arch }} · {{ kind === 'appimage' ? '.AppImage' : '.tar.gz' }}
          </p>
        </div>
        <button
            type="button"
            :aria-label="t('common.close')"
            class="flex h-9 w-9 flex-none items-center justify-center rounded-full border border-white/15 bg-white/10 text-pw-text transition-colors hover:bg-white/20"
            @click="close"
        >
          <CloseIcon class="h-5 w-5"/>
        </button>
      </div>

      <p class="text-sm text-pw-text-hint">{{ t('download.guide.intro') }}</p>

      <div role="tablist" :aria-label="t('download.guide.distroLabel')" class="flex flex-wrap gap-2">
        <button
            v-for="id in LINUX_DISTRO_ORDER"
            :key="id"
            type="button"
            role="tab"
            :aria-selected="distro === id"
            class="rounded-md border px-3 py-1.5 text-sm transition-colors"
            :class="distro === id
              ? 'border-pw-blue bg-pw-blue/10 text-pw-text'
              : 'border-white/40 text-pw-text-hint hover:bg-white/[0.06]'"
            @click="distro = id"
        >
          {{ t(`download.guide.distros.${id}.label`) }}
        </button>
      </div>

      <ol class="flex flex-col gap-5">
        <li class="flex flex-col gap-2">
          <h3 class="text-sm font-semibold text-pw-text">{{ t('download.guide.stepLibraries') }}</h3>
          <p class="text-sm text-pw-text-hint">{{ t('download.guide.librariesHint') }}</p>
          <CodeBlock :code="libraryCommand"/>
          <p v-if="distroNote" class="text-xs text-pw-text-muted">{{ distroNote }}</p>
        </li>

        <li class="flex flex-col gap-2">
          <h3 class="text-sm font-semibold text-pw-text">
            {{ kind === 'appimage' ? t('download.guide.stepRunAppImage') : t('download.guide.stepRunArchive') }}
          </h3>
          <CodeBlock :code="runCommands"/>
        </li>
      </ol>

      <div
          v-if="kind === 'appimage' || isArm"
          class="flex flex-col gap-3 border-t border-white/40 pt-4"
      >
        <h3 class="text-sm font-semibold text-pw-text">{{ t('download.guide.troubleshooting') }}</h3>
        <div v-if="kind === 'appimage'" class="flex flex-col gap-2">
          <p class="text-sm text-pw-text-hint">{{ t('download.guide.noFuse') }}</p>
          <CodeBlock :code="noFuseCommand"/>
        </div>
        <p v-if="isArm" class="text-sm text-pw-text-hint">{{ t('download.guide.armGlibc') }}</p>
      </div>
    </div>
  </dialog>
</template>
