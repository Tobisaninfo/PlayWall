import {describe, expect, it, vi} from 'vitest'
import ModuleInstance from './main.js'
import type {PadDto, PageDto, ProjectDto, ProjectMetadata} from './connection/protocol.js'
import type {ModuleConfig} from './config.js'
import {PAD_CURRENT_COLOR_FEEDBACK_ID, PAGE_CURRENT_COLOR_FEEDBACK_ID} from './ids.js'

/**
 * `InstanceBase`'s constructor only requires `{id: string, _isInstanceContext: true}` (see
 * `isInstanceContext` in `@companion-module/base`) plus whichever host-API methods the code path under
 * test actually calls — `setVariableValues`/`setVariableDefinitions`/`checkFeedbacks` here, since both
 * `handleProjectSettingsChanged` and `updateActivePage` also refresh pad/page variables and feedbacks
 * as a side effect. `ModuleInstance`'s own constructor takes `internal: unknown`, so no cast is needed
 * to pass this in.
 */
function createModuleInstance() {
    const setVariableValues = vi.fn()
    const setVariableDefinitions = vi.fn()
    const checkFeedbacks = vi.fn()

    const instance = new ModuleInstance({
        id: 'test-instance',
        _isInstanceContext: true,
        setVariableValues,
        setVariableDefinitions,
        checkFeedbacks,
    })

    return {instance, setVariableValues, setVariableDefinitions, checkFeedbacks}
}

const BASE_CONFIG: ModuleConfig = {
    host: 'localhost',
    port: 10023,
    useTls: false,
    reconnectDelaySeconds: 1,
    pageSyncMode: 'sync',
}

function fakeMetadata(volume: number | null | undefined): ProjectMetadata {
    return {
        id: 'project-1',
        name: 'Test project',
        defaultColor: null,
        playColor: null,
        introColor: null,
        numberOfHorizontalPads: 1,
        numberOfVerticalPads: 1,
        volume,
    }
}

function fakeProject(volume: number | null | undefined): ProjectDto {
    return {metadata: fakeMetadata(volume), pages: []}
}

function fakePad(id: string, position: number, name: string | null = null): PadDto {
    return {id, position, name, defaultColor: null, playColor: null, introColor: null}
}

/** A single-page project containing exactly the given pads, at position 0. */
function fakeProjectWithPads(pads: PadDto[]): ProjectDto {
    const page: PageDto = {id: 'page-0', position: 0, settings: null, pads}
    return {...fakeProject(1), pages: [page]}
}

/** `handleProjectSettingsChanged` is private; the restriction is compile-time only. */
function applyProjectSettingsChanged(instance: ModuleInstance, metadata: ProjectMetadata): void {
    ;(
        instance as unknown as { handleProjectSettingsChanged: (metadata: ProjectMetadata) => void }
    ).handleProjectSettingsChanged(metadata)
}

/** `handlePageShown` is private; the restriction is compile-time only. */
function applyPageShown(instance: ModuleInstance, index: number): void {
    ;(instance as unknown as { handlePageShown: (index: number) => void }).handlePageShown(index)
}

/** `handleProjectLoaded` is private; the restriction is compile-time only. */
function applyProjectLoaded(instance: ModuleInstance, project: ProjectDto): void {
    ;(instance as unknown as { handleProjectLoaded: (project: ProjectDto) => void }).handleProjectLoaded(project)
}

/** `handleProjectCleared` is private; the restriction is compile-time only. */
function applyProjectCleared(instance: ModuleInstance): void {
    ;(instance as unknown as { handleProjectCleared: () => void }).handleProjectCleared()
}

/** `handlePadStatus` is private; the restriction is compile-time only. */
function applyPadStatus(instance: ModuleInstance, padId: string, status: string): void {
    ;(instance as unknown as {
        handlePadStatus: (padId: string, status: string) => void
    }).handlePadStatus(padId, status)
}

/** `handlePadStatuses` is private; the restriction is compile-time only. */
function applyPadStatuses(instance: ModuleInstance, statuses: Record<string, string>): void {
    ;(
        instance as unknown as { handlePadStatuses: (statuses: Record<string, string>) => void }
    ).handlePadStatuses(statuses)
}

/** `handlePadReplaced` is private; the restriction is compile-time only. */
function applyPadReplaced(instance: ModuleInstance, newPad: PadDto, targetPadId: string): void {
    ;(
        instance as unknown as { handlePadReplaced: (newPad: PadDto, targetPadId: string) => void }
    ).handlePadReplaced(newPad, targetPadId)
}

