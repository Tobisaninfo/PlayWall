import type {CompanionAdvancedFeedbackResult} from '@companion-module/base'
import {buildActivePageOverlay} from './activePageOverlay.js'
import {resolveColorStyle} from './padColor.js'
import type {PageDto} from '../connection/protocol.js'

/**
 * Resolves the feedback style for a page: background/text from the page's own color, unchanged
 * whether active or not. When `isActive` and the target control's pixel dimensions are known, a red
 * border overlay is added on top (see `domain/activePageOverlay.ts`) — it never touches
 * the actual content area, so the page color and text stay exactly as in the non-active case.
 */
export function resolvePageStyle(page: PageDto, isActive: boolean, image: {
    width: number;
    height: number
} | undefined): CompanionAdvancedFeedbackResult {
    const style = resolveColorStyle(page.settings?.color)

    if (!isActive || !image) {
        return style
    }

    const overlay = buildActivePageOverlay(image.width, image.height)
    return {
        ...style,
        imageBuffer: overlay.imageBuffer,
        imageBufferEncoding: overlay.imageBufferEncoding,
        imageBufferPosition: {x: 0, y: 0, width: image.width, height: image.height},
    }
}
