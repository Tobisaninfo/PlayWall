import {combineRgb} from '@companion-module/base'
import type {CompanionPresetDefinitions, CompanionPresetSection} from '@companion-module/base'
import type {PadSelectorOptions} from './domain/padSelector.js'
import type {PageActionOptions, PageFeedbackOptions} from './domain/pageSelector.js'
import type {VolumeActionOptions} from './domain/volumeControl.js'
import {
    PAD_CURRENT_COLOR_FEEDBACK_ID,
    PAD_PLAY_STOP_ACTION_ID,
    PAGE_CURRENT_COLOR_FEEDBACK_ID,
    PAGE_NAVIGATE_ACTION_ID,
    VOLUME_CHANGE_ACTION_ID
} from './ids.js'
import type {ModuleSchema} from './main.js'

/** PlayWall projects can have up to a 10x10 pad grid per page. */
const MAX_PAD_POSITION = 100

/** Matches the "jump to page" number field's range (`domain/pageSelector.ts`). */
const MAX_PAGE_POSITION = 20

function presetId(position: number): string {
    return `pad_${position}`
}

function padOptions(position: number): PadSelectorOptions {
    return {mode: 'index', padName: '', padPosition: position}
}

function pagePresetId(position: number): string {
    return `page_${position}`
}

function pageFeedbackOptions(position: number): PageFeedbackOptions {
    return {pageNumber: position}
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

    for (let position = 1; position <= MAX_PAGE_POSITION; position++) {
        presets[pagePresetId(position)] = {
            type: 'simple',
            name: `Page ${position}`,
            style: {
                text: `$(label:page_name_${position})`,
                size: 'auto',
                color: combineRgb(255, 255, 255),
                bgcolor: combineRgb(0, 0, 0),
            },
            previewStyle: {
                text: `Page ${position}`,
                color: combineRgb(255, 255, 255),
                bgcolor: combineRgb(0, 0, 0),
            },
            steps: [
                {
                    down: [
                        {
                            actionId: PAGE_NAVIGATE_ACTION_ID,
                            options: {mode: 'jump', pageNumber: position} satisfies PageActionOptions,
                        },
                    ],
                    up: [],
                },
            ],
            feedbacks: [
                {
                    feedbackId: PAGE_CURRENT_COLOR_FEEDBACK_ID,
                    options: pageFeedbackOptions(position),
                },
            ],
        }
    }

    presets['page_previous'] = {
        type: 'simple',
        name: 'Previous page',
        style: {text: 'Page ◀', size: 'auto', color: combineRgb(255, 255, 255), bgcolor: combineRgb(0, 0, 0)},
        steps: [{
            down: [{
                actionId: PAGE_NAVIGATE_ACTION_ID,
                options: {mode: 'previous', pageNumber: 1} satisfies PageActionOptions
            }], up: []
        }],
        feedbacks: [],
    }
    presets['page_next'] = {
        type: 'simple',
        name: 'Next page',
        style: {text: 'Page ▶', size: 'auto', color: combineRgb(255, 255, 255), bgcolor: combineRgb(0, 0, 0)},
        steps: [{
            down: [{
                actionId: PAGE_NAVIGATE_ACTION_ID,
                options: {mode: 'next', pageNumber: 1} satisfies PageActionOptions
            }], up: []
        }],
        feedbacks: [],
    }

    presets['volume_up'] = {
        type: 'simple',
        name: 'Volume up (5%)',
        style: {text: 'Vol\\n+5%', size: 'auto', color: combineRgb(255, 255, 255), bgcolor: combineRgb(0, 0, 0)},
        steps: [{
            down: [{
                actionId: VOLUME_CHANGE_ACTION_ID,
                options: {mode: 'increase', delta: '5'} satisfies VolumeActionOptions
            }], up: []
        }],
        feedbacks: [],
    }
    presets['volume_down'] = {
        type: 'simple',
        name: 'Volume down (5%)',
        style: {text: 'Vol\\n-5%', size: 'auto', color: combineRgb(255, 255, 255), bgcolor: combineRgb(0, 0, 0)},
        steps: [{
            down: [{
                actionId: VOLUME_CHANGE_ACTION_ID,
                options: {mode: 'decrease', delta: '5'} satisfies VolumeActionOptions
            }], up: []
        }],
        feedbacks: [],
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
        {
            id: 'pages',
            name: 'Pages (position 1-20)',
            definitions: [
                ...Array.from({length: MAX_PAGE_POSITION}, (_, index) => pagePresetId(index + 1)),
                'page_previous',
                'page_next',
            ],
        },
        {
            id: 'volume',
            name: 'Volume',
            definitions: ['volume_up', 'volume_down'],
        },
    ]
}
