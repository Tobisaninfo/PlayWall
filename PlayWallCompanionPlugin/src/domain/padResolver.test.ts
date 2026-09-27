import {describe, expect, it} from 'vitest'
import {findPadByName, findPadByPosition} from './padResolver.js'
import type {PadDto, PageDto, ProjectDto} from '../connection/protocol.js'

function fakePad(id: string, position: number, name: string | null = null): PadDto {
    return {id, position, name, defaultColor: null, playColor: null, introColor: null}
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

describe('findPadByName', () => {
    it('finds a pad by its exact name on the first page that has it', () => {
        const project = fakeProject([
            fakePage(0, [fakePad('pad-1', 0, 'Intro')]),
            fakePage(1, [fakePad('pad-2', 0, 'Outro')]),
        ])

        expect(findPadByName(project, 'Outro')?.id).toBe('pad-2')
    })

    it('returns the first match when multiple pads share the same name', () => {
        const project = fakeProject([
            fakePage(0, [fakePad('pad-1', 0, 'Duplicate')]),
            fakePage(1, [fakePad('pad-2', 0, 'Duplicate')]),
        ])

        expect(findPadByName(project, 'Duplicate')?.id).toBe('pad-1')
    })

    it('returns undefined when no pad has that name', () => {
        const project = fakeProject([fakePage(0, [fakePad('pad-1', 0, 'Intro')])])

        expect(findPadByName(project, 'Missing')).toBeUndefined()
    })
})

describe('findPadByPosition', () => {
    it('finds the pad at the given (0-based) page and position', () => {
        const project = fakeProject([
            fakePage(0, [fakePad('pad-1', 0), fakePad('pad-2', 1)]),
            fakePage(1, [fakePad('pad-3', 0)]),
        ])

        expect(findPadByPosition(project, 1, 0)?.id).toBe('pad-3')
    })

    it('returns undefined for a page that does not exist', () => {
        const project = fakeProject([fakePage(0, [fakePad('pad-1', 0)])])

        expect(findPadByPosition(project, 5, 0)).toBeUndefined()
    })

    it('returns undefined for a position not occupied on that page', () => {
        const project = fakeProject([fakePage(0, [fakePad('pad-1', 0)])])

        expect(findPadByPosition(project, 0, 3)).toBeUndefined()
    })
})
