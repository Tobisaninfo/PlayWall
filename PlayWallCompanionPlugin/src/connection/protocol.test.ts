import {describe, expect, it} from 'vitest'
import {
    ALL_PADS_STOP_REQUEST_CLASS,
    buildAllPadsStopRequest,
    buildGlobalChangeVolumeRequest,
    GLOBAL_CHANGE_VOLUME_REQUEST_CLASS,
    PROJECT_SETTINGS_UPDATE,
} from './protocol.js'

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

describe('buildGlobalChangeVolumeRequest', () => {
    it('uses the GlobalChangeVolumeRequest class and carries the given volume', () => {
        const request = buildGlobalChangeVolumeRequest(0.75)

        expect(request['@class']).toBe(GLOBAL_CHANGE_VOLUME_REQUEST_CLASS)
        expect(request.volume).toBe(0.75)
        expect(Object.keys(request).sort()).toEqual(['@class', 'messageId', 'volume'])
    })

    it('generates a fresh messageId on every call', () => {
        const first = buildGlobalChangeVolumeRequest(0.5)
        const second = buildGlobalChangeVolumeRequest(0.5)

        expect(first.messageId).not.toBe(second.messageId)
    })
})

describe('PROJECT_SETTINGS_UPDATE schema', () => {
    const baseMetadata = {
        id: 'project-1',
        name: 'Test project',
        numberOfHorizontalPads: 5,
        numberOfVerticalPads: 4,
    }

    it('parses a broadcast carrying a numeric volume', () => {
        const result = PROJECT_SETTINGS_UPDATE.safeParse({
            '@class': 'de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate',
            messageId: 'm',
            projectMetadata: {...baseMetadata, volume: 0.42},
        })

        expect(result.success).toBe(true)
        expect(result.success && result.data.projectMetadata.volume).toBe(0.42)
    })

    it('accepts a missing volume field, mirroring ProjectMetadataDto.volume being nullable on the wire', () => {
        const result = PROJECT_SETTINGS_UPDATE.safeParse({
            '@class': 'de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate',
            messageId: 'm',
            projectMetadata: baseMetadata,
        })

        expect(result.success).toBe(true)
        expect(result.success && result.data.projectMetadata.volume).toBeUndefined()
    })

    it('rejects a broadcast with no projectMetadata at all', () => {
        const result = PROJECT_SETTINGS_UPDATE.safeParse({
            '@class': 'de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate',
            messageId: 'm',
        })

        expect(result.success).toBe(false)
    })
})
