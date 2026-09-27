import {describe, expect, it} from 'vitest'
import {isPlayingStatus, PlaybackStore} from './playbackStore.js'

describe('isPlayingStatus', () => {
    it('treats PLAYING, PAUSING, PAUSED and STOPPING as playing', () => {
        expect(isPlayingStatus('PLAYING')).toBe(true)
        expect(isPlayingStatus('PAUSING')).toBe(true)
        expect(isPlayingStatus('PAUSED')).toBe(true)
        expect(isPlayingStatus('STOPPING')).toBe(true)
    })

    it('treats STOPPED and unknown statuses as not playing', () => {
        expect(isPlayingStatus('STOPPED')).toBe(false)
        expect(isPlayingStatus('SOMETHING_ELSE')).toBe(false)
    })

    it('treats an unobserved status (undefined) as not playing', () => {
        expect(isPlayingStatus(undefined)).toBe(false)
    })
})

describe('PlaybackStore', () => {
    it('has no status for a pad that has never been observed', () => {
        expect(new PlaybackStore().getPadStatus('pad-1')).toBeUndefined()
    })

    it('remembers a pad\'s status once set', () => {
        const store = new PlaybackStore()

        store.setPadStatus('pad-1', 'PLAYING')

        expect(store.getPadStatus('pad-1')).toBe('PLAYING')
    })

    it('overwrites a pad\'s previous status when set again', () => {
        const store = new PlaybackStore()
        store.setPadStatus('pad-1', 'PLAYING')

        store.setPadStatus('pad-1', 'STOPPED')

        expect(store.getPadStatus('pad-1')).toBe('STOPPED')
    })

    it('bulk-seeds statuses for several pads at once, without touching pads not in the batch', () => {
        const store = new PlaybackStore()
        store.setPadStatus('pad-1', 'PLAYING')

        store.setPadStatuses({'pad-2': 'STOPPED', 'pad-3': 'PAUSED'})

        expect(store.getPadStatus('pad-1')).toBe('PLAYING')
        expect(store.getPadStatus('pad-2')).toBe('STOPPED')
        expect(store.getPadStatus('pad-3')).toBe('PAUSED')
    })

    it('forgets every status once cleared', () => {
        const store = new PlaybackStore()
        store.setPadStatus('pad-1', 'PLAYING')

        store.clear()

        expect(store.getPadStatus('pad-1')).toBeUndefined()
    })
})
