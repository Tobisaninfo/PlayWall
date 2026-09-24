import {InstanceBase, InstanceStatus, type SomeCompanionConfigField} from '@companion-module/base'
import {GetActionDefinitions} from './actions.js'
import {GetConfigFields, type ModuleConfig} from './config.js'
import {GetFeedbackDefinitions} from './feedbacks.js'
import {GetPresetDefinitions, GetPresetSections} from './presets.js'
import {
    UpdatePadVariableValues,
    UpdatePageVariableValues,
    UpdateVariableDefinitions,
    type VariablesSchema
} from './variables.js'
import {UpgradeScripts} from './upgrades.js'
import {ClientWebSocketHandler, type ConnectionStatus} from './connection/ClientWebSocketHandler.js'
import type {PadDto, ProjectDto} from './connection/protocol.js'
import type {PadSelectorOptions} from './domain/padSelector.js'
import type {PageActionOptions, PageFeedbackOptions} from './domain/pageSelector.js'
import {replacePadInProject, swapPadsInProject} from './domain/projectMutations.js'
import {PageNavigationStore} from './domain/pageNavigationStore.js'
import {PlaybackStore} from './domain/playbackStore.js'
import {ProjectStore} from './domain/projectStore.js'
import {
    PAD_CURRENT_COLOR_FEEDBACK_ID,
    PAD_PLAY_STOP_ACTION_ID,
    PAGE_CURRENT_COLOR_FEEDBACK_ID,
    PAGE_NAVIGATE_ACTION_ID
} from './ids.js'

export type ModuleSchema = {
    config: ModuleConfig
    secrets: undefined
    actions: {
        [PAD_PLAY_STOP_ACTION_ID]: { options: PadSelectorOptions }
        [PAGE_NAVIGATE_ACTION_ID]: { options: PageActionOptions }
    }
    feedbacks: {
        [PAD_CURRENT_COLOR_FEEDBACK_ID]: { type: 'advanced'; options: PadSelectorOptions }
        [PAGE_CURRENT_COLOR_FEEDBACK_ID]: { type: 'advanced'; options: PageFeedbackOptions }
    }
    variables: VariablesSchema
}

export {UpgradeScripts}

export default class ModuleInstance extends InstanceBase<ModuleSchema> {
    config!: ModuleConfig
    connection: ClientWebSocketHandler | undefined
    readonly projectStore = new ProjectStore()
    readonly playbackStore = new PlaybackStore()
    readonly pageNavigationStore = new PageNavigationStore()

    constructor(internal: unknown) {
        super(internal)
    }

    async init(config: ModuleConfig): Promise<void> {
        this.config = config

        this.updateVariableDefinitions()
        this.setActionDefinitions(GetActionDefinitions(this))
        this.setFeedbackDefinitions(GetFeedbackDefinitions(this))
        this.setPresetDefinitions(GetPresetSections(), GetPresetDefinitions())

        this.connectToServer()
    }

    async destroy(): Promise<void> {
        this.connection?.destroy()
        this.connection = undefined
    }

    async configUpdated(config: ModuleConfig): Promise<void> {
        this.config = config
        this.connectToServer()
    }

    getConfigFields(): SomeCompanionConfigField[] {
        return GetConfigFields()
    }

    updateVariableDefinitions(): void {
        const project = this.projectStore.getProject()
        const padsPerPage = project ? project.metadata.numberOfHorizontalPads * project.metadata.numberOfVerticalPads : 0
        const pageCount = project ? project.pages.length : 0
        UpdateVariableDefinitions(this, padsPerPage, pageCount)
    }

    private connectToServer(): void {
        this.connection?.destroy()

        this.connection = new ClientWebSocketHandler({
            host: this.config.host,
            port: this.config.port,
            useTls: this.config.useTls,
            reconnectDelaySeconds: this.config.reconnectDelaySeconds,
            onStatusChange: (status) => this.handleStatusChange(status),
            onProjectLoaded: (project) => this.handleProjectLoaded(project),
            onProjectCleared: () => this.handleProjectCleared(),
            onPageShown: (index) => this.handlePageShown(index),
            onPadStatus: (padId, status) => this.handlePadStatus(padId, status),
            onPadStatuses: (statuses) => this.handlePadStatuses(statuses),
            onPadReplaced: (newPad, targetPadId) => this.handlePadReplaced(newPad, targetPadId),
            onPadsSwapped: (padId1, padId2) => this.handlePadsSwapped(padId1, padId2),
            onPagesChanged: (project) => this.handlePagesChanged(project),
            log: (level, message) => this.log(level, message),
        })
        this.connection.connect()
    }

