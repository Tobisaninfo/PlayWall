import type {CompanionInputFieldDropdown, CompanionInputFieldNumber} from '@companion-module/base'

export type PageActionMode = 'previous' | 'next' | 'jump'

export type PageActionOptions = {
    mode: PageActionMode
    pageNumber: number
}

export type PageFeedbackOptions = {
    pageNumber: number
}

export const PAGE_ACTION_OPTIONS: (CompanionInputFieldDropdown<keyof PageActionOptions> | CompanionInputFieldNumber<keyof PageActionOptions>)[] = [
    {
        id: 'mode',
        type: 'dropdown',
        label: 'Page',
        choices: [
            {id: 'previous', label: 'Previous page'},
            {id: 'next', label: 'Next page'},
            {id: 'jump', label: 'Jump to page'},
        ],
        default: 'jump',
        disableAutoExpression: true,
    },
    {
        id: 'pageNumber',
        type: 'number',
        label: 'Page number (1-based)',
        min: 1,
        max: 20,
        default: 1,
        isVisibleExpression: '$(options:mode) == "jump"',
    },
]

/** A page is always addressed directly by its (1-based) position for feedback purposes. */
export const PAGE_FEEDBACK_OPTIONS: CompanionInputFieldNumber<keyof PageFeedbackOptions>[] = [
    {
        id: 'pageNumber',
        type: 'number',
        label: 'Page number (1-based)',
        min: 1,
        max: 20,
        default: 1,
    },
]

/**
 * Mirrors PageActionHandler.java's exact clamping: `previous`/`next` only act while there's a page in
 * that direction (no wrap-around), `jump` only acts if the (1-based) target is a real page. Returns
 * the 0-based target page index, or `undefined` for a no-op — matching the desktop client 1:1.
 */
export function computeTargetPage(mode: PageActionMode, pageNumber: number, currentIndex: number, pageCount: number): number | undefined {
    switch (mode) {
        case 'previous':
            return currentIndex > 0 ? currentIndex - 1 : undefined
        case 'next':
            return currentIndex < pageCount - 1 ? currentIndex + 1 : undefined
        case 'jump': {
            const target = pageNumber - 1
            return target >= 0 && target < pageCount ? target : undefined
        }
    }
}
