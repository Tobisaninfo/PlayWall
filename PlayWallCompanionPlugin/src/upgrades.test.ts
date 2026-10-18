import {describe, expect, it} from 'vitest'
import type {CompanionMigrationAction} from '@companion-module/base'
import {addStopAllShouldFadeOut} from './upgrades.js'

function migrationAction(actionId: string, options: CompanionMigrationAction['options']): CompanionMigrationAction {
    return {id: `id-${actionId}`, controlId: 'control', actionId, options}
}

function run(actions: CompanionMigrationAction[]) {
    return addStopAllShouldFadeOut({currentConfig: {} as never}, {config: null, secrets: null, actions, feedbacks: []})
}

describe('addStopAllShouldFadeOut', () => {
    it('sets shouldFadeOut=true on stop_all actions without the option', () => {
        const action = migrationAction('stop_all', {})

        const result = run([action])

        expect(result.updatedActions).toEqual([action])
        expect(action.options.shouldFadeOut).toEqual({value: true, isExpression: false})
    })

    it('keeps an already configured value', () => {
        const action = migrationAction('stop_all', {shouldFadeOut: {value: false, isExpression: false}})

        const result = run([action])

        expect(result.updatedActions).toEqual([])
        expect(action.options.shouldFadeOut).toEqual({value: false, isExpression: false})
    })

    it('ignores other actions', () => {
        const action = migrationAction('volume_change', {})

        const result = run([action])

        expect(result.updatedActions).toEqual([])
        expect(action.options).toEqual({})
    })
})
