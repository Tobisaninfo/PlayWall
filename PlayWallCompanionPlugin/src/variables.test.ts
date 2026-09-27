import {describe, expect, it, vi} from 'vitest'
import {UpdatePageVariableValues, UpdateVariableDefinitions} from './variables.js'
import type {ProjectDto} from './connection/protocol.js'
import type ModuleInstance from './main.js'

function stubModuleInstanceForDefinitions(setVariableDefinitions: (defs: unknown) => void): ModuleInstance {
    return {setVariableDefinitions} as unknown as ModuleInstance
}

function stubModuleInstanceForValues(setVariableValues: (values: unknown) => void): ModuleInstance {
    return {setVariableValues} as unknown as ModuleInstance
}

function fakeProjectWithPages(names: (string | null)[]): ProjectDto {
    return {
        metadata: {
            id: 'project-1',
            name: 'Test project',
            defaultColor: null,
            playColor: null,
            introColor: null,
            numberOfHorizontalPads: 1,
            numberOfVerticalPads: 1,
            volume: 1,
        },
        pages: names.map((name, index) => ({
            id: `page-${index}`,
            position: index,
            settings: {name, color: null},
            pads: [],
        })),
    }
}

describe('UpdateVariableDefinitions', () => {
    it('always defines current_volume and current_volume_percent, even with no project loaded yet', () => {
        const setVariableDefinitions = vi.fn()

        UpdateVariableDefinitions(stubModuleInstanceForDefinitions(setVariableDefinitions), 0, 0)

        expect(setVariableDefinitions).toHaveBeenCalledTimes(1)
        const definitions = setVariableDefinitions.mock.calls[0]?.[0] as Record<string, unknown>
        expect(definitions).toHaveProperty('current_volume')
        expect(definitions).toHaveProperty('current_volume_percent')
    })

    it('always defines page_active, plus one page_name_<n> per page in the project', () => {
        const setVariableDefinitions = vi.fn()

        UpdateVariableDefinitions(stubModuleInstanceForDefinitions(setVariableDefinitions), 0, 3)

        const definitions = setVariableDefinitions.mock.calls[0]?.[0] as Record<string, unknown>
        expect(definitions).toHaveProperty('page_active')
        expect(definitions).toHaveProperty('page_name_1')
        expect(definitions).toHaveProperty('page_name_2')
        expect(definitions).toHaveProperty('page_name_3')
        expect(definitions).not.toHaveProperty('page_name_4')
    })
})

describe('UpdatePageVariableValues', () => {
    it('does nothing when no project is loaded', () => {
        const setVariableValues = vi.fn()

        UpdatePageVariableValues(stubModuleInstanceForValues(setVariableValues), undefined, 0)

        expect(setVariableValues).not.toHaveBeenCalled()
    })

    it('sets page_active as the 1-based active page position', () => {
        const setVariableValues = vi.fn()
        const project = fakeProjectWithPages(['A', 'B', 'C'])

        UpdatePageVariableValues(stubModuleInstanceForValues(setVariableValues), project, 1)

        expect(setVariableValues).toHaveBeenCalledWith(expect.objectContaining({page_active: '2'}))
    })

    it('sets every page_name_<n> from the project, in 1-based order', () => {
        const setVariableValues = vi.fn()
        const project = fakeProjectWithPages(['Intro', 'Main', 'Outro'])

        UpdatePageVariableValues(stubModuleInstanceForValues(setVariableValues), project, 0)

        expect(setVariableValues).toHaveBeenCalledWith(
            expect.objectContaining({page_name_1: 'Intro', page_name_2: 'Main', page_name_3: 'Outro'}),
        )
    })

    it('resolves a missing page name to an empty string', () => {
        const setVariableValues = vi.fn()
        const project = fakeProjectWithPages([null])

        UpdatePageVariableValues(stubModuleInstanceForValues(setVariableValues), project, 0)

        expect(setVariableValues).toHaveBeenCalledWith(expect.objectContaining({page_name_1: ''}))
    })
})
