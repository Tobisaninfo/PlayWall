import {combineRgb} from '@companion-module/base'
import {MODERN_COLOR_PALETTE} from './modernColorPalette.generated.js'
import type {PadDto, ProjectMetadata} from '../connection/protocol.js'

/**
 * PlayWall never assigns a color at project/pad creation — `defaultColor`/`playColor`/`introColor`
 * stay null until someone explicitly picks one in Project/Pad Display settings. The desktop client
 * falls back to GRAY1 for an unset color (see e.g. PadSettingsDisplayViewController.java); mirrored
 * here so an unconfigured project still gets a sensible feedback color instead of black.
 */
const UNSET_COLOR_FALLBACK = 'GRAY1'
const FALLBACK_COLOR = combineRgb(0, 0, 0)
const WHITE = combineRgb(255, 255, 255)

function hexToRgbNumber(hex: string): number | undefined {
    const match = /^#?([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})$/.exec(hex)
    if (!match) {
        return undefined
    }
    return combineRgb(Number.parseInt(match[1], 16), Number.parseInt(match[2], 16), Number.parseInt(match[3], 16))
}

export interface PadFeedbackStyle {
    bgcolor: number
    color: number
}

/**
 * Resolves the feedback style for a pad: background from the pad's own color if configured,
 * otherwise the project's, picking the "play" or "default" color depending on whether the pad is
 * currently playing, plus the matching black/white text color PlayWall itself picked for that
 * background (`MODERN_COLOR_PALETTE`, generated from `ModernColor.json` — see
 * scripts/generate-modern-color-palette.mjs). PlayWall's `introColor` is a CSS keyframe accent in
 * the desktop UI, not a discrete playback state broadcast over the wire, so it has no equivalent
 * here — this feedback is intentionally binary (playing/not playing).
 */
export function resolvePadStyle(pad: PadDto, projectMetadata: ProjectMetadata, isPlaying: boolean): PadFeedbackStyle {
    const colorName = (isPlaying
        ? (pad.playColor ?? projectMetadata.playColor)
        : (pad.defaultColor ?? projectMetadata.defaultColor)) ?? UNSET_COLOR_FALLBACK

    const entry = MODERN_COLOR_PALETTE[colorName]
    const bgcolor = entry && hexToRgbNumber(entry.bg)
    const color = entry && hexToRgbNumber(entry.font)
    if (bgcolor === undefined || color === undefined) {
        return {bgcolor: FALLBACK_COLOR, color: WHITE}
    }

    return {bgcolor, color}
}
