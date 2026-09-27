import {describe, expect, it} from 'vitest'
import {resolvePageStyle} from './pageColor.js'
import type {PageDto} from '../connection/protocol.js'

function fakePage(color: string | null | undefined): PageDto {
    return {id: 'page-1', position: 0, settings: {name: 'Page 1', color}, pads: []}
}

describe('resolvePageStyle', () => {
    it('returns the plain color style for a non-active page, even with image dimensions known', () => {
        const style = resolvePageStyle(fakePage('RED1'), false, {width: 72, height: 72})

        expect(style).not.toHaveProperty('imageBuffer')
        expect(style.bgcolor).toBeTypeOf('number')
        expect(style.color).toBeTypeOf('number')
    })

    it('returns the plain color style for the active page when the image size is unknown', () => {
        const style = resolvePageStyle(fakePage('RED1'), true, undefined)

        expect(style).not.toHaveProperty('imageBuffer')
    })

    it('adds a border image overlay for the active page once the image size is known', () => {
        const style = resolvePageStyle(fakePage('RED1'), true, {width: 72, height: 72})

        expect(style).toMatchObject({
            imageBufferEncoding: {pixelFormat: 'RGBA'},
            imageBufferPosition: {x: 0, y: 0, width: 72, height: 72},
        })
        expect(style).toHaveProperty('imageBuffer')
    })

    it('keeps the same bgcolor/color for the active page as for the non-active one', () => {
        const inactive = resolvePageStyle(fakePage('RED1'), false, undefined)
        const active = resolvePageStyle(fakePage('RED1'), true, {width: 10, height: 10})

        expect(active.bgcolor).toBe(inactive.bgcolor)
        expect(active.color).toBe(inactive.color)
    })

    it('falls back to a concrete default style when the page has no color yet', () => {
        const style = resolvePageStyle(fakePage(null), false, undefined)

        expect(style.bgcolor).toBeTypeOf('number')
        expect(style.color).toBeTypeOf('number')
    })
})
