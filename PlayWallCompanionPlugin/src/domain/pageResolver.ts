import type {PageDto, ProjectDto} from '../connection/protocol.js'

/** Finds the page at the given 1-based position (matching `PageDto.position`, which is 0-based). */
export function resolvePage(project: ProjectDto, pageNumber: number): PageDto | undefined {
    return project.pages.find((page) => page.position === pageNumber - 1)
}
