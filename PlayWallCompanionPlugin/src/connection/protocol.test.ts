import {describe, expect, it} from 'vitest'
import {ALL_PADS_STOP_REQUEST_CLASS, buildAllPadsStopRequest} from './protocol.js'

describe('buildAllPadsStopRequest', () => {
    it('uses the AllPadsStopRequest class and carries no extra fields', () => {
        const request = buildAllPadsStopRequest()

        expect(request['@class']).toBe(ALL_PADS_STOP_REQUEST_CLASS)
        expect(Object.keys(request).sort()).toEqual(['@class', 'messageId'])
    })

    it('generates a fresh messageId on every call', () => {
        const first = buildAllPadsStopRequest()
        const second = buildAllPadsStopRequest()

        expect(first.messageId).not.toBe(second.messageId)
    })
})
