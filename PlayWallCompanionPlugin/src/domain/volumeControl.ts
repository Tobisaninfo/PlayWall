import type {CompanionInputFieldDropdown} from '@companion-module/base'

/** Matches PlayWallServerCommon's `ProjectMetadata.DEFAULT_VOLUME`. */
export const DEFAULT_VOLUME = 1.0

const MIN_VOLUME = 0.0
const MAX_VOLUME = 1.0

export type VolumeChangeMode = 'increase' | 'decrease'
export type VolumeChangeDelta = '5' | '10'

export type VolumeActionOptions = {
    mode: VolumeChangeMode
    delta: VolumeChangeDelta
}

const DELTA_VALUES: Record<VolumeChangeDelta, number> = {
    '5': 0.05,
    '10': 0.1,
}

export function resolveDelta(delta: VolumeChangeDelta): number {
    return DELTA_VALUES[delta]
}

export const VOLUME_ACTION_OPTIONS: CompanionInputFieldDropdown<keyof VolumeActionOptions>[] = [
    {
        id: 'mode',
        type: 'dropdown',
        label: 'Direction',
        choices: [
            {id: 'increase', label: 'Louder (+)'},
            {id: 'decrease', label: 'Quieter (-)'},
        ],
        default: 'increase',
    },
    {
        id: 'delta',
        type: 'dropdown',
        label: 'Step size',
        choices: [
            {id: '5', label: '5%'},
            {id: '10', label: '10%'},
        ],
        default: '5',
    },
]

/**
 * Mirrors PlayWallClient's `GlobalVolumeActionHandler` exactly: clamp to [0, 1], no-op (same value
 * returned) if already at the bound in that direction. Companion mirrors the client's [0, 1] range, not
 * the server validator's wider [0, 1.25] bound, which is only meaningful for per-pad volume.
 * The result is rounded to 2 decimals to avoid floating-point step noise (e.g. 0.7500000000000001).
 */
export function computeTargetVolume(mode: VolumeChangeMode, delta: number, currentVolume: number): number {
    const target = mode === 'increase' ? Math.min(currentVolume + delta, MAX_VOLUME) : Math.max(currentVolume - delta, MIN_VOLUME)
    return Math.round(target * 100) / 100
}

/** Formats a volume value (0-1) for display in the `current_volume` variable. */
export function formatVolume(volume: number | null | undefined): string {
    return String(volume ?? DEFAULT_VOLUME)
}

/** Formats a volume value (0-1) as a percentage (e.g. 0.75 -> "75%") for the `current_volume_percent` variable. */
export function formatVolumePercent(volume: number | null | undefined): string {
    return `${Math.round((volume ?? DEFAULT_VOLUME) * 100)}%`
}

/** The `current_volume`/`current_volume_percent` variable values for a given volume (0-1), in one call. */
export function volumeVariableValues(volume: number | null | undefined): {
    current_volume: string;
    current_volume_percent: string
} {
    return {current_volume: formatVolume(volume), current_volume_percent: formatVolumePercent(volume)}
}
