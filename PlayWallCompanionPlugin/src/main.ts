import {InstanceBase, InstanceStatus, type SomeCompanionConfigField} from '@companion-module/base'
import {GetActionDefinitions} from './actions.js'
import {GetConfigFields, type ModuleConfig} from './config.js'
import {GetFeedbackDefinitions} from './feedbacks.js'
import {UpdatePadNameVariableValues, UpdateVariableDefinitions, type VariablesSchema} from './variables.js'
import {UpgradeScripts} from './upgrades.js'
import {ClientWebSocketHandler, type ConnectionStatus} from './connection/ClientWebSocketHandler.js'
import type {ProjectDto} from './connection/protocol.js'
import type {PadSelectorOptions} from './domain/padSelector.js'
import {PlaybackStore} from './domain/playbackStore.js'
import {ProjectStore} from './domain/projectStore.js'
import {PAD_CURRENT_COLOR_FEEDBACK_ID, PAD_PLAY_STOP_ACTION_ID} from './ids.js'

export type ModuleSchema = {
    config: ModuleConfig
    secrets: undefined
    actions: {
        [PAD_PLAY_STOP_ACTION_ID]: { options: PadSelectorOptions }
    }
    feedbacks: {
        [PAD_CURRENT_COLOR_FEEDBACK_ID]: { type: 'advanced'; options: PadSelectorOptions }
    }
    variables: VariablesSchema
}

export {UpgradeScripts}

export default class ModuleInstance extends InstanceBase<ModuleSchema> {
    config!: ModuleConfig
    connection: ClientWebSocketHandler | undefined
    readonly projectStore = new ProjectStore()
    readonly playbackStore = new PlaybackStore()

    constructor(internal: unknown) {
        super(internal)
    }

    async init(config: ModuleConfig): Promise<void> {
        this.config = config

        this.updateVariableDefinitions()
        this.setActionDefinitions(GetActionDefinitions(this))
        this.setFeedbackDefinitions(GetFeedbackDefinitions(this))

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
        UpdateVariableDefinitions(this, padsPerPage)
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
            log: (level, message) => this.log(level, message),
        })
        this.connection.connect()
    }

    private handleProjectLoaded(project: ProjectDto): void {
        this.projectStore.setProject(project)
        this.playbackStore.clear()
        this.setVariableValues({project_name: project.metadata.name})
        this.updateVariableDefinitions()
        this.refreshPadNameVariableValues()
        this.checkFeedbacks(PAD_CURRENT_COLOR_FEEDBACK_ID)
    }

    private handleProjectCleared(): void {
        this.projectStore.clear()
        this.playbackStore.clear()
        this.setVariableValues({project_name: ''})
        this.updateVariableDefinitions()
        this.checkFeedbacks(PAD_CURRENT_COLOR_FEEDBACK_ID)
    }

    private handlePageShown(index: number): void {
        this.playbackStore.setCurrentPageIndex(index)
        this.refreshPadNameVariableValues()
        this.checkFeedbacks(PAD_CURRENT_COLOR_FEEDBACK_ID)
    }

    private handlePadStatus(padId: string, status: string): void {
        this.playbackStore.setPadStatus(padId, status)
        this.checkFeedbacks(PAD_CURRENT_COLOR_FEEDBACK_ID)
    }

    private refreshPadNameVariableValues(): void {
        UpdatePadNameVariableValues(this, this.projectStore.getProject(), this.playbackStore.getCurrentPageIndex())
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
