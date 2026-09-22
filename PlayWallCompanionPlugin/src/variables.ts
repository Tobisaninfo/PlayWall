import type {CompanionVariableDefinitions} from '@companion-module/base'
import type {ProjectDto} from './connection/protocol.js'
import type ModuleInstance from './main.js'

export type VariablesSchema = {
    connection_status: string
    project_name: string
} & Record<string, string>

/**
 * Defines the static variables plus one `pad_name_<n>` and `pad_status_<n>` (1-based) variable per
 * pad slot on a page — the grid size is only known once a project has been loaded, so this is
 * re-called (via `self.updateVariableDefinitions()`) whenever that changes.
 */
export function UpdateVariableDefinitions(self: ModuleInstance, padsPerPage = 0): void {
    const definitions: CompanionVariableDefinitions<VariablesSchema> = {
        connection_status: {name: 'Connection status to the PlayWall server'},
        project_name: {name: 'Name of the project currently open in PlayWall'},
    }

    for (let position = 1; position <= padsPerPage; position++) {
        definitions[`pad_name_${position}`] = {name: `Name of the pad at position ${position} on the current page`}
        definitions[`pad_status_${position}`] = {name: `Playback status of the pad at position ${position} on the current page`}
    }

    self.setVariableDefinitions(definitions)
}

/**
 * Recomputes the `pad_name_<n>` and `pad_status_<n>` variable values from the given project's
 * currently shown page (0-based `pageIndex`, matching `PageDto.position`). Gaps in the grid, and pads
 * whose status hasn't been observed yet (e.g. empty pads, which never get a PadStatusUpdate), resolve
 * to an empty string.
 */
export function UpdatePadVariableValues(self: ModuleInstance, project: ProjectDto | undefined, pageIndex: number): void {
    if (!project) {
        return
    }

    const padsPerPage = project.metadata.numberOfHorizontalPads * project.metadata.numberOfVerticalPads
    const page = project.pages.find((page) => page.position === pageIndex)

    const values: Record<string, string> = {}
    for (let position = 1; position <= padsPerPage; position++) {
        const pad = page?.pads.find((pad) => pad.position === position - 1)
        values[`pad_name_${position}`] = pad?.name ?? ''
        values[`pad_status_${position}`] = (pad && self.playbackStore.getPadStatus(pad.id)) ?? ''
    }

    self.setVariableValues(values)
}
