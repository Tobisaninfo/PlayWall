import type {
    CompanionInputFieldDropdown,
    CompanionInputFieldNumber,
    CompanionInputFieldTextInput
} from '@companion-module/base'
import {findPadByName, findPadByPosition} from './padResolver.js'
import type {PlaybackStore} from './playbackStore.js'
import type {PadDto, ProjectDto} from '../connection/protocol.js'

export type PadSelectorMode = 'name' | 'index'

export type PadSelectorOptions = {
    mode: PadSelectorMode
    padName: string
    padPosition: number
}

/**
 * The action and the feedback both let the user pick a pad the same way, so they share this option
 * group (id-compatible with both `SomeCompanionActionInputField` and `SomeCompanionFeedbackInputField`,
 * which only differ in field types this option group doesn't use).
 */
export const PAD_SELECTOR_OPTIONS: (CompanionInputFieldDropdown<keyof PadSelectorOptions> | CompanionInputFieldTextInput<keyof PadSelectorOptions> | CompanionInputFieldNumber<keyof PadSelectorOptions>)[] = [
    {
        id: 'mode',
        type: 'dropdown',
        label: 'Select pad by',
        choices: [
            {id: 'name', label: 'Name'},
            {id: 'index', label: 'Position on the current page'},
        ],
        default: 'name',
        disableAutoExpression: true,
    },
    {
        id: 'padName',
        type: 'textinput',
        label: 'Pad name',
        tooltip: 'The exact name of the pad, searched across all pages of the project.',
        isVisibleExpression: '$(options:mode) == "name"',
    },
    {
        id: 'padPosition',
        type: 'number',
        label: 'Pad position (1-based)',
        tooltip: "The pad's position on the page currently shown in PlayWall.",
        min: 1,
        max: 100,
        default: 1,
        isVisibleExpression: '$(options:mode) == "index"',
    },
]

/**
 * Resolves the pad an action/feedback instance points to, given the current project and playback
 * state. Returns `undefined` if the project isn't known yet, or no matching pad exists.
 */
export function resolvePad(options: PadSelectorOptions, project: ProjectDto | undefined, playbackStore: PlaybackStore): PadDto | undefined {
    if (!project) {
        return undefined
    }

    if (options.mode === 'index') {
        return findPadByPosition(project, playbackStore.getCurrentPageIndex(), options.padPosition - 1)
    }

    return findPadByName(project, options.padName)
}
