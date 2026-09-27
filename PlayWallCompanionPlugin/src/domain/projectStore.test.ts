import {describe, expect, it} from 'vitest'
import {ProjectStore} from './projectStore.js'
import type {ProjectDto} from '../connection/protocol.js'

function fakeProject(name: string): ProjectDto {
    return {
        metadata: {
            id: 'project-1',
            name,
            defaultColor: null,
            playColor: null,
            introColor: null,
            numberOfHorizontalPads: 1,
            numberOfVerticalPads: 1,
            volume: 1,
        },
        pages: [],
    }
}

describe('ProjectStore', () => {
    it('has no project initially', () => {
        expect(new ProjectStore().getProject()).toBeUndefined()
    })

    it('remembers whatever project was set', () => {
        const store = new ProjectStore()

        store.setProject(fakeProject('A'))

        expect(store.getProject()?.metadata.name).toBe('A')
    })

    it('overwrites a previously stored project when a new one is set', () => {
        const store = new ProjectStore()
        store.setProject(fakeProject('A'))

        store.setProject(fakeProject('B'))

        expect(store.getProject()?.metadata.name).toBe('B')
    })

    it('clears the stored project', () => {
        const store = new ProjectStore()
        store.setProject(fakeProject('A'))

        store.clear()

        expect(store.getProject()).toBeUndefined()
    })
})
