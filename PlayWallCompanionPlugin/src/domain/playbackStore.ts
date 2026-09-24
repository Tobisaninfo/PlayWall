/**
 * PadControllerStatus values (PlayWallCommon) under which a pad is playing or on its way there —
 * i.e. a toggle action should send Stop, and the feedback should use the play color.
 */
const PLAYING_STATUSES = new Set(['PLAYING', 'PAUSING', 'PAUSED', 'STOPPING'])

export function isPlayingStatus(status: string | undefined): boolean {
    return status !== undefined && PLAYING_STATUSES.has(status)
}

/**
 * Holds each pad's last known playback status — the live, transient runtime state Companion needs
 * for the Play/Stop action and its feedback. Distinct from `ProjectStore` (the project's static
 * content) and `PageNavigationStore` (which page is active).
 */
export class PlaybackStore {
    private readonly padStatuses = new Map<string, string>()

    setPadStatus(padId: string, status: string): void {
        this.padStatuses.set(padId, status)
    }

    /** Bulk-seeds statuses, e.g. from the initial "currently loaded project" fetch on (re)connect. */
    setPadStatuses(statuses: Record<string, string>): void {
        for (const [padId, status] of Object.entries(statuses)) {
            this.padStatuses.set(padId, status)
        }
    }

    /** `undefined` means no status has been observed yet for this pad — treat as not playing. */
    getPadStatus(padId: string): string | undefined {
        return this.padStatuses.get(padId)
    }

    /** Called when no project is loaded at all: nothing is relevant anymore. */
    clear(): void {
        this.padStatuses.clear()
    }
}
