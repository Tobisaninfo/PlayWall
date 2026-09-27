import {describe, expect, it} from 'vitest'
import {ACTIVE_PAGE_BORDER_COLOR, buildActivePageOverlay} from './activePageOverlay.js'

function decode(imageBuffer: string): Buffer {
    return Buffer.from(imageBuffer, 'base64')
}

function pixelAt(buffer: Buffer, width: number, x: number, y: number): [r: number, g: number, b: number, a: number] {
    const offset = (y * width + x) * 4
    return [buffer[offset]!, buffer[offset + 1]!, buffer[offset + 2]!, buffer[offset + 3]!]
}

const [BORDER_R, BORDER_G, BORDER_B] = [
    (ACTIVE_PAGE_BORDER_COLOR >> 16) & 0xff,
    (ACTIVE_PAGE_BORDER_COLOR >> 8) & 0xff,
    ACTIVE_PAGE_BORDER_COLOR & 0xff,
]

describe('buildActivePageOverlay', () => {
    it('produces an RGBA buffer sized for the given dimensions', () => {
        const overlay = buildActivePageOverlay(20, 20)

        expect(overlay.imageBufferEncoding).toEqual({pixelFormat: 'RGBA'})
        expect(decode(overlay.imageBuffer)).toHaveLength(20 * 20 * 4)
    })

    it('paints the border pixels fully opaque in the configured border color', () => {
        const overlay = buildActivePageOverlay(20, 20)
        const buffer = decode(overlay.imageBuffer)

        expect(pixelAt(buffer, 20, 0, 0)).toEqual([BORDER_R, BORDER_G, BORDER_B, 255])
    })

    it('leaves the content area fully transparent, so the page color and text stay visible', () => {
        const overlay = buildActivePageOverlay(20, 20)
        const buffer = decode(overlay.imageBuffer)

        // Dead center of a 20x20 image is far from every edge.
        expect(pixelAt(buffer, 20, 10, 10)).toEqual([0, 0, 0, 0])
    })

    it('transitions from opaque border to transparent content exactly at the border thickness', () => {
        const overlay = buildActivePageOverlay(20, 20)
        const buffer = decode(overlay.imageBuffer)

        // 3px from the left edge is still border; 4px in is already content (thickness is 4px).
        expect(pixelAt(buffer, 20, 3, 10)[3]).toBe(255)
        expect(pixelAt(buffer, 20, 4, 10)[3]).toBe(0)
    })
})
