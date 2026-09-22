import type {CompanionActionDefinitions} from '@companion-module/base'
import {isPlayingStatus} from './domain/playbackStore.js'
import {PAD_SELECTOR_OPTIONS, resolvePad} from './domain/padSelector.js'
import {PAD_PLAY_STOP_ACTION_ID} from './ids.js'
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
                const pad = resolvePad(options, self.projectStore.getProject(), self.playbackStore)
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
    }
}
