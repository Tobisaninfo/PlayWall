import {describe, expect, it} from 'vitest'
import {computeTargetPage} from './pageSelector.js'

describe('computeTargetPage', () => {
    describe('previous', () => {
        it('moves to the previous page when not already on the first page', () => {
            expect(computeTargetPage('previous', 1, 2, 5)).toBe(1)
        })

        it('does not wrap around past the first page', () => {
            expect(computeTargetPage('previous', 1, 0, 5)).toBeUndefined()
        })
    })

    describe('next', () => {
        it('moves to the next page when not already on the last page', () => {
            expect(computeTargetPage('next', 1, 1, 5)).toBe(2)
        })

        it('does not wrap around past the last page', () => {
            expect(computeTargetPage('next', 1, 4, 5)).toBeUndefined()
        })

        it('does not act on an empty project', () => {
            expect(computeTargetPage('next', 1, 0, 0)).toBeUndefined()
        })
    })

    describe('jump', () => {
        it('jumps to the given 1-based page number', () => {
            expect(computeTargetPage('jump', 3, 0, 5)).toBe(2)
        })

        it('rejects a page number below 1', () => {
            expect(computeTargetPage('jump', 0, 0, 5)).toBeUndefined()
        })

        it('rejects a page number beyond the project size', () => {
            expect(computeTargetPage('jump', 6, 0, 5)).toBeUndefined()
        })

        it('allows "jumping" to the already-active page (a no-op result, not special-cased)', () => {
            expect(computeTargetPage('jump', 1, 0, 5)).toBe(0)
        })
    })
})
