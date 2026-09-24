import type {CompanionFeedbackDefinitions} from '@companion-module/base'
import {isPlayingStatus} from './domain/playbackStore.js'
import {resolvePadStyle} from './domain/padColor.js'
import {resolvePageStyle} from './domain/pageColor.js'
import {PAD_SELECTOR_OPTIONS, resolvePad} from './domain/padSelector.js'
import {PAGE_FEEDBACK_OPTIONS} from './domain/pageSelector.js'
import {resolvePage} from './domain/pageResolver.js'
import {PAD_CURRENT_COLOR_FEEDBACK_ID, PAGE_CURRENT_COLOR_FEEDBACK_ID} from './ids.js'
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
                const pad = resolvePad(options, project, self.pageNavigationStore.getActivePage())
                if (!pad || !project) {
                    return {}
                }

                const isPlaying = isPlayingStatus(self.playbackStore.getPadStatus(pad.id))
                return resolvePadStyle(pad, project.metadata, isPlaying)
            },
        },
        [PAGE_CURRENT_COLOR_FEEDBACK_ID]: {
            type: 'advanced',
            name: 'Page color',
            description: "Colors the button using the page's own color, with a matching black/white text color, and a border while it's the active page (Companion's own, see the module's page sync setting).",
            options: PAGE_FEEDBACK_OPTIONS,
            affectedProperties: ['bgcolor', 'color'],
            callback: (feedback) => {
                const options = feedback.options
                const project = self.projectStore.getProject()
                const page = project && resolvePage(project, options.pageNumber)
                if (!page) {
                    return {}
                }

                const isActive = options.pageNumber - 1 === self.pageNavigationStore.getActivePage()
                return resolvePageStyle(page, isActive, feedback.image)
            },
        },
    }
}
