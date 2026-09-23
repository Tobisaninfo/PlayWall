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
const BLACK = combineRgb(0, 0, 0)
const WHITE = combineRgb(255, 255, 255)

function hexToRgbComponents(hex: string): [r: number, g: number, b: number] | undefined {
    const match = /^#?([0-9a-fA-F]{2})([0-9a-fA-F]{2})([0-9a-fA-F]{2})$/.exec(hex)
    if (!match) {
        return undefined
    }
    return [Number.parseInt(match[1], 16), Number.parseInt(match[2], 16), Number.parseInt(match[3], 16)]
}

/**
 * Ported from PlayWallClient's `get-appropriate-text-color()` (src/main/sass/utils/colors.scss):
 * picks black or white text by perceived brightness (ITU-R BT.601 luma weights), so pad names stay
 * readable against any of the named background colors.
 */
function getAppropriateTextColor(r: number, g: number, b: number): number {
    const darkness = 1 - (0.299 * r + 0.587 * g + 0.114 * b) / 255
    return darkness < 0.5 ? BLACK : WHITE
}

export interface PadFeedbackStyle {
    bgcolor: number
    color: number
}

/**
 * Resolves the feedback style for a pad: background from the pad's own color if configured,
 * otherwise the project's, picking the "play" or "default" color depending on whether the pad is
 * currently playing, plus a matching black/white text color. PlayWall's `introColor` is a CSS
 * keyframe accent in the desktop UI, not a discrete playback state broadcast over the wire, so it
 * has no equivalent here — this feedback is intentionally binary (playing/not playing).
 */
export function resolvePadStyle(pad: PadDto, projectMetadata: ProjectMetadata, isPlaying: boolean): PadFeedbackStyle {
    const colorName = (isPlaying
        ? (pad.playColor ?? projectMetadata.playColor)
        : (pad.defaultColor ?? projectMetadata.defaultColor)) ?? UNSET_COLOR_FALLBACK

    const components = hexToRgbComponents(MODERN_COLOR_HEX[colorName] ?? '')
    if (!components) {
        return {bgcolor: FALLBACK_COLOR, color: WHITE}
    }

    const [r, g, b] = components
    return {bgcolor: combineRgb(r, g, b), color: getAppropriateTextColor(r, g, b)}
}
