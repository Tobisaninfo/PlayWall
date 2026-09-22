import {InstanceBase, InstanceStatus, type SomeCompanionConfigField} from '@companion-module/base'
import {GetConfigFields, type ModuleConfig} from './config.js'
import {UpdateVariableDefinitions, type VariablesSchema} from './variables.js'
import {UpgradeScripts} from './upgrades.js'
import {ClientWebSocketHandler, type ConnectionStatus} from './connection/ClientWebSocketHandler.js'
import type {ProjectDto} from './connection/protocol.js'
import {ProjectStore} from './domain/projectStore.js'

export type ModuleSchema = {
    config: ModuleConfig
    secrets: undefined
    actions: Record<string, never>
    feedbacks: Record<string, never>
    variables: VariablesSchema
}

export {UpgradeScripts}

export default class ModuleInstance extends InstanceBase<ModuleSchema> {
    config!: ModuleConfig
    connection: ClientWebSocketHandler | undefined
    readonly projectStore = new ProjectStore()

    constructor(internal: unknown) {
        super(internal)
    }

    async init(config: ModuleConfig): Promise<void> {
        this.config = config

        this.updateVariableDefinitions()

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
        UpdateVariableDefinitions(this)
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
            log: (level, message) => this.log(level, message),
        })
        this.connection.connect()
    }

    private handleProjectLoaded(project: ProjectDto): void {
        this.projectStore.setProject(project)
        this.setVariableValues({project_name: project.metadata.name})
    }

    private handleProjectCleared(): void {
        this.projectStore.clear()
        this.setVariableValues({project_name: ''})
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
