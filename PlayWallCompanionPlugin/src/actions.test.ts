import type {CompanionActionCallbackContext, CompanionActionEvent} from '@companion-module/base'
import {describe, expect, it, vi} from 'vitest'
import {GetActionDefinitions} from './actions.js'
import {PAGE_NAVIGATE_ACTION_ID, STOP_ALL_ACTION_ID, VOLUME_CHANGE_ACTION_ID} from './ids.js'
import type {PageActionOptions} from './domain/pageSelector.js'
import type {VolumeActionOptions} from './domain/volumeControl.js'
import type {ProjectDto} from './connection/protocol.js'
import type ModuleInstance from './main.js'

const ACTION_CONTEXT = {} as CompanionActionCallbackContext

interface StubConnection {
    stopAllPads?: () => void
    changeGlobalVolume?: (volume: number) => void
    showPage?: (index: number) => void
}

function stubModuleInstance(
    options: {
        connection?: StubConnection
        project?: ProjectDto
        activePage?: number
        pageSyncMode?: 'sync' | 'async'
        updateActivePage?: (index: number) => void
    } = {},
): ModuleInstance {
    return {
        connection: options.connection,
        projectStore: {getProject: () => options.project},
        pageNavigationStore: {getActivePage: () => options.activePage ?? 0},
        config: {pageSyncMode: options.pageSyncMode ?? 'sync'},
        updateActivePage: options.updateActivePage ?? vi.fn(),
        log: vi.fn(),
    } as unknown as ModuleInstance
}

/** A minimal, validly-typed `ProjectDto` — only `metadata.volume` is relevant to the volume_change action. */
function fakeProject(volume: number | null | undefined): ProjectDto {
    return {
        metadata: {
            id: 'project-1',
            name: 'Test project',
            defaultColor: null,
            playColor: null,
            introColor: null,
            numberOfHorizontalPads: 1,
            numberOfVerticalPads: 1,
            volume,
        },
        pages: [],
    }
}

/** A project with `pageCount` pages (positions 0..pageCount-1) — volume/pad grid size are irrelevant here. */
function fakeProjectWithPages(pageCount: number): ProjectDto {
    return {
        ...fakeProject(1),
        pages: Array.from({length: pageCount}, (_, position) => ({
            id: `page-${position}`,
            position,
            settings: null,
            pads: [],
        })),
    }
}

/** `CompanionActionDefinitions` types each entry as possibly `false`/`undefined`; it never actually is here. */
function getStopAllAction(self: ModuleInstance) {
    const action = GetActionDefinitions(self)[STOP_ALL_ACTION_ID]
    if (!action) {
        throw new Error('stop_all action definition is missing')
    }
    return action
}

function getVolumeChangeAction(self: ModuleInstance) {
    const action = GetActionDefinitions(self)[VOLUME_CHANGE_ACTION_ID]
    if (!action) {
        throw new Error('volume_change action definition is missing')
    }
    return action
}

function getPageNavigateAction(self: ModuleInstance) {
    const action = GetActionDefinitions(self)[PAGE_NAVIGATE_ACTION_ID]
    if (!action) {
        throw new Error('page_navigate action definition is missing')
    }
    return action
}

function volumeActionEvent(options: VolumeActionOptions): CompanionActionEvent<VolumeActionOptions> {
    return {options} as CompanionActionEvent<VolumeActionOptions>
}

function pageActionEvent(options: PageActionOptions): CompanionActionEvent<PageActionOptions> {
    return {options} as CompanionActionEvent<PageActionOptions>
}

describe('stop_all action', () => {
    const STOP_ALL_EVENT = {} as CompanionActionEvent<Record<string, never>>

    it('has no configurable options', () => {
        const action = getStopAllAction(stubModuleInstance())

        expect(action.options).toEqual([])
    })

    it('stops all pads through the connection when pressed', () => {
        const stopAllPads = vi.fn()

        const action = getStopAllAction(stubModuleInstance({connection: {stopAllPads}}))
        action.callback(STOP_ALL_EVENT, ACTION_CONTEXT)

        expect(stopAllPads).toHaveBeenCalledTimes(1)
        expect(stopAllPads).toHaveBeenCalledWith()
    })

    it('does not throw when there is no active connection yet', () => {
        const action = getStopAllAction(stubModuleInstance())

        expect(() => action.callback(STOP_ALL_EVENT, ACTION_CONTEXT)).not.toThrow()
    })
})

