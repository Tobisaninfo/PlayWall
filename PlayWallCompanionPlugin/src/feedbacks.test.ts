import type {CompanionFeedbackAdvancedEvent, CompanionFeedbackCallbackContext} from '@companion-module/base'
import {describe, expect, it} from 'vitest'
import {GetFeedbackDefinitions} from './feedbacks.js'
import {PAGE_CURRENT_COLOR_FEEDBACK_ID} from './ids.js'
import type {PageFeedbackOptions} from './domain/pageSelector.js'
import type {ProjectDto} from './connection/protocol.js'
import type ModuleInstance from './main.js'

const FEEDBACK_CONTEXT = {} as CompanionFeedbackCallbackContext

function stubModuleInstance(options: { project?: ProjectDto; activePage?: number } = {}): ModuleInstance {
    return {
        projectStore: {getProject: () => options.project},
        pageNavigationStore: {getActivePage: () => options.activePage ?? 0},
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

function getPageColorFeedback(self: ModuleInstance) {
    const feedback = GetFeedbackDefinitions(self)[PAGE_CURRENT_COLOR_FEEDBACK_ID]
    if (!feedback) {
        throw new Error('page_current_color feedback definition is missing')
    }
    return feedback
}

function feedbackEvent(
    options: PageFeedbackOptions,
    image?: { width: number; height: number },
): CompanionFeedbackAdvancedEvent<PageFeedbackOptions> {
    return {options, image} as unknown as CompanionFeedbackAdvancedEvent<PageFeedbackOptions>
}

describe('page_current_color feedback', () => {
    it('returns an empty result when no project is loaded', () => {
        const feedback = getPageColorFeedback(stubModuleInstance())

        const result = feedback.callback(feedbackEvent({pageNumber: 1}), FEEDBACK_CONTEXT)

        expect(result).toEqual({})
    })

    it('returns an empty result for a page number beyond the project size', () => {
        const feedback = getPageColorFeedback(stubModuleInstance({project: fakeProjectWithPages(['RED1'])}))

        const result = feedback.callback(feedbackEvent({pageNumber: 5}), FEEDBACK_CONTEXT)

        expect(result).toEqual({})
    })

    it('does not add a border overlay for a page that is not the active one', () => {
        const feedback = getPageColorFeedback(
            stubModuleInstance({project: fakeProjectWithPages(['RED1', 'BLUE1']), activePage: 0}),
        )

        const result = feedback.callback(feedbackEvent({pageNumber: 2}, {width: 72, height: 72}), FEEDBACK_CONTEXT)

        expect(result).not.toHaveProperty('imageBuffer')
    })

    it('adds a border overlay for the active page', () => {
        const feedback = getPageColorFeedback(
            stubModuleInstance({project: fakeProjectWithPages(['RED1', 'BLUE1']), activePage: 1}),
        )

        const result = feedback.callback(feedbackEvent({pageNumber: 2}, {width: 72, height: 72}), FEEDBACK_CONTEXT)

        expect(result).toHaveProperty('imageBuffer')
    })
})
