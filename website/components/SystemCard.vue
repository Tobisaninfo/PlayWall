<script setup lang="ts">
const props = defineProps<{
  systemId: SystemId
  highlighted: boolean
}>()

const {t} = useI18n()

const platformIds = SYSTEM_PLATFORMS[props.systemId]

const systemName = computed(() => t(`download.platforms.${platformIds[0]}.name`))

const rows = computed(() =>
    platformIds.map((id) => ({
      id,
      url: release.platforms[id],
      arch: t(`download.platforms.${id}.arch`),
      requirements: t(`download.platforms.${id}.requirements`),
      fileType: t(`download.platforms.${id}.fileType`),
    })),
)
</script>

<template>
  <div
      class="flex flex-col gap-4 rounded-xl border p-5 transition-colors"
      :class="highlighted ? 'border-pw-blue bg-pw-blue/5' : 'border-white/10 bg-white/[0.02]'"
  >
    <div class="flex flex-wrap items-start justify-between gap-x-3 gap-y-1.5">
      <div>
        <div class="flex items-center gap-2">
          <OsIcon :system="systemId" class="h-5 w-5 flex-none" :class="highlighted ? 'text-pw-blue' : 'text-pw-text-muted'"/>
          <p class="font-display text-lg font-semibold text-pw-text">{{ systemName }}</p>
        </div>
        <p v-if="rows.length === 1" class="font-mono text-xs uppercase tracking-wide text-pw-text-muted">
          {{ rows[0]?.arch }}
        </p>
      </div>
      <span
          v-if="highlighted"
          class="rounded-full bg-pw-blue-strong px-2.5 py-1 font-mono text-[11px] uppercase tracking-wide text-white"
      >
        {{ t('download.recommended') }}
      </span>
    </div>

    <!-- Einzelne Architektur: Windows, macOS. Die Beschreibung nimmt den
         Platz vor dem Button ein (flex-1), sodass der Button unabhängig von
         der Textlänge immer am unteren Kartenrand sitzt — nicht irgendwo
         mit Leerraum darunter. -->
    <template v-if="rows.length === 1 && rows[0]">
      <div class="flex flex-1 flex-col gap-2">
        <p class="text-sm text-pw-text-hint">{{ rows[0].requirements }}</p>
        <p class="font-mono text-xs text-pw-text-muted">{{ rows[0].fileType }}</p>
      </div>
      <a
          :href="rows[0].url"
          class="inline-flex items-center justify-center rounded-md bg-pw-blue-strong px-4 py-2.5 text-sm font-medium text-white transition-opacity hover:opacity-90"
      >
        {{ t('download.downloadCta') }}
      </a>
    </template>

    <!-- Mehrere Architekturen: Linux (amd64 / arm64). Jede Architektur
         behält ihren eigenen Button direkt unter der zugehörigen
         Beschreibung — die beiden Buttons gehören inhaltlich zusammen mit
         ihrem jeweiligen Block, ein gemeinsamer Bündig-unten-Anker wie oben
         ergäbe hier keinen Sinn. -->
    <div v-else class="flex flex-1 flex-col gap-4">
      <div v-for="row in rows" :key="row.id" class="flex flex-col gap-1.5 border-t border-white/5 pt-4 first:border-none first:pt-0">
        <p class="font-mono text-xs uppercase tracking-wide text-pw-text-muted">{{ row.arch }}</p>
        <p class="text-sm text-pw-text-hint">{{ row.requirements }}</p>
        <p class="font-mono text-xs text-pw-text-muted">{{ row.fileType }}</p>
        <a
            :href="row.url"
            class="inline-flex items-center justify-center rounded-md bg-pw-blue-strong px-4 py-2 text-sm font-medium text-white transition-opacity hover:opacity-90"
        >
          {{ t('download.downloadCta') }}
        </a>
      </div>
    </div>
  </div>
</template>
