/**
 * PadControllerStatus values (PlayWallCommon) under which a pad is playing or on its way there —
 * i.e. a toggle action should send Stop, and the feedback should use the play color.
 */
const PLAYING_STATUSES = new Set(['PLAYING', 'PAUSING', 'PAUSED', 'STOPPING'])

export function isPlayingStatus(status: string | undefined): boolean {
    return status !== undefined && PLAYING_STATUSES.has(status)
}

/**
 * Holds the live, transient PlayWall runtime state Companion needs for the Play/Stop action and its
 * feedback: which page is currently shown, and each pad's last known playback status. Distinct from
 * `ProjectStore`, which holds the project's static content.
 */
export class PlaybackStore {
    private currentPageIndex = 0
    private readonly padStatuses = new Map<string, string>()

    setCurrentPageIndex(index: number): void {
        this.currentPageIndex = index
    }

    getCurrentPageIndex(): number {
        return this.currentPageIndex
    }

    setPadStatus(padId: string, status: string): void {
        this.padStatuses.set(padId, status)
    }

    /** `undefined` means no status has been observed yet for this pad — treat as not playing. */
    getPadStatus(padId: string): string | undefined {
        return this.padStatuses.get(padId)
    }

    /** Called when a (new) project is loaded: page 0, no known pad statuses yet. */
    clear(): void {
        this.currentPageIndex = 0
        this.padStatuses.clear()
    }
}
