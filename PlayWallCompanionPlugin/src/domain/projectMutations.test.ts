import {describe, expect, it} from 'vitest'
import {replacePadInProject, swapPadsInProject} from './projectMutations.js'
import type {PadDto, PageDto, ProjectDto} from '../connection/protocol.js'

function fakePad(id: string, position: number): PadDto {
    return {id, position, name: id, defaultColor: null, playColor: null, introColor: null}
}

function fakePage(position: number, pads: PadDto[]): PageDto {
    return {id: `page-${position}`, position, settings: null, pads}
}

function fakeProject(pages: PageDto[]): ProjectDto {
    return {
        metadata: {
            id: 'project-1',
            name: 'Test project',
            defaultColor: null,
            playColor: null,
            introColor: null,
            numberOfHorizontalPads: 10,
            numberOfVerticalPads: 10,
            volume: 1,
        },
        pages,
    }
}

describe('replacePadInProject', () => {
    it('replaces the pad with the given id with the new pad, at the same position in the array', () => {
        const project = fakeProject([fakePage(0, [fakePad('pad-1', 0), fakePad('pad-2', 1)])])
        const newPad = fakePad('pad-3', 1)

        const result = replacePadInProject(project, newPad, 'pad-2')

        expect(result.pages[0]?.pads.map((pad) => pad.id)).toEqual(['pad-1', 'pad-3'])
    })

    it('leaves every other page untouched (same array reference)', () => {
        const otherPage = fakePage(1, [fakePad('pad-9', 0)])
        const project = fakeProject([fakePage(0, [fakePad('pad-1', 0)]), otherPage])

        const result = replacePadInProject(project, fakePad('pad-2', 0), 'pad-1')

        expect(result.pages[1]).toBe(otherPage)
    })

    it('returns the project unchanged when the target pad id does not exist', () => {
        const project = fakeProject([fakePage(0, [fakePad('pad-1', 0)])])

        expect(replacePadInProject(project, fakePad('pad-2', 0), 'missing-id')).toBe(project)
    })
})

describe('swapPadsInProject', () => {
    it('swaps the positions of two pads on the same page', () => {
        const project = fakeProject([fakePage(0, [fakePad('pad-1', 0), fakePad('pad-2', 1)])])

        const result = swapPadsInProject(project, 'pad-1', 'pad-2')

        const pads = result.pages[0]?.pads ?? []
        expect(pads.find((pad) => pad.id === 'pad-1')?.position).toBe(1)
        expect(pads.find((pad) => pad.id === 'pad-2')?.position).toBe(0)
    })

    it('swaps both position and page membership for pads on different pages', () => {
        const project = fakeProject([fakePage(0, [fakePad('pad-1', 0)]), fakePage(1, [fakePad('pad-2', 3)])])

        const result = swapPadsInProject(project, 'pad-1', 'pad-2')

        expect(result.pages[0]?.pads.map((pad) => pad.id)).toEqual(['pad-2'])
        expect(result.pages[0]?.pads[0]?.position).toBe(0)
        expect(result.pages[1]?.pads.map((pad) => pad.id)).toEqual(['pad-1'])
        expect(result.pages[1]?.pads[0]?.position).toBe(3)
    })

    it('returns the project unchanged when either pad id does not exist', () => {
        const project = fakeProject([fakePage(0, [fakePad('pad-1', 0)])])

        expect(swapPadsInProject(project, 'pad-1', 'missing-id')).toBe(project)
    })
})