describe('volume_change action', () => {
    it('offers a direction and a step-size dropdown', () => {
        const action = getVolumeChangeAction(stubModuleInstance())

        expect(action.options.map((option) => option.id)).toEqual(['mode', 'delta'])
    })

    it('warns and never touches the connection when no project is loaded', () => {
        const changeGlobalVolume = vi.fn()
        const action = getVolumeChangeAction(stubModuleInstance({connection: {changeGlobalVolume}}))

        action.callback(volumeActionEvent({mode: 'increase', delta: '5'}), ACTION_CONTEXT)

        expect(changeGlobalVolume).not.toHaveBeenCalled()
    })

    it('increases the current volume by the selected step and sends the new value', () => {
        const changeGlobalVolume = vi.fn()
        const action = getVolumeChangeAction(
            stubModuleInstance({connection: {changeGlobalVolume}, project: fakeProject(0.5)}),
        )

        action.callback(volumeActionEvent({mode: 'increase', delta: '5'}), ACTION_CONTEXT)

        expect(changeGlobalVolume).toHaveBeenCalledWith(0.55)
    })

    it('decreases the current volume by the selected step', () => {
        const changeGlobalVolume = vi.fn()
        const action = getVolumeChangeAction(
            stubModuleInstance({connection: {changeGlobalVolume}, project: fakeProject(0.5)}),
        )

        action.callback(volumeActionEvent({mode: 'decrease', delta: '10'}), ACTION_CONTEXT)

        expect(changeGlobalVolume).toHaveBeenCalledWith(0.4)
    })

    it('falls back to DEFAULT_VOLUME (1.0) when the project has no volume yet', () => {
        const changeGlobalVolume = vi.fn()
        const action = getVolumeChangeAction(
            stubModuleInstance({connection: {changeGlobalVolume}, project: fakeProject(null)}),
        )

        action.callback(volumeActionEvent({mode: 'decrease', delta: '5'}), ACTION_CONTEXT)

        expect(changeGlobalVolume).toHaveBeenCalledWith(0.95)
    })

    it('does not call the connection when already at the bound (no-op, matching GlobalVolumeActionHandler)', () => {
        const changeGlobalVolume = vi.fn()
        const action = getVolumeChangeAction(
            stubModuleInstance({connection: {changeGlobalVolume}, project: fakeProject(1)}),
        )

        action.callback(volumeActionEvent({mode: 'increase', delta: '10'}), ACTION_CONTEXT)

        expect(changeGlobalVolume).not.toHaveBeenCalled()
    })
})

describe('page_navigate action', () => {
    it('offers a mode dropdown and a page-number field', () => {
        const action = getPageNavigateAction(stubModuleInstance())

        expect(action.options.map((option) => option.id)).toEqual(['mode', 'pageNumber'])
    })

    it('warns and does nothing when no project is loaded', () => {
        const updateActivePage = vi.fn()
        const action = getPageNavigateAction(stubModuleInstance({updateActivePage}))

        action.callback(pageActionEvent({mode: 'next', pageNumber: 1}), ACTION_CONTEXT)

        expect(updateActivePage).not.toHaveBeenCalled()
    })

    it('warns and does nothing when there is no valid target (e.g. "next" from the last page)', () => {
        const updateActivePage = vi.fn()
        const showPage = vi.fn()
        const action = getPageNavigateAction(
            stubModuleInstance({
                project: fakeProjectWithPages(3),
                activePage: 2,
                updateActivePage,
                connection: {showPage},
            }),
        )

        action.callback(pageActionEvent({mode: 'next', pageNumber: 1}), ACTION_CONTEXT)

        expect(updateActivePage).not.toHaveBeenCalled()
        expect(showPage).not.toHaveBeenCalled()
    })

    it('updates the active page locally but does not notify the server in "async" mode', () => {
        const updateActivePage = vi.fn()
        const showPage = vi.fn()
        const action = getPageNavigateAction(
            stubModuleInstance({
                project: fakeProjectWithPages(3),
                activePage: 0,
                updateActivePage,
                connection: {showPage},
                pageSyncMode: 'async',
            }),
        )

        action.callback(pageActionEvent({mode: 'next', pageNumber: 1}), ACTION_CONTEXT)

        expect(updateActivePage).toHaveBeenCalledWith(1)
        expect(showPage).not.toHaveBeenCalled()
    })

    it('updates the active page and notifies the server in "sync" mode', () => {
        const updateActivePage = vi.fn()
        const showPage = vi.fn()
        const action = getPageNavigateAction(
            stubModuleInstance({
                project: fakeProjectWithPages(3),
                activePage: 0,
                updateActivePage,
                connection: {showPage},
                pageSyncMode: 'sync',
            }),
        )

        action.callback(pageActionEvent({mode: 'jump', pageNumber: 3}), ACTION_CONTEXT)

        expect(updateActivePage).toHaveBeenCalledWith(2)
        expect(showPage).toHaveBeenCalledWith(2)
    })
})
