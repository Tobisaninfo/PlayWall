import type {CompanionFeedbackDefinitions} from '@companion-module/base'
import {isPlayingStatus} from './domain/playbackStore.js'
import {resolvePadStyle} from './domain/padColor.js'
import {PAD_SELECTOR_OPTIONS, resolvePad} from './domain/padSelector.js'
import {PAD_CURRENT_COLOR_FEEDBACK_ID} from './ids.js'
import type ModuleInstance from './main.js'
import type {ModuleSchema} from './main.js'

export function GetFeedbackDefinitions(self: ModuleInstance): CompanionFeedbackDefinitions<ModuleSchema['feedbacks']> {
    return {
        [PAD_CURRENT_COLOR_FEEDBACK_ID]: {
            type: 'advanced',
            name: 'Pad color (Play/Stop)',
            description: "Colors the button using the pad's (or, failing that, the project's) default/play color, depending on whether the pad is currently playing, with a matching black/white text color.",
            options: PAD_SELECTOR_OPTIONS,
            affectedProperties: ['bgcolor', 'color'],
            callback: (feedback) => {
                const options = feedback.options
                const project = self.projectStore.getProject()
                const pad = resolvePad(options, project, self.playbackStore)
                if (!pad || !project) {
                    return {}
                }

                const isPlaying = isPlayingStatus(self.playbackStore.getPadStatus(pad.id))
                return resolvePadStyle(pad, project.metadata, isPlaying)
            },
        },
    }
}
