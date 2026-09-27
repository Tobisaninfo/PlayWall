import {describe, expect, it, vi} from 'vitest'
import ModuleInstance from './main.js'
import type {ProjectDto, ProjectMetadata} from './connection/protocol.js'
import type {ModuleConfig} from './config.js'

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
        const {instance, setVariableValues} = createModuleInstance()
        // projectStore was never seeded with a project.

        applyProjectSettingsChanged(instance, fakeMetadata(0.5))

        expect(setVariableValues).not.toHaveBeenCalled()
    })

    it('replaces the cached project metadata, so a later read sees the new volume', () => {
        const {instance} = createModuleInstance()
        instance.projectStore.setProject(fakeProject(0.5))

        applyProjectSettingsChanged(instance, fakeMetadata(0.9))

        expect(instance.projectStore.getProject()?.metadata.volume).toBe(0.9)
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
