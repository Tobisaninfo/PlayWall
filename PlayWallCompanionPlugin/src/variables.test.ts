import {describe, expect, it, vi} from 'vitest'
import {UpdatePadVariableValues, UpdatePageVariableValues, UpdateVariableDefinitions} from './variables.js'
import type {PadDto, ProjectDto} from './connection/protocol.js'
import type ModuleInstance from './main.js'

function stubModuleInstanceForDefinitions(setVariableDefinitions: (defs: unknown) => void): ModuleInstance {
    return {setVariableDefinitions} as unknown as ModuleInstance
}

function stubModuleInstanceForValues(setVariableValues: (values: unknown) => void): ModuleInstance {
    return {setVariableValues} as unknown as ModuleInstance
}

function stubModuleInstanceForPadValues(
    setVariableValues: (values: unknown) => void,
    padStatuses: Record<string, string> = {},
): ModuleInstance {
    return {
        setVariableValues,
        playbackStore: {getPadStatus: (padId: string) => padStatuses[padId]},
    } as unknown as ModuleInstance
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

function fakePad(id: string, position: number, name: string | null): PadDto {
    return {id, position, name, defaultColor: null, playColor: null, introColor: null}
}

function fakeProjectWithPadsOnPage(pageIndex: number, pads: PadDto[], horizontal: number, vertical: number): ProjectDto {
    return {
        metadata: {
            id: 'project-1',
            name: 'Test project',
            defaultColor: null,
            playColor: null,
            introColor: null,
            numberOfHorizontalPads: horizontal,
            numberOfVerticalPads: vertical,
            volume: 1,
        },
        pages: [{id: `page-${pageIndex}`, position: pageIndex, settings: null, pads}],
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

    it('defines a pad_name_<n> and pad_status_<n> per pad slot on a page', () => {
        const setVariableDefinitions = vi.fn()

        UpdateVariableDefinitions(stubModuleInstanceForDefinitions(setVariableDefinitions), 4, 0)

        const definitions = setVariableDefinitions.mock.calls[0]?.[0] as Record<string, unknown>
        for (let position = 1; position <= 4; position++) {
            expect(definitions).toHaveProperty(`pad_name_${position}`)
            expect(definitions).toHaveProperty(`pad_status_${position}`)
        }
        expect(definitions).not.toHaveProperty('pad_name_5')
        expect(definitions).not.toHaveProperty('pad_status_5')
    })
})

describe('UpdatePadVariableValues', () => {
    it('does nothing when no project is loaded', () => {
        const setVariableValues = vi.fn()

        UpdatePadVariableValues(stubModuleInstanceForPadValues(setVariableValues), undefined, 0)

        expect(setVariableValues).not.toHaveBeenCalled()
    })

    it('sets pad_name_<n>/pad_status_<n> from the pads on the given (0-based) active page, 1-based', () => {
        const setVariableValues = vi.fn()
        const project = fakeProjectWithPadsOnPage(
            0,
            [fakePad('pad-1', 0, 'Intro'), fakePad('pad-2', 1, 'Outro')],
            2,
            1,
        )

        UpdatePadVariableValues(
            stubModuleInstanceForPadValues(setVariableValues, {'pad-1': 'PLAYING', 'pad-2': 'STOPPED'}),
            project,
            0,
        )

        expect(setVariableValues).toHaveBeenCalledWith({
            pad_name_1: 'Intro',
            pad_status_1: 'PLAYING',
            pad_name_2: 'Outro',
            pad_status_2: 'STOPPED',
        })
    })

    it('resolves an empty grid slot to empty name/status strings', () => {
        const setVariableValues = vi.fn()
        const project = fakeProjectWithPadsOnPage(0, [fakePad('pad-1', 0, 'Intro')], 2, 1)

        UpdatePadVariableValues(stubModuleInstanceForPadValues(setVariableValues), project, 0)

        expect(setVariableValues).toHaveBeenCalledWith(
            expect.objectContaining({pad_name_2: '', pad_status_2: ''}),
        )
    })

    it('resolves a pad with an unobserved status to an empty status string', () => {
        const setVariableValues = vi.fn()
        const project = fakeProjectWithPadsOnPage(0, [fakePad('pad-1', 0, 'Intro')], 1, 1)

        UpdatePadVariableValues(stubModuleInstanceForPadValues(setVariableValues), project, 0)

        expect(setVariableValues).toHaveBeenCalledWith(expect.objectContaining({pad_status_1: ''}))
    })

    it('only shows the pads of the given active page, not other pages', () => {
        const setVariableValues = vi.fn()
        const project = fakeProjectWithPadsOnPage(1, [fakePad('pad-1', 0, 'On page 2')], 1, 1)

        UpdatePadVariableValues(stubModuleInstanceForPadValues(setVariableValues), project, 0)

        expect(setVariableValues).toHaveBeenCalledWith(expect.objectContaining({pad_name_1: ''}))
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
