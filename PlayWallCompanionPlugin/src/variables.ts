import type ModuleInstance from './main.js'

export type VariablesSchema = {
    connection_status: string
    project_name: string
}

export function UpdateVariableDefinitions(self: ModuleInstance): void {
    self.setVariableDefinitions({
        connection_status: {name: 'Connection status to the PlayWall server'},
        project_name: {name: 'Name of the project currently open in PlayWall'},
    })
}
