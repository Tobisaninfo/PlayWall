import type {PadDto, ProjectDto} from '../connection/protocol.js'

/**
 * Finds the pad with the exact given name, searching all pages in order (then pads in page order).
 * If multiple pads share the same name — not prevented anywhere in PlayWall — the first match wins;
 * this is a known limitation of "by name" lookup.
 */
export function findPadByName(project: ProjectDto, name: string): PadDto | undefined {
    for (const page of project.pages) {
        const pad = page.pads.find((pad) => pad.name === name)
        if (pad) {
            return pad
        }
    }
    return undefined
}

/**
 * Finds the pad at the given 0-based position on the given 0-based page index (matching
 * PadDto.position/PageDto.position on the wire).
 */
export function findPadByPosition(project: ProjectDto, pageIndex: number, position: number): PadDto | undefined {
    const page = project.pages.find((page) => page.position === pageIndex)
    return page?.pads.find((pad) => pad.position === position)
}
