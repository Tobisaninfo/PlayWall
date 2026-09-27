import type {CompanionActionCallbackContext, CompanionActionEvent} from '@companion-module/base'
import {describe, expect, it, vi} from 'vitest'
import {GetActionDefinitions} from './actions.js'
import {STOP_ALL_ACTION_ID} from './ids.js'
import type ModuleInstance from './main.js'

/** The `stop_all` callback ignores both parameters, so empty stubs are enough to satisfy the type. */
const ACTION_EVENT = {} as CompanionActionEvent<Record<string, never>>
const ACTION_CONTEXT = {} as CompanionActionCallbackContext

function stubModuleInstance(connection: { stopAllPads: () => void } | undefined): ModuleInstance {
    return {connection} as unknown as ModuleInstance
}

/** `CompanionActionDefinitions` types each entry as possibly `false`/`undefined`; it never actually is here. */
function getStopAllAction(self: ModuleInstance) {
    const action = GetActionDefinitions(self)[STOP_ALL_ACTION_ID]
    if (!action) {
        throw new Error('stop_all action definition is missing')
    }
    return action
}

describe('stop_all action', () => {
    it('has no configurable options', () => {
        const action = getStopAllAction(stubModuleInstance(undefined))

        expect(action.options).toEqual([])
    })

    it('stops all pads through the connection when pressed', () => {
        const stopAllPads = vi.fn()

        const action = getStopAllAction(stubModuleInstance({stopAllPads}))
        action.callback(ACTION_EVENT, ACTION_CONTEXT)

        expect(stopAllPads).toHaveBeenCalledTimes(1)
        expect(stopAllPads).toHaveBeenCalledWith()
    })

    it('does not throw when there is no active connection yet', () => {
        const action = getStopAllAction(stubModuleInstance(undefined))

        expect(() => action.callback(ACTION_EVENT, ACTION_CONTEXT)).not.toThrow()
    })
})
