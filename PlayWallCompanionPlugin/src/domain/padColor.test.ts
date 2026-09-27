import {describe, expect, it} from 'vitest'
import {resolveColorStyle, resolvePadStyle} from './padColor.js'
import type {PadDto, ProjectMetadata} from '../connection/protocol.js'

function fakePad(overrides: Partial<PadDto> = {}): PadDto {
    return {id: 'pad-1', position: 0, name: 'Pad', defaultColor: null, playColor: null, introColor: null, ...overrides}
}

function fakeMetadata(overrides: Partial<ProjectMetadata> = {}): ProjectMetadata {
    return {
        id: 'project-1',
        name: 'Test project',
        defaultColor: null,
        playColor: null,
        introColor: null,
        numberOfHorizontalPads: 10,
        numberOfVerticalPads: 10,
        volume: 1,
        ...overrides,
    }
}

describe('resolveColorStyle', () => {
    it('resolves a known color name to a concrete bgcolor/color pair', () => {
        const style = resolveColorStyle('RED1')

        expect(style.bgcolor).toBeTypeOf('number')
        expect(style.color).toBeTypeOf('number')
    })

    it('resolves the same color name to the same style every time', () => {
        expect(resolveColorStyle('RED1')).toEqual(resolveColorStyle('RED1'))
    })

    it('two different color names resolve to different backgrounds', () => {
        expect(resolveColorStyle('RED1').bgcolor).not.toBe(resolveColorStyle('BLUE1').bgcolor)
    })

    it('falls back to the same style as GRAY1 for null/undefined (PlayWall never sets a color at creation)', () => {
        const gray1 = resolveColorStyle('GRAY1')

        expect(resolveColorStyle(null)).toEqual(gray1)
        expect(resolveColorStyle(undefined)).toEqual(gray1)
    })

    it('falls back to GRAY1 for an unrecognized color name', () => {
        expect(resolveColorStyle('NotARealColor')).toEqual(resolveColorStyle('GRAY1'))
    })
})

describe('resolvePadStyle', () => {
    it('uses the pad\'s own play color while playing', () => {
        const pad = fakePad({playColor: 'RED1', defaultColor: 'BLUE1'})

        expect(resolvePadStyle(pad, fakeMetadata(), true)).toEqual(resolveColorStyle('RED1'))
    })

    it('uses the pad\'s own default color while not playing', () => {
        const pad = fakePad({playColor: 'RED1', defaultColor: 'BLUE1'})

        expect(resolvePadStyle(pad, fakeMetadata(), false)).toEqual(resolveColorStyle('BLUE1'))
    })

    it('falls back to the project\'s play color when the pad has none of its own', () => {
        const pad = fakePad({playColor: null})

        expect(resolvePadStyle(pad, fakeMetadata({playColor: 'RED2'}), true)).toEqual(resolveColorStyle('RED2'))
    })

    it('falls back to the project\'s default color when the pad has none of its own', () => {
        const pad = fakePad({defaultColor: null})

        expect(resolvePadStyle(pad, fakeMetadata({defaultColor: 'BLUE2'}), false)).toEqual(resolveColorStyle('BLUE2'))
    })
})
