import {combineRgb} from '@companion-module/base'
import type {PadDto, ProjectMetadata} from '../connection/protocol.js'

/**
 * The "hi" shade of PlayWall's named color palette (PlayWallCommon's `Color` enum), copied from
 * `PlayWallClient/src/main/sass/components/modern-color.scss`. Companion has no other source for
 * these RGB values — the wire protocol only ever carries the enum name (e.g. "RED1").
 */
const MODERN_COLOR_HEX: Record<string, string> = {
    RED1: '#ef9a9a',
    RED2: '#ef5350',
    RED3: '#e53935',
    DARK_RED1: '#D92349',
    DARK_RED2: '#C92349',
    DARK_RED3: '#A90329',
    PINK1: '#f48fb1',
    PINK2: '#ec407a',
    PINK3: '#d81b60',
    PURPLE1: '#ce93d8',
    PURPLE2: '#ab47bc',
    PURPLE3: '#8e24aa',
    LIGHT_BLUE1: '#80deea',
    LIGHT_BLUE2: '#26c6da',
    LIGHT_BLUE3: '#00acc1',
    BLUE1: '#90caf9',
    BLUE2: '#42a5f5',
    BLUE3: '#1e88e5',
    LIGHT_GREEN1: '#c5e1a5',
    LIGHT_GREEN2: '#9ccc65',
    LIGHT_GREEN3: '#7cb342',
    LIME1: '#e6ee9c',
    LIME2: '#d4e157',
    LIME3: '#c0ca33',
    YELLOW1: '#fff59d',
    YELLOW2: '#ffee58',
    YELLOW3: '#fdd835',
    ORANGE1: '#ffcc80',
    ORANGE2: '#ffa726',
    ORANGE3: '#fb8c00',
    GRAY1: '#eeeeee',
    GRAY2: '#cccccc',
    GRAY3: '#aaaaaa',
    GRAY4: '#888888',
    GRAY5: '#666666',
    GRAY6: '#444444',
}

/**
 * PlayWall never assigns a color at project/pad creation — `defaultColor`/`playColor`/`introColor`
 * stay null until someone explicitly picks one in Project/Pad Display settings. The desktop client
 * falls back to GRAY1 for an unset color (see e.g. PadSettingsDisplayViewController.java); mirrored
 * here so an unconfigured project still gets a sensible feedback color instead of black.
 */
const UNSET_COLOR_FALLBACK = 'GRAY1'
const FALLBACK_COLOR = combineRgb(0, 0, 0)

function hexToRgbNumber(hex: string): number {
    const match = /^#?([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})$/.exec(hex)
    if (!match) {
        return FALLBACK_COLOR
    }
    return combineRgb(parseInt(match[1], 16), parseInt(match[2], 16), parseInt(match[3], 16))
}

/**
 * Resolves the feedback color for a pad: the pad's own color if configured, otherwise the project's,
 * picking the "play" or "default" color depending on whether the pad is currently playing. PlayWall's
 * `introColor` is a CSS keyframe accent in the desktop UI, not a discrete playback state broadcast over
 * the wire, so it has no equivalent here — this feedback is intentionally binary (playing/not playing).
 */
export function resolvePadColor(pad: PadDto, projectMetadata: ProjectMetadata, isPlaying: boolean): number {
    const colorName = (isPlaying
        ? (pad.playColor ?? projectMetadata.playColor)
        : (pad.defaultColor ?? projectMetadata.defaultColor)) ?? UNSET_COLOR_FALLBACK

    const hex = MODERN_COLOR_HEX[colorName]
    return hex ? hexToRgbNumber(hex) : FALLBACK_COLOR
}
