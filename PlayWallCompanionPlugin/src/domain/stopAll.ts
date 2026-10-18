import type {CompanionInputFieldCheckbox} from '@companion-module/base'

export type StopAllActionOptions = {
    shouldFadeOut: boolean
}

/** Mirrors the PlayWall client's `StopAllAction.shouldFadeOut` setting (enabled by default). */
export const STOP_ALL_ACTION_OPTIONS: CompanionInputFieldCheckbox<keyof StopAllActionOptions>[] = [
    {
        type: 'checkbox',
        id: 'shouldFadeOut',
        label: 'Respect fade out',
        default: true,
    },
]
