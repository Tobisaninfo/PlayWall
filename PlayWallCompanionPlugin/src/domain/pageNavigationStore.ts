/**
 * Holds Companion's own notion of "the active page" (0-based, matching `PageDto.position`). This is
 * the single source of truth both the page-navigation action/feedback/variables *and* the pad
 * action/feedback/variables ("index" mode, see `domain/padSelector.ts`) resolve against — the two are
 * deliberately kept linked to each other, per the module's Page navigation mode setting:
 * - "sync": `main.ts`'s `handlePageShown` mirrors every `ProjectPageShownUpdate` broadcast from the
 *   server into this store, and the page-navigate action also sends its own changes to the server.
 * - "async": this store only ever changes via the page-navigate action; PlayWall's own shown page
 *   (broadcast the same way) is ignored, so both pad and page actions in Companion stay fully
 *   independent of whatever PlayWall itself shows.
 */
export class PageNavigationStore {
    private activePageIndex = 0

    getActivePage(): number {
        return this.activePageIndex
    }

    setActivePage(index: number): void {
        this.activePageIndex = index
    }

    /** Called when a (new) project is loaded, or none is loaded at all: back to the first page. */
    resetPage(): void {
        this.activePageIndex = 0
    }
}