/** `handlePadsSwapped` is private; the restriction is compile-time only. */
function applyPadsSwapped(instance: ModuleInstance, padId1: string, padId2: string): void {
    ;(
        instance as unknown as { handlePadsSwapped: (padId1: string, padId2: string) => void }
    ).handlePadsSwapped(padId1, padId2)
}

/** `handlePagesChanged` is private; the restriction is compile-time only. */
function applyPagesChanged(instance: ModuleInstance, project: ProjectDto): void {
    ;(instance as unknown as { handlePagesChanged: (project: ProjectDto) => void }).handlePagesChanged(project)
}

describe('ModuleInstance.handleProjectSettingsChanged', () => {
    it('updates current_volume/current_volume_percent from the broadcast metadata', () => {
        const {instance, setVariableValues} = createModuleInstance()
        instance.projectStore.setProject(fakeProject(0.5))

        applyProjectSettingsChanged(instance, fakeMetadata(0.75))

        expect(setVariableValues).toHaveBeenCalledWith(
            expect.objectContaining({current_volume: '0.75', current_volume_percent: '75%'}),
        )
    })

    it('falls back to DEFAULT_VOLUME (1.0/100%) once the broadcast metadata has no volume yet', () => {
        const {instance, setVariableValues} = createModuleInstance()
        instance.projectStore.setProject(fakeProject(0.5))

        applyProjectSettingsChanged(instance, fakeMetadata(null))

        expect(setVariableValues).toHaveBeenCalledWith(
            expect.objectContaining({current_volume: '1', current_volume_percent: '100%'}),
        )
    })

    it('does nothing when no project is loaded yet', () => {
        const {instance, setVariableValues, setVariableDefinitions, checkFeedbacks} = createModuleInstance()
        // projectStore was never seeded with a project.

        applyProjectSettingsChanged(instance, fakeMetadata(0.5))

        expect(setVariableValues).not.toHaveBeenCalled()
        expect(setVariableDefinitions).not.toHaveBeenCalled()
        expect(checkFeedbacks).not.toHaveBeenCalled()
    })

    it('replaces the cached project metadata, so a later read sees the new volume', () => {
        const {instance} = createModuleInstance()
        instance.projectStore.setProject(fakeProject(0.5))

        applyProjectSettingsChanged(instance, fakeMetadata(0.9))

        expect(instance.projectStore.getProject()?.metadata.volume).toBe(0.9)
    })

    it('re-checks the pad/page color feedbacks, so a changed defaultColor/playColor actually repaints buttons', () => {
        const {instance, checkFeedbacks} = createModuleInstance()
        instance.projectStore.setProject(fakeProject(0.5))

        applyProjectSettingsChanged(instance, {...fakeMetadata(0.5), defaultColor: 'RED1', playColor: 'BLUE1'})

        expect(checkFeedbacks).toHaveBeenCalledWith([PAD_CURRENT_COLOR_FEEDBACK_ID, PAGE_CURRENT_COLOR_FEEDBACK_ID])
    })

    it('re-sizes the pad variable definitions, in case the pad grid dimensions changed too', () => {
        const {instance, setVariableDefinitions} = createModuleInstance()
        instance.projectStore.setProject(fakeProject(0.5))

        applyProjectSettingsChanged(instance, {
            ...fakeMetadata(0.5),
            numberOfHorizontalPads: 3,
            numberOfVerticalPads: 2
        })

        const definitions = setVariableDefinitions.mock.calls.at(-1)?.[0] as Record<string, unknown>
        expect(definitions).toHaveProperty('pad_name_6')
        expect(definitions).not.toHaveProperty('pad_name_7')
    })
})

describe('ModuleInstance.updateActivePage', () => {
    it('updates the active page in PageNavigationStore', () => {
        const {instance} = createModuleInstance()

        instance.updateActivePage(3)

        expect(instance.pageNavigationStore.getActivePage()).toBe(3)
    })

    it('refreshes page_active (1-based) once a project is loaded', () => {
        const {instance, setVariableValues} = createModuleInstance()
        instance.projectStore.setProject(fakeProject(0.5))

        instance.updateActivePage(2)

        expect(setVariableValues).toHaveBeenCalledWith(expect.objectContaining({page_active: '3'}))
    })
})

describe('ModuleInstance.handlePageShown (private, invoked via the connection callback)', () => {
    it('follows PlayWall\'s own shown page in "sync" mode', () => {
        const {instance} = createModuleInstance()
        instance.config = {...BASE_CONFIG, pageSyncMode: 'sync'}

        applyPageShown(instance, 5)

        expect(instance.pageNavigationStore.getActivePage()).toBe(5)
    })

    it('ignores PlayWall\'s own shown page in "async" mode, keeping Companion\'s own active page', () => {
        const {instance} = createModuleInstance()
        instance.config = {...BASE_CONFIG, pageSyncMode: 'async'}
        instance.updateActivePage(2)

        applyPageShown(instance, 5)

        expect(instance.pageNavigationStore.getActivePage()).toBe(2)
    })
})