    private handleProjectLoaded(project: ProjectDto): void {
        this.projectStore.setProject(project)
        this.pageNavigationStore.resetPage()
        this.setVariableValues({project_name: project.metadata.name})
        this.updateVariableDefinitions()
        this.refreshPadVariableValues()
        this.refreshPageVariableValues()
        this.checkFeedbacks(PAD_CURRENT_COLOR_FEEDBACK_ID, PAGE_CURRENT_COLOR_FEEDBACK_ID)
    }

    private handleProjectCleared(): void {
        this.projectStore.clear()
        this.playbackStore.clear()
        this.pageNavigationStore.resetPage()
        this.setVariableValues({project_name: ''})
        this.updateVariableDefinitions()
        this.checkFeedbacks(PAD_CURRENT_COLOR_FEEDBACK_ID, PAGE_CURRENT_COLOR_FEEDBACK_ID)
    }

    /**
     * PlayWall's own shown page changed. Pad actions/feedback/variables ("index" mode) are tied to
     * Companion's own active page (`PageNavigationStore`), not directly to this — so PlayWall's page
     * only affects Companion at all in "sync" config mode, via `updateActivePage`. In "async" mode
     * this is a pure no-op: pad and page actions stay fully decoupled from PlayWall.
     */
    private handlePageShown(index: number): void {
        if (this.config.pageSyncMode === 'sync') {
            this.updateActivePage(index)
        }
    }

    /**
     * A page was added/deleted/inserted/reordered/replaced/renamed on the desktop client: refresh the
     * cached project and the page variables/feedback, without touching playback or navigation state
     * (unlike `handleProjectLoaded`, this isn't a new project).
     */
    private handlePagesChanged(project: ProjectDto): void {
        this.projectStore.setProject(project)
        this.updateVariableDefinitions()
        this.refreshPadVariableValues()
        this.refreshPageVariableValues()
        this.checkFeedbacks(PAD_CURRENT_COLOR_FEEDBACK_ID, PAGE_CURRENT_COLOR_FEEDBACK_ID)
    }

    /**
     * Sets Companion's own active page (see `PageNavigationStore`) and refreshes everything tied to
     * it — both page state *and* pad state ("index" mode pad actions/feedback/variables resolve
     * against this same active page, see `domain/padSelector.ts`), so the two stay linked together.
     */
    updateActivePage(index: number): void {
        this.pageNavigationStore.setActivePage(index)
        this.refreshPageVariableValues()
        this.refreshPadVariableValues()
        this.checkFeedbacks(PAGE_CURRENT_COLOR_FEEDBACK_ID, PAD_CURRENT_COLOR_FEEDBACK_ID)
    }

    private handlePadStatus(padId: string, status: string): void {
        this.playbackStore.setPadStatus(padId, status)
        this.refreshPadVariableValues()
        this.checkFeedbacks(PAD_CURRENT_COLOR_FEEDBACK_ID)
    }

    private handlePadStatuses(statuses: Record<string, string>): void {
        this.playbackStore.setPadStatuses(statuses)
        this.refreshPadVariableValues()
        this.checkFeedbacks(PAD_CURRENT_COLOR_FEEDBACK_ID)
    }

    private handlePadReplaced(newPad: PadDto, targetPadId: string): void {
        const project = this.projectStore.getProject()
        if (!project) {
            return
        }

        this.projectStore.setProject(replacePadInProject(project, newPad, targetPadId))
        this.refreshPadVariableValues()
        this.checkFeedbacks(PAD_CURRENT_COLOR_FEEDBACK_ID)
    }

    private handlePadsSwapped(padId1: string, padId2: string): void {
        const project = this.projectStore.getProject()
        if (!project) {
            return
        }

        this.projectStore.setProject(swapPadsInProject(project, padId1, padId2))
        this.refreshPadVariableValues()
        this.checkFeedbacks(PAD_CURRENT_COLOR_FEEDBACK_ID)
    }

    private refreshPadVariableValues(): void {
        UpdatePadVariableValues(this, this.projectStore.getProject(), this.pageNavigationStore.getActivePage())
    }

    private refreshPageVariableValues(): void {
        UpdatePageVariableValues(this, this.projectStore.getProject(), this.pageNavigationStore.getActivePage())
    }

    private handleStatusChange(status: ConnectionStatus): void {
        switch (status) {
            case 'connecting':
                this.updateStatus(InstanceStatus.Connecting)
                break
            case 'connected':
                this.updateStatus(InstanceStatus.Ok)
                break
            case 'disconnected':
                this.updateStatus(InstanceStatus.ConnectionFailure)
                break
        }

        this.setVariableValues({connection_status: status})
    }
}
