import type {CompanionVariableDefinitions} from '@companion-module/base'
import type {ProjectDto} from './connection/protocol.js'
import type ModuleInstance from './main.js'

export type VariablesSchema = {
    connection_status: string
    project_name: string
} & Record<string, string>

/**
 * Defines the static variables plus one `pad_name_<n>` and `pad_status_<n>` (1-based) variable per
 * pad slot on a page, and one `page_name_<n>` (1-based) variable per page in the project — the grid
 * size and page count are only known once a project has been loaded, so this is re-called (via
 * `self.updateVariableDefinitions()`) whenever either changes.
 */
export function UpdateVariableDefinitions(self: ModuleInstance, padsPerPage = 0, pageCount = 0): void {
    const definitions: CompanionVariableDefinitions<VariablesSchema> = {
        connection_status: {name: 'Connection status to the PlayWall server'},
        project_name: {name: 'Name of the project currently open in PlayWall'},
        page_active: {name: 'Position of the currently active page'},
        current_volume: {name: "PlayWall's current global volume (0-1)"},
    }

    for (let position = 1; position <= padsPerPage; position++) {
        definitions[`pad_name_${position}`] = {name: `Name of the pad at position ${position} on Companion's active page`}
        definitions[`pad_status_${position}`] = {name: `Playback status of the pad at position ${position} on Companion's active page`}
    }

    for (let position = 1; position <= pageCount; position++) {
        definitions[`page_name_${position}`] = {name: `Name of the page at position ${position}`}
    }

    self.setVariableDefinitions(definitions)
}

/**
 * Recomputes the `pad_name_<n>` and `pad_status_<n>` variable values from the given project's pads on
 * Companion's own active page (0-based `pageIndex`, matching `PageDto.position` — see
 * `PageNavigationStore`; in "sync" mode this is also whatever page PlayWall itself shows). Gaps in the
 * grid, and pads whose status hasn't been observed yet (e.g. empty pads, which never get a
 * PadStatusUpdate), resolve to an empty string.
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

/**
 * Recomputes `page_active` (1-based) and every `page_name_<n>` value from the given project and
 * Companion's own active page index (0-based, matching `PageDto.position`).
 */
export function UpdatePageVariableValues(self: ModuleInstance, project: ProjectDto | undefined, activePageIndex: number): void {
    if (!project) {
        return
    }

    const values: Record<string, string> = {
        page_active: String(activePageIndex + 1),
    }

    for (let position = 1; position <= project.pages.length; position++) {
        const page = project.pages.find((page) => page.position === position - 1)
        values[`page_name_${position}`] = page?.settings?.name ?? ''
    }

    self.setVariableValues(values)
}
