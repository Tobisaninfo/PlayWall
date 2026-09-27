import type {CompanionFeedbackAdvancedEvent, CompanionFeedbackCallbackContext} from '@companion-module/base'
import {describe, expect, it} from 'vitest'
import {GetFeedbackDefinitions} from './feedbacks.js'
import {resolveColorStyle} from './domain/padColor.js'
import {PAD_CURRENT_COLOR_FEEDBACK_ID, PAGE_CURRENT_COLOR_FEEDBACK_ID} from './ids.js'
import type {PadSelectorOptions} from './domain/padSelector.js'
import type {PageFeedbackOptions} from './domain/pageSelector.js'
import type {PadDto, PageDto, ProjectDto} from './connection/protocol.js'
import type ModuleInstance from './main.js'

const FEEDBACK_CONTEXT = {} as CompanionFeedbackCallbackContext

function stubModuleInstance(
    options: { project?: ProjectDto; activePage?: number; padStatus?: string } = {},
): ModuleInstance {
    return {
        projectStore: {getProject: () => options.project},
        pageNavigationStore: {getActivePage: () => options.activePage ?? 0},
        playbackStore: {getPadStatus: () => options.padStatus},
    } as unknown as ModuleInstance
}

function fakeProjectWithPages(pageColors: (string | null)[]): ProjectDto {
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
        pages: pageColors.map((color, index) => ({
            id: `page-${index}`,
            position: index,
            settings: {name: `Page ${index + 1}`, color},
            pads: [],
        })),
    }
}

function fakePad(overrides: Partial<PadDto> = {}): PadDto {
    return {id: 'pad-1', position: 0, name: 'Pad', defaultColor: null, playColor: null, introColor: null, ...overrides}
}

/** A single-page project containing just the given pad, at position 0. */
function fakeProjectWithPad(pad: PadDto): ProjectDto {
    const page: PageDto = {id: 'page-0', position: 0, settings: null, pads: [pad]}
    return {...fakeProjectWithPages([]), pages: [page]}
}

function getPageColorFeedback(self: ModuleInstance) {
    const feedback = GetFeedbackDefinitions(self)[PAGE_CURRENT_COLOR_FEEDBACK_ID]
    if (!feedback) {
        throw new Error('page_current_color feedback definition is missing')
    }
    return feedback
}

function getPadColorFeedback(self: ModuleInstance) {
    const feedback = GetFeedbackDefinitions(self)[PAD_CURRENT_COLOR_FEEDBACK_ID]
    if (!feedback) {
        throw new Error('pad_current_color feedback definition is missing')
    }
    return feedback
}

function pageFeedbackEvent(
    options: PageFeedbackOptions,
    image?: { width: number; height: number },
): CompanionFeedbackAdvancedEvent<PageFeedbackOptions> {
    return {options, image} as unknown as CompanionFeedbackAdvancedEvent<PageFeedbackOptions>
}

function padFeedbackEvent(options: PadSelectorOptions): CompanionFeedbackAdvancedEvent<PadSelectorOptions> {
    return {options} as unknown as CompanionFeedbackAdvancedEvent<PadSelectorOptions>
}

const PAD_BY_NAME_OPTIONS: PadSelectorOptions = {mode: 'name', padName: 'Pad', padPosition: 1}

describe('page_current_color feedback', () => {
    it('returns an empty result when no project is loaded', () => {
        const feedback = getPageColorFeedback(stubModuleInstance())

        const result = feedback.callback(pageFeedbackEvent({pageNumber: 1}), FEEDBACK_CONTEXT)

        expect(result).toEqual({})
    })

    it('returns an empty result for a page number beyond the project size', () => {
        const feedback = getPageColorFeedback(stubModuleInstance({project: fakeProjectWithPages(['RED1'])}))

        const result = feedback.callback(pageFeedbackEvent({pageNumber: 5}), FEEDBACK_CONTEXT)

        expect(result).toEqual({})
    })

    it('does not add a border overlay for a page that is not the active one', () => {
        const feedback = getPageColorFeedback(
            stubModuleInstance({project: fakeProjectWithPages(['RED1', 'BLUE1']), activePage: 0}),
        )

        const result = feedback.callback(pageFeedbackEvent({pageNumber: 2}, {width: 72, height: 72}), FEEDBACK_CONTEXT)

        expect(result).not.toHaveProperty('imageBuffer')
    })

    it('adds a border overlay for the active page', () => {
        const feedback = getPageColorFeedback(
            stubModuleInstance({project: fakeProjectWithPages(['RED1', 'BLUE1']), activePage: 1}),
        )

        const result = feedback.callback(pageFeedbackEvent({pageNumber: 2}, {width: 72, height: 72}), FEEDBACK_CONTEXT)

        expect(result).toHaveProperty('imageBuffer')
    })
})

describe('pad_current_color feedback', () => {
    it('returns an empty result when no project is loaded', () => {
        const feedback = getPadColorFeedback(stubModuleInstance())

        const result = feedback.callback(padFeedbackEvent(PAD_BY_NAME_OPTIONS), FEEDBACK_CONTEXT)

        expect(result).toEqual({})
    })

    it('returns an empty result when the pad cannot be resolved', () => {
        const feedback = getPadColorFeedback(stubModuleInstance({project: fakeProjectWithPages([])}))

        const result = feedback.callback(padFeedbackEvent(PAD_BY_NAME_OPTIONS), FEEDBACK_CONTEXT)

        expect(result).toEqual({})
    })

    it('uses the play color while the pad is playing', () => {
        const pad = fakePad({playColor: 'RED1', defaultColor: 'BLUE1'})
        const feedback = getPadColorFeedback(
            stubModuleInstance({project: fakeProjectWithPad(pad), padStatus: 'PLAYING'}),
        )

        const result = feedback.callback(padFeedbackEvent(PAD_BY_NAME_OPTIONS), FEEDBACK_CONTEXT)

        expect(result).toEqual(resolveColorStyle('RED1'))
    })

    it('uses the default color while the pad is not playing', () => {
        const pad = fakePad({playColor: 'RED1', defaultColor: 'BLUE1'})
        const feedback = getPadColorFeedback(
            stubModuleInstance({project: fakeProjectWithPad(pad), padStatus: 'STOPPED'}),
        )

        const result = feedback.callback(padFeedbackEvent(PAD_BY_NAME_OPTIONS), FEEDBACK_CONTEXT)

        expect(result).toEqual(resolveColorStyle('BLUE1'))
    })

    it('treats a pad with no observed status yet as not playing', () => {
        const pad = fakePad({playColor: 'RED1', defaultColor: 'BLUE1'})
        const feedback = getPadColorFeedback(
            stubModuleInstance({project: fakeProjectWithPad(pad), padStatus: undefined}),
        )

        const result = feedback.callback(padFeedbackEvent(PAD_BY_NAME_OPTIONS), FEEDBACK_CONTEXT)

        expect(result).toEqual(resolveColorStyle('BLUE1'))
    })
})
