import type {CompanionStaticUpgradeScript} from '@companion-module/base'
import type {ModuleConfig} from './config.js'
import {STOP_ALL_ACTION_ID} from './ids.js'

/** Adds the `shouldFadeOut` option (introduced later) to existing "Stop all pads" actions, keeping their fade-out behaviour. */
export const addStopAllShouldFadeOut: CompanionStaticUpgradeScript<ModuleConfig> = (_context, props) => {
    const updatedActions = props.actions.filter((action) => action.actionId === STOP_ALL_ACTION_ID && action.options.shouldFadeOut === undefined)
    for (const action of updatedActions) {
        action.options.shouldFadeOut = {value: true, isExpression: false}
    }

    return {
        updatedConfig: null,
        updatedActions,
        updatedFeedbacks: [],
    }
}

export const UpgradeScripts: CompanionStaticUpgradeScript<ModuleConfig>[] = [
    /* Once a script has been added it cannot be removed! */
    addStopAllShouldFadeOut,
]
