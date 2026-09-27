import {describe, expect, it} from 'vitest'
import {PageNavigationStore} from './pageNavigationStore.js'

describe('PageNavigationStore', () => {
    it('starts on the first page (0)', () => {
        expect(new PageNavigationStore().getActivePage()).toBe(0)
    })

    it('remembers whatever page was set', () => {
        const store = new PageNavigationStore()

        store.setActivePage(4)

        expect(store.getActivePage()).toBe(4)
    })

    it('resets back to the first page', () => {
        const store = new PageNavigationStore()
        store.setActivePage(4)

        store.resetPage()

        expect(store.getActivePage()).toBe(0)
    })
})
