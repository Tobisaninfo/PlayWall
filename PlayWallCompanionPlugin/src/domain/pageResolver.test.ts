import {describe, expect, it} from 'vitest'
import {resolvePage} from './pageResolver.js'
import type {ProjectDto} from '../connection/protocol.js'

function fakeProject(pagePositions: number[]): ProjectDto {
    return {
        metadata: {
            id: 'project-1',
            name: 'Test project',
            defaultColor: null,
            playColor: null,
            introColor: null,
            numberOfHorizontalPads: 1,
            numberOfVerticalPads: 1,
            volume: 1,
        },
        pages: pagePositions.map((position) => ({id: `page-${position}`, position, settings: null, pads: []})),
    }
}

describe('resolvePage', () => {
    it('resolves a 1-based page number to the page at that 0-based position', () => {
        const project = fakeProject([0, 1, 2])

        expect(resolvePage(project, 2)?.id).toBe('page-1')
    })

    it('returns undefined for a page number beyond the project size', () => {
        const project = fakeProject([0, 1, 2])

        expect(resolvePage(project, 4)).toBeUndefined()
    })

    it('returns undefined for page number 0 (there is no position -1)', () => {
        const project = fakeProject([0, 1, 2])

        expect(resolvePage(project, 0)).toBeUndefined()
    })
})