describe('ModuleInstance.handleProjectLoaded (private, invoked via the connection callback)', () => {
    it('stores the project so it can be read back later', () => {
        const {instance} = createModuleInstance()
        const project = fakeProject(0.5)

        applyProjectLoaded(instance, project)

        expect(instance.projectStore.getProject()).toBe(project)
    })

    it('resets Companion\'s own active page back to the first page', () => {
        const {instance} = createModuleInstance()
        instance.updateActivePage(3)

        applyProjectLoaded(instance, fakeProject(0.5))

        expect(instance.pageNavigationStore.getActivePage()).toBe(0)
    })

    it('sets project_name and the volume variables from the newly loaded project', () => {
        const {instance, setVariableValues} = createModuleInstance()
        const project: ProjectDto = {...fakeProject(0.5), metadata: {...fakeMetadata(0.5), name: 'My Project'}}

        applyProjectLoaded(instance, project)

        expect(setVariableValues).toHaveBeenCalledWith(
            expect.objectContaining({project_name: 'My Project', current_volume: '0.5', current_volume_percent: '50%'}),
        )
    })

    it('re-checks the pad/page color feedbacks, so buttons show the new project\'s colors', () => {
        const {instance, checkFeedbacks} = createModuleInstance()

        applyProjectLoaded(instance, fakeProject(0.5))

        expect(checkFeedbacks).toHaveBeenCalledWith([PAD_CURRENT_COLOR_FEEDBACK_ID, PAGE_CURRENT_COLOR_FEEDBACK_ID])
    })
})

describe('ModuleInstance.handleProjectCleared (private, invoked via the connection callback)', () => {
    it('clears the stored project', () => {
        const {instance} = createModuleInstance()
        instance.projectStore.setProject(fakeProject(0.5))

        applyProjectCleared(instance)

        expect(instance.projectStore.getProject()).toBeUndefined()
    })

    it('resets project_name and the volume variables to blank', () => {
        const {instance, setVariableValues} = createModuleInstance()
        instance.projectStore.setProject(fakeProject(0.5))

        applyProjectCleared(instance)

        expect(setVariableValues).toHaveBeenCalledWith({
            project_name: '',
            current_volume: '',
            current_volume_percent: '',
        })
    })

    it('resets Companion\'s own active page back to the first page', () => {
        const {instance} = createModuleInstance()
        instance.projectStore.setProject(fakeProject(0.5))
        instance.updateActivePage(3)

        applyProjectCleared(instance)

        expect(instance.pageNavigationStore.getActivePage()).toBe(0)
    })

    it('re-checks the pad/page color feedbacks, so buttons go back to their unconfigured look', () => {
        const {instance, checkFeedbacks} = createModuleInstance()
        instance.projectStore.setProject(fakeProject(0.5))

        applyProjectCleared(instance)

        expect(checkFeedbacks).toHaveBeenCalledWith([PAD_CURRENT_COLOR_FEEDBACK_ID, PAGE_CURRENT_COLOR_FEEDBACK_ID])
    })
})

describe('ModuleInstance.handlePadStatus (private, invoked via the connection callback)', () => {
    it('records the status so it is reflected in pad_status_<n>', () => {
        const {instance, setVariableValues} = createModuleInstance()
        instance.projectStore.setProject(fakeProjectWithPads([fakePad('pad-1', 0)]))

        applyPadStatus(instance, 'pad-1', 'PLAYING')

        expect(instance.playbackStore.getPadStatus('pad-1')).toBe('PLAYING')
        expect(setVariableValues).toHaveBeenCalledWith(expect.objectContaining({pad_status_1: 'PLAYING'}))
    })

    it('re-checks the pad color feedback, so the button repaints with the new play/default color', () => {
        const {instance, checkFeedbacks} = createModuleInstance()
        instance.projectStore.setProject(fakeProjectWithPads([fakePad('pad-1', 0)]))

        applyPadStatus(instance, 'pad-1', 'PLAYING')

        expect(checkFeedbacks).toHaveBeenCalledWith([PAD_CURRENT_COLOR_FEEDBACK_ID])
    })
})

