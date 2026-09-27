import {describe, expect, it, vi} from 'vitest'
import {UpdateVariableDefinitions} from './variables.js'
import type ModuleInstance from './main.js'

function stubModuleInstance(setVariableDefinitions: (defs: unknown) => void): ModuleInstance {
    return {setVariableDefinitions} as unknown as ModuleInstance
}

describe('UpdateVariableDefinitions', () => {
    it('always defines current_volume and current_volume_percent, even with no project loaded yet', () => {
        const setVariableDefinitions = vi.fn()

        UpdateVariableDefinitions(stubModuleInstance(setVariableDefinitions), 0, 0)

        expect(setVariableDefinitions).toHaveBeenCalledTimes(1)
        const definitions = setVariableDefinitions.mock.calls[0]?.[0] as Record<string, unknown>
        expect(definitions).toHaveProperty('current_volume')
        expect(definitions).toHaveProperty('current_volume_percent')
    })
})
