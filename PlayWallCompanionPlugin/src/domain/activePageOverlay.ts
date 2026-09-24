/**
 * Companion's simple advanced-feedback style (`bgcolor`/`color`) has no border property — a red border
 * requires drawing pixels directly, via the advanced feedback's `imageBuffer` support. This draws a
 * single RGBA overlay on top of the plain `bgcolor` fill (the page's own color, unchanged): opaque red
 * within `BORDER_THICKNESS_PX` of any edge, fully transparent everywhere else. The content area (where
 * the button's text renders) is never touched: Companion draws this image buffer above the text, so
 * any opaque pixel there would hide it.
 */

const BORDER_THICKNESS_PX = 4

export const ACTIVE_PAGE_BORDER_COLOR = 0xff0000

export interface ImageOverlay {
    imageBuffer: string
    imageBufferEncoding: { pixelFormat: 'RGBA' }
}

function unpackRgb(combined: number): [r: number, g: number, b: number] {
    return [(combined >> 16) & 0xff, (combined >> 8) & 0xff, combined & 0xff]
}

/**
 * Builds the active-page overlay: draw this on top of the page's own plain `bgcolor` fill (unchanged
 * from the non-active style).
 */
export function buildActivePageOverlay(width: number, height: number): ImageOverlay {
    const [borderR, borderG, borderB] = unpackRgb(ACTIVE_PAGE_BORDER_COLOR)
    const buffer = Buffer.alloc(width * height * 4)

    for (let y = 0; y < height; y++) {
        for (let x = 0; x < width; x++) {
            const distanceToEdge = Math.min(x, y, width - 1 - x, height - 1 - y)
            if (distanceToEdge >= BORDER_THICKNESS_PX) {
                continue // left fully transparent — bgcolor (the page's own color) and the text stay visible
            }

            const offset = (y * width + x) * 4
            buffer[offset] = borderR
            buffer[offset + 1] = borderG
            buffer[offset + 2] = borderB
            buffer[offset + 3] = 255
        }
    }

    return {
        imageBuffer: buffer.toString('base64'),
        imageBufferEncoding: {pixelFormat: 'RGBA'},
    }
}
