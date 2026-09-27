import type {CompanionSimplePresetDefinition, CompanionSomePresetDefinition} from '@companion-module/base'
import {describe, expect, it} from 'vitest'
import {GetPresetDefinitions, GetPresetSections} from './presets.js'
import {PAGE_CURRENT_COLOR_FEEDBACK_ID, PAGE_NAVIGATE_ACTION_ID} from './ids.js'
import type {ModuleSchema} from './main.js'

/** Every preset this module defines is a plain 'simple' one; narrow away the (unused) union members. */
function expectSimplePreset(
    preset: CompanionSomePresetDefinition<ModuleSchema> | undefined,
    name: string,
): CompanionSimplePresetDefinition<ModuleSchema> {
    if (!preset || preset.type !== 'simple') {
        throw new Error(`${name} should be a simple preset`)
    }
    return preset
}

describe('page presets', () => {
    it('defines a jump preset for every position 1-20, each jumping to its own position', () => {
        const presets = GetPresetDefinitions()

        for (let position = 1; position <= 20; position++) {
            const preset = expectSimplePreset(presets[`page_${position}`], `page_${position}`)
            expect(preset.steps[0]?.down[0]).toMatchObject({
                actionId: PAGE_NAVIGATE_ACTION_ID,
                options: {mode: 'jump', pageNumber: position},
            })
            expect(preset.feedbacks[0]?.feedbackId).toBe(PAGE_CURRENT_COLOR_FEEDBACK_ID)
        }

        expect(presets['page_21']).toBeUndefined()
    })

    it('defines previous/next presets with no feedback', () => {
        const presets = GetPresetDefinitions()

        const previous = expectSimplePreset(presets['page_previous'], 'page_previous')
        expect(previous.steps[0]?.down[0]).toMatchObject({
            actionId: PAGE_NAVIGATE_ACTION_ID,
            options: {mode: 'previous'}
        })
        expect(previous.feedbacks).toEqual([])

        const next = expectSimplePreset(presets['page_next'], 'page_next')
        expect(next.steps[0]?.down[0]).toMatchObject({actionId: PAGE_NAVIGATE_ACTION_ID, options: {mode: 'next'}})
        expect(next.feedbacks).toEqual([])
    })

    it('lists exactly the 22 page presets, in order, under the "pages" section', () => {
        const sections = GetPresetSections()
        const pagesSection = sections.find((section) => section.id === 'pages')

        expect(pagesSection?.definitions).toEqual([
            ...Array.from({length: 20}, (_, index) => `page_${index + 1}`),
            'page_previous',
            'page_next',
        ])
    })
})
