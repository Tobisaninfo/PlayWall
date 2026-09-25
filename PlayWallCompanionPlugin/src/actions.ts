import type {CompanionActionDefinitions} from '@companion-module/base'
import {isPlayingStatus} from './domain/playbackStore.js'
import {PAD_SELECTOR_OPTIONS, resolvePad} from './domain/padSelector.js'
import {computeTargetPage, PAGE_ACTION_OPTIONS} from './domain/pageSelector.js'
import {computeTargetVolume, DEFAULT_VOLUME, resolveDelta, VOLUME_ACTION_OPTIONS} from './domain/volumeControl.js'
import {PAD_PLAY_STOP_ACTION_ID, PAGE_NAVIGATE_ACTION_ID, VOLUME_CHANGE_ACTION_ID} from './ids.js'
import type ModuleInstance from './main.js'
import type {ModuleSchema} from './main.js'

export function GetActionDefinitions(self: ModuleInstance): CompanionActionDefinitions<ModuleSchema['actions']> {
    return {
        [PAD_PLAY_STOP_ACTION_ID]: {
            name: 'Toggle pad Play/Stop',
            description: "Plays the pad if it's not currently playing, otherwise stops it.",
            options: PAD_SELECTOR_OPTIONS,
            callback: (action) => {
                const options = action.options
                const pad = resolvePad(options, self.projectStore.getProject(), self.pageNavigationStore.getActivePage())
                if (!pad) {
                    self.log('warn', `Toggle Play/Stop: could not resolve a pad for options ${JSON.stringify(options)}`)
                    return
                }

                if (isPlayingStatus(self.playbackStore.getPadStatus(pad.id))) {
                    self.connection?.stopPad(pad.id)
                } else {
                    self.connection?.playPad(pad.id)
                }
            },
        },
        [PAGE_NAVIGATE_ACTION_ID]: {
            name: 'Change page',
            description: 'Moves to the previous/next page, or jumps to a specific page.',
            options: PAGE_ACTION_OPTIONS,
            callback: (action) => {
                const options = action.options
                const project = self.projectStore.getProject()
                if (!project) {
                    self.log('warn', 'Page navigation: no project loaded')
                    return
                }

                const target = computeTargetPage(options.mode, options.pageNumber, self.pageNavigationStore.getActivePage(), project.pages.length)
                if (target === undefined) {
                    self.log('warn', `Page navigation: no target page for options ${JSON.stringify(options)}`)
                    return
                }

                self.updateActivePage(target)
                if (self.config.pageSyncMode === 'sync') {
                    self.connection?.showPage(target)
                }
            },
        },
        [VOLUME_CHANGE_ACTION_ID]: {
            name: 'Change volume',
            description: "Increases or decreases PlayWall's global volume by a fixed step.",
            options: VOLUME_ACTION_OPTIONS,
            callback: (action) => {
                const options = action.options
                const project = self.projectStore.getProject()
                if (!project) {
                    self.log('warn', 'Volume change: no project loaded')
                    return
                }

                const currentVolume = project.metadata.volume ?? DEFAULT_VOLUME
                const target = computeTargetVolume(options.mode, resolveDelta(options.delta), currentVolume)
                if (target === currentVolume) {
                    return
                }

                self.connection?.changeGlobalVolume(target)
            },
        },
    }
}