describe('ModuleInstance.handlePadStatuses (private, invoked via the connection callback)', () => {
    it('bulk-seeds every pad status at once, e.g. right after (re)connecting', () => {
        const {instance} = createModuleInstance()

        applyPadStatuses(instance, {'pad-1': 'PLAYING', 'pad-2': 'STOPPED'})

        expect(instance.playbackStore.getPadStatus('pad-1')).toBe('PLAYING')
        expect(instance.playbackStore.getPadStatus('pad-2')).toBe('STOPPED')
    })

    it('re-checks the pad color feedback for the freshly seeded statuses', () => {
        const {instance, checkFeedbacks} = createModuleInstance()

        applyPadStatuses(instance, {'pad-1': 'PLAYING'})

        expect(checkFeedbacks).toHaveBeenCalledWith([PAD_CURRENT_COLOR_FEEDBACK_ID])
    })
})

describe('ModuleInstance.handlePadReplaced (private, invoked via the connection callback)', () => {
    it('replaces the pad in the cached project (e.g. after a drag & drop move/duplicate)', () => {
        const {instance} = createModuleInstance()
        instance.projectStore.setProject(fakeProjectWithPads([fakePad('pad-1', 0, 'Old')]))

        applyPadReplaced(instance, fakePad('pad-2', 0, 'New'), 'pad-1')

        expect(instance.projectStore.getProject()?.pages[0]?.pads.map((pad) => pad.id)).toEqual(['pad-2'])
    })

    it('re-checks the pad color feedback, so the replaced pad shows its own colors', () => {
        const {instance, checkFeedbacks} = createModuleInstance()
        instance.projectStore.setProject(fakeProjectWithPads([fakePad('pad-1', 0, 'Old')]))

        applyPadReplaced(instance, fakePad('pad-2', 0, 'New'), 'pad-1')

        expect(checkFeedbacks).toHaveBeenCalledWith([PAD_CURRENT_COLOR_FEEDBACK_ID])
    })

    it('does nothing when no project is loaded yet', () => {
        const {instance, setVariableValues, checkFeedbacks} = createModuleInstance()

        applyPadReplaced(instance, fakePad('pad-2', 0, 'New'), 'pad-1')

        expect(setVariableValues).not.toHaveBeenCalled()
        expect(checkFeedbacks).not.toHaveBeenCalled()
    })
})

describe('ModuleInstance.handlePadsSwapped (private, invoked via the connection callback)', () => {
    it('swaps the two pads\' positions in the cached project', () => {
        const {instance} = createModuleInstance()
        instance.projectStore.setProject(fakeProjectWithPads([fakePad('pad-1', 0), fakePad('pad-2', 1)]))

        applyPadsSwapped(instance, 'pad-1', 'pad-2')

        const pads = instance.projectStore.getProject()?.pages[0]?.pads ?? []
        expect(pads.find((pad) => pad.id === 'pad-1')?.position).toBe(1)
        expect(pads.find((pad) => pad.id === 'pad-2')?.position).toBe(0)
    })

    it('re-checks the pad color feedback for both swapped pads\' new positions', () => {
        const {instance, checkFeedbacks} = createModuleInstance()
        instance.projectStore.setProject(fakeProjectWithPads([fakePad('pad-1', 0), fakePad('pad-2', 1)]))

        applyPadsSwapped(instance, 'pad-1', 'pad-2')

        expect(checkFeedbacks).toHaveBeenCalledWith([PAD_CURRENT_COLOR_FEEDBACK_ID])
    })

    it('does nothing when no project is loaded yet', () => {
        const {instance, setVariableValues, checkFeedbacks} = createModuleInstance()

        applyPadsSwapped(instance, 'pad-1', 'pad-2')

        expect(setVariableValues).not.toHaveBeenCalled()
        expect(checkFeedbacks).not.toHaveBeenCalled()
    })
})

describe('ModuleInstance.handlePagesChanged (private, invoked via the connection callback)', () => {
    it('re-stores the freshly re-fetched project after a page add/delete/reorder/rename', () => {
        const {instance} = createModuleInstance()
        const project = fakeProject(0.5)

        applyPagesChanged(instance, project)

        expect(instance.projectStore.getProject()).toBe(project)
    })

    it('does not reset Companion\'s own active page (unlike handleProjectLoaded, this isn\'t a new project)', () => {
        const {instance} = createModuleInstance()
        instance.updateActivePage(3)

        applyPagesChanged(instance, fakeProject(0.5))

        expect(instance.pageNavigationStore.getActivePage()).toBe(3)
    })

    it('re-checks the pad/page color feedbacks, so a renamed/recolored page repaints its button', () => {
        const {instance, checkFeedbacks} = createModuleInstance()

        applyPagesChanged(instance, fakeProject(0.5))

        expect(checkFeedbacks).toHaveBeenCalledWith([PAD_CURRENT_COLOR_FEEDBACK_ID, PAGE_CURRENT_COLOR_FEEDBACK_ID])
    })
})
