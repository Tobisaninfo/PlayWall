import type {CompanionActionCallbackContext, CompanionActionEvent} from '@companion-module/base'
import {describe, expect, it, vi} from 'vitest'
import {GetActionDefinitions} from './actions.js'
import {PAD_PLAY_STOP_ACTION_ID, PAGE_NAVIGATE_ACTION_ID, STOP_ALL_ACTION_ID, VOLUME_CHANGE_ACTION_ID} from './ids.js'
import type {PadSelectorOptions} from './domain/padSelector.js'
import type {PageActionOptions} from './domain/pageSelector.js'
import type {VolumeActionOptions} from './domain/volumeControl.js'
import type {PadDto, PageDto, ProjectDto} from './connection/protocol.js'
import type ModuleInstance from './main.js'

const ACTION_CONTEXT = {} as CompanionActionCallbackContext

interface StubConnection {
    stopAllPads?: () => void
    changeGlobalVolume?: (volume: number) => void
    showPage?: (index: number) => void
    playPad?: (padId: string) => void
    stopPad?: (padId: string) => void
}

function stubModuleInstance(
    options: {
        connection?: StubConnection
        project?: ProjectDto
        activePage?: number
        pageSyncMode?: 'sync' | 'async'
        updateActivePage?: (index: number) => void
        padStatus?: string
    } = {},
): ModuleInstance {
    return {
        connection: options.connection,
        projectStore: {getProject: () => options.project},
        pageNavigationStore: {getActivePage: () => options.activePage ?? 0},
        playbackStore: {getPadStatus: () => options.padStatus},
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

/** A single-page project containing just the given pad, at position 0. */
function fakeProjectWithPad(pad: PadDto): ProjectDto {
    const page: PageDto = {id: 'page-0', position: 0, settings: null, pads: [pad]}
    return {...fakeProject(1), pages: [page]}
}

function fakePad(overrides: Partial<PadDto> = {}): PadDto {
    return {id: 'pad-1', position: 0, name: 'Pad', defaultColor: null, playColor: null, introColor: null, ...overrides}
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

function getPadPlayStopAction(self: ModuleInstance) {
    const action = GetActionDefinitions(self)[PAD_PLAY_STOP_ACTION_ID]
    if (!action) {
        throw new Error('pad_play_stop action definition is missing')
    }
    return action
}

function volumeActionEvent(options: VolumeActionOptions): CompanionActionEvent<VolumeActionOptions> {
    return {options} as CompanionActionEvent<VolumeActionOptions>
}

function pageActionEvent(options: PageActionOptions): CompanionActionEvent<PageActionOptions> {
    return {options} as CompanionActionEvent<PageActionOptions>
}

function padActionEvent(options: PadSelectorOptions): CompanionActionEvent<PadSelectorOptions> {
    return {options} as CompanionActionEvent<PadSelectorOptions>
}

const PAD_BY_NAME_OPTIONS: PadSelectorOptions = {mode: 'name', padName: 'Pad', padPosition: 1}

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

describe('pad_play_stop action', () => {
    it('offers a "select pad by" dropdown plus a name field and a position field', () => {
        const action = getPadPlayStopAction(stubModuleInstance())

        expect(action.options.map((option) => option.id)).toEqual(['mode', 'padName', 'padPosition'])
    })

    it('warns and never touches the connection when the pad cannot be resolved', () => {
        const playPad = vi.fn()
        const stopPad = vi.fn()
        const action = getPadPlayStopAction(stubModuleInstance({connection: {playPad, stopPad}}))

        action.callback(padActionEvent(PAD_BY_NAME_OPTIONS), ACTION_CONTEXT)

        expect(playPad).not.toHaveBeenCalled()
        expect(stopPad).not.toHaveBeenCalled()
    })

    it('plays the pad when it is not currently playing', () => {
        const playPad = vi.fn()
        const stopPad = vi.fn()
        const pad = fakePad({id: 'pad-1'})
        const action = getPadPlayStopAction(
            stubModuleInstance({
                connection: {playPad, stopPad},
                project: fakeProjectWithPad(pad),
                padStatus: 'STOPPED'
            }),
        )

        action.callback(padActionEvent(PAD_BY_NAME_OPTIONS), ACTION_CONTEXT)

        expect(playPad).toHaveBeenCalledWith('pad-1')
        expect(stopPad).not.toHaveBeenCalled()
    })

    it('stops the pad when it is currently playing', () => {
        const playPad = vi.fn()
        const stopPad = vi.fn()
        const pad = fakePad({id: 'pad-1'})
        const action = getPadPlayStopAction(
            stubModuleInstance({
                connection: {playPad, stopPad},
                project: fakeProjectWithPad(pad),
                padStatus: 'PLAYING'
            }),
        )

        action.callback(padActionEvent(PAD_BY_NAME_OPTIONS), ACTION_CONTEXT)

        expect(stopPad).toHaveBeenCalledWith('pad-1')
        expect(playPad).not.toHaveBeenCalled()
    })

    it('treats a pad with no observed status yet as not playing', () => {
        const playPad = vi.fn()
        const pad = fakePad({id: 'pad-1'})
        const action = getPadPlayStopAction(
            stubModuleInstance({connection: {playPad}, project: fakeProjectWithPad(pad), padStatus: undefined}),
        )

        action.callback(padActionEvent(PAD_BY_NAME_OPTIONS), ACTION_CONTEXT)

        expect(playPad).toHaveBeenCalledWith('pad-1')
    })
})
