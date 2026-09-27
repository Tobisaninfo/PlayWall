import {describe, expect, it} from 'vitest'
import {resolvePad} from './padSelector.js'
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

describe('resolvePad', () => {
    it('returns undefined when no project is known yet', () => {
        expect(resolvePad({mode: 'name', padName: 'Intro', padPosition: 1}, undefined, 0)).toBeUndefined()
    })

    it('"name" mode finds the pad by name, regardless of the active page', () => {
        const project = fakeProject([fakePage(0, []), fakePage(1, [fakePad('pad-1', 0, 'Intro')])])

        const pad = resolvePad({mode: 'name', padName: 'Intro', padPosition: 1}, project, 0)

        expect(pad?.id).toBe('pad-1')
    })

    it('"index" mode resolves against the given active page and the 1-based padPosition option', () => {
        const project = fakeProject([
            fakePage(0, [fakePad('pad-1', 0)]),
            fakePage(1, [fakePad('pad-2', 2)]),
        ])

        const pad = resolvePad({mode: 'index', padName: '', padPosition: 3}, project, 1)

        expect(pad?.id).toBe('pad-2')
    })

    it('"index" mode ignores pads on a page other than the active one', () => {
        const project = fakeProject([fakePage(0, [fakePad('pad-1', 0)])])

        const pad = resolvePad({mode: 'index', padName: '', padPosition: 1}, project, 1)

        expect(pad).toBeUndefined()
    })
})
