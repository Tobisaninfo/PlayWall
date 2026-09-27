import type {PadDto, PageDto, ProjectDto} from '../connection/protocol.js'

interface LocatedPad {
    page: PageDto
    pad: PadDto
}

function locatePad(project: ProjectDto, padId: string): LocatedPad | undefined {
    for (const page of project.pages) {
        const pad = page.pads.find((pad) => pad.id === padId)
        if (pad) {
            return {page, pad}
        }
    }
    return undefined
}

/**
 * Replaces the pad with id `targetPadId` (wherever it currently is) with `newPad` — mirrors the
 * server's `PadReplaceUpdate` broadcast, fired when a pad is moved onto, or duplicated onto, another
 * pad's slot via drag & drop on the desktop client. Returns a new `ProjectDto`; the pads array of
 * every other page is left untouched (same array reference).
 */
export function replacePadInProject(project: ProjectDto, newPad: PadDto, targetPadId: string): ProjectDto {
    const located = locatePad(project, targetPadId)
    if (!located) {
        return project
    }

    return {
        ...project,
        pages: project.pages.map((page) =>
            page === located.page
                ? {...page, pads: page.pads.map((pad) => (pad.id === targetPadId ? newPad : pad))}
                : page,
        ),
    }
}

/**
 * Updates the pad with `updatedPad.id` (wherever it currently is) in place with its fresh data —
 * mirrors the server's `PadUpdate` broadcast, fired whenever a pad's own settings (name, colors, ...)
 * are edited via PlayWall's Pad Settings dialog. The id (and therefore position/page) never changes for
 * this broadcast, unlike `PadReplaceUpdate`, so this is really just `replacePadInProject` with the
 * pad replacing itself — kept as its own named function since the two broadcasts mean different things.
 * Returns a new `ProjectDto`; if the pad can't be found, the project is returned unchanged.
 */
export function updatePadInProject(project: ProjectDto, updatedPad: PadDto): ProjectDto {
    return replacePadInProject(project, updatedPad, updatedPad.id)
}

/**
 * Swaps the positions — and, if they were on different pages, the page membership too — of the two
 * given pads. Mirrors the server's `PadSwapUpdate` broadcast / `ProjectController.swapPad()`. Returns
 * a new `ProjectDto`; if either pad can't be found, the project is returned unchanged.
 */
export function swapPadsInProject(project: ProjectDto, padId1: string, padId2: string): ProjectDto {
    const located1 = locatePad(project, padId1)
    const located2 = locatePad(project, padId2)
    if (!located1 || !located2) {
        return project
    }

    const swappedPad1: PadDto = {...located1.pad, position: located2.pad.position}
    const swappedPad2: PadDto = {...located2.pad, position: located1.pad.position}

    return {
        ...project,
        pages: project.pages.map((page) => {
            if (page !== located1.page && page !== located2.page) {
                return page
            }

            const pads = page.pads.filter((pad) => pad.id !== padId1 && pad.id !== padId2)
            if (page === located1.page) {
                pads.push(swappedPad2)
            }
            if (page === located2.page) {
                pads.push(swappedPad1)
            }

            return {...page, pads}
        }),
    }
}
