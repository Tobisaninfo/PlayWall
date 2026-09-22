import type {ProjectDto} from '../connection/protocol.js'

/**
 * Holds the PlayWall project currently known to be open on the server. Re-derived on every
 * (re)connect, so it is kept in memory only — no need to persist it across process restarts.
 */
export class ProjectStore {
    private project: ProjectDto | undefined

    setProject(project: ProjectDto): void {
        this.project = project
    }

    clear(): void {
        this.project = undefined
    }

    getProject(): ProjectDto | undefined {
        return this.project
    }
}
