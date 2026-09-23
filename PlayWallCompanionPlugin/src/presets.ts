import {combineRgb} from '@companion-module/base'
import type {CompanionPresetDefinitions, CompanionPresetSection} from '@companion-module/base'
import type {PadSelectorOptions} from './domain/padSelector.js'
import {PAD_CURRENT_COLOR_FEEDBACK_ID, PAD_PLAY_STOP_ACTION_ID} from './ids.js'
import type {ModuleSchema} from './main.js'

/** PlayWall projects can have up to a 10x10 pad grid per page. */
const MAX_PAD_POSITION = 100

function presetId(position: number): string {
    return `pad_${position}`
}

function padOptions(position: number): PadSelectorOptions {
    return {mode: 'index', padName: '', padPosition: position}
}

/**
 * One ready-to-use button per pad position (1-100) on the currently shown page: pressing it toggles
 * Play/Stop, its background/text color follows `pad_current_color`, and its label shows the pad's
 * current name — so a user can drag a preset onto their grid instead of configuring the action,
 * feedback and text variable by hand.
 */
export function GetPresetDefinitions(): CompanionPresetDefinitions<ModuleSchema> {
    const presets: CompanionPresetDefinitions<ModuleSchema> = {}

    for (let position = 1; position <= MAX_PAD_POSITION; position++) {
        presets[presetId(position)] = {
            type: 'simple',
            name: `Pad ${position}`,
            style: {
                text: `$(label:pad_name_${position})`,
                size: 'auto',
                color: combineRgb(255, 255, 255),
                bgcolor: combineRgb(0, 0, 0),
            },
            previewStyle: {
                text: `Pad ${position}`,
                color: combineRgb(255, 255, 255),
                bgcolor: combineRgb(0, 0, 0),
            },
            steps: [
                {
                    down: [
                        {
                            actionId: PAD_PLAY_STOP_ACTION_ID,
                            options: padOptions(position),
                        },
                    ],
                    up: [],
                },
            ],
            feedbacks: [
                {
                    feedbackId: PAD_CURRENT_COLOR_FEEDBACK_ID,
                    options: padOptions(position),
                },
            ],
        }
    }

    return presets
}

export function GetPresetSections(): CompanionPresetSection<ModuleSchema>[] {
    return [
        {
            id: 'pads',
            name: 'Pads (position 1-100 on the current page)',
            definitions: Array.from({length: MAX_PAD_POSITION}, (_, index) => presetId(index + 1)),
        },
    ]
}
