import {describe, expect, it} from 'vitest'
import {
    ALL_PADS_STOP_REQUEST_CLASS,
    buildAllPadsStopRequest,
    buildGlobalChangeVolumeRequest,
    buildPadPlayRequest,
    buildPadStopRequest,
    buildProjectGetRequest,
    buildProjectPageShowRequest,
    GLOBAL_CHANGE_VOLUME_REQUEST_CLASS,
    PAD_PLAY_REQUEST_CLASS,
    PAD_REPLACE_UPDATE,
    PAD_STATUS_UPDATE,
    PAD_STOP_REQUEST_CLASS,
    PAD_SWAP_UPDATE,
    PAD_UPDATE,
    PROJECT_GET_REQUEST_CLASS,
    PROJECT_GET_RESPONSE,
    PROJECT_LOADED_UPDATE,
    PROJECT_PAGE_SHOW_REQUEST_CLASS,
    PROJECT_PAGE_SHOWN_UPDATE,
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

describe('buildPadPlayRequest', () => {
    it('uses the PadPlayRequest class and carries the given padId', () => {
        const request = buildPadPlayRequest('pad-1')

        expect(request['@class']).toBe(PAD_PLAY_REQUEST_CLASS)
        expect(request.padId).toBe('pad-1')
        expect(Object.keys(request).sort()).toEqual(['@class', 'messageId', 'padId'])
    })

    it('generates a fresh messageId on every call', () => {
        const first = buildPadPlayRequest('pad-1')
        const second = buildPadPlayRequest('pad-1')

        expect(first.messageId).not.toBe(second.messageId)
    })
})

describe('buildPadStopRequest', () => {
    it('uses the PadStopRequest class, carries the given padId, and requests a graceful (non-immediate) stop', () => {
        const request = buildPadStopRequest('pad-1')

        expect(request['@class']).toBe(PAD_STOP_REQUEST_CLASS)
        expect(request.padId).toBe('pad-1')
        expect(request.isImmediately).toBe(false)
    })

    it('generates a fresh messageId on every call', () => {
        const first = buildPadStopRequest('pad-1')
        const second = buildPadStopRequest('pad-1')

        expect(first.messageId).not.toBe(second.messageId)
    })
})

describe('PAD_STATUS_UPDATE schema', () => {
    it('parses a broadcast with a padId and a status', () => {
        const result = PAD_STATUS_UPDATE.safeParse({'@class': 'x', messageId: 'm', padId: 'pad-1', status: 'PLAYING'})

        expect(result.success).toBe(true)
        expect(result.success && result.data).toMatchObject({padId: 'pad-1', status: 'PLAYING'})
    })

    it('rejects a broadcast with no status', () => {
        const result = PAD_STATUS_UPDATE.safeParse({'@class': 'x', messageId: 'm', padId: 'pad-1'})

        expect(result.success).toBe(false)
    })
})

describe('PAD_REPLACE_UPDATE schema', () => {
    it('parses a broadcast carrying the replacing pad and the id of the pad it overwrote', () => {
        const sourcePad = {
            id: 'pad-2',
            position: 1,
            name: 'New pad',
            defaultColor: null,
            playColor: null,
            introColor: null
        }

        const result = PAD_REPLACE_UPDATE.safeParse({'@class': 'x', messageId: 'm', sourcePad, targetPadId: 'pad-1'})

        expect(result.success).toBe(true)
        expect(result.success && result.data.targetPadId).toBe('pad-1')
        expect(result.success && result.data.sourcePad.id).toBe('pad-2')
    })
})

describe('PAD_SWAP_UPDATE schema', () => {
    it('parses a broadcast carrying the two swapped pad ids', () => {
        const result = PAD_SWAP_UPDATE.safeParse({'@class': 'x', messageId: 'm', pad1: 'pad-1', pad2: 'pad-2'})

        expect(result.success).toBe(true)
        expect(result.success && result.data).toMatchObject({pad1: 'pad-1', pad2: 'pad-2'})
    })
})

describe('PAD_UPDATE schema', () => {
    it('parses a broadcast carrying the pad\'s freshly edited settings, e.g. changed colors', () => {
        const pad = {
            id: 'pad-1',
            position: 0,
            name: 'Renamed',
            defaultColor: 'RED1',
            playColor: 'BLUE1',
            introColor: null
        }

        const result = PAD_UPDATE.safeParse({'@class': 'x', messageId: 'm', pad})

        expect(result.success).toBe(true)
        expect(result.success && result.data.pad).toMatchObject({id: 'pad-1', defaultColor: 'RED1', playColor: 'BLUE1'})
    })

    it('rejects a broadcast with no pad', () => {
        const result = PAD_UPDATE.safeParse({'@class': 'x', messageId: 'm'})

        expect(result.success).toBe(false)
    })
})

describe('buildProjectGetRequest', () => {
    it('uses the ProjectGetRequest class with a null projectId (the "currently loaded project" query)', () => {
        const request = buildProjectGetRequest()

        expect(request['@class']).toBe(PROJECT_GET_REQUEST_CLASS)
        expect(request.projectId).toBeNull()
        expect(Object.keys(request).sort()).toEqual(['@class', 'messageId', 'projectId'])
    })

    it('generates a fresh messageId on every call', () => {
        const first = buildProjectGetRequest()
        const second = buildProjectGetRequest()

        expect(first.messageId).not.toBe(second.messageId)
    })
})

describe('PROJECT_GET_RESPONSE schema', () => {
    const project = {
        metadata: {id: 'project-1', name: 'Test project', numberOfHorizontalPads: 5, numberOfVerticalPads: 4},
        pages: [],
    }

    it('parses a response for the currently loaded project, including currentPageIndex/padStatuses', () => {
        const result = PROJECT_GET_RESPONSE.safeParse({
            '@class': 'x',
            messageId: 'm',
            project,
            currentPageIndex: 2,
            padStatuses: {'pad-1': 'PLAYING'},
        })

        expect(result.success).toBe(true)
        expect(result.success && result.data.currentPageIndex).toBe(2)
        expect(result.success && result.data.padStatuses).toEqual({'pad-1': 'PLAYING'})
    })

    it('accepts a response for a project fetched by id, with no currentPageIndex/padStatuses', () => {
        const result = PROJECT_GET_RESPONSE.safeParse({'@class': 'x', messageId: 'm', project})

        expect(result.success).toBe(true)
        expect(result.success && result.data.currentPageIndex).toBeUndefined()
        expect(result.success && result.data.padStatuses).toBeUndefined()
    })
})

describe('PROJECT_LOADED_UPDATE schema', () => {
    it('parses a broadcast carrying the newly opened project', () => {
        const project = {
            metadata: {id: 'project-1', name: 'Test project', numberOfHorizontalPads: 5, numberOfVerticalPads: 4},
            pages: [],
        }

        const result = PROJECT_LOADED_UPDATE.safeParse({'@class': 'x', messageId: 'm', project})

        expect(result.success).toBe(true)
        expect(result.success && result.data.project.metadata.name).toBe('Test project')
    })

    it('rejects a broadcast with no project', () => {
        const result = PROJECT_LOADED_UPDATE.safeParse({'@class': 'x', messageId: 'm'})

        expect(result.success).toBe(false)
    })
})

describe('buildProjectPageShowRequest', () => {
    it('uses the ProjectPageShowRequest class and carries the given (0-based) index', () => {
        const request = buildProjectPageShowRequest(3)

        expect(request['@class']).toBe(PROJECT_PAGE_SHOW_REQUEST_CLASS)
        expect(request.index).toBe(3)
        expect(Object.keys(request).sort()).toEqual(['@class', 'index', 'messageId'])
    })

    it('generates a fresh messageId on every call', () => {
        const first = buildProjectPageShowRequest(0)
        const second = buildProjectPageShowRequest(0)

        expect(first.messageId).not.toBe(second.messageId)
    })
})

describe('PROJECT_PAGE_SHOWN_UPDATE schema', () => {
    it('parses a broadcast with a numeric index', () => {
        const result = PROJECT_PAGE_SHOWN_UPDATE.safeParse({'@class': 'x', messageId: 'm', index: 2})

        expect(result.success).toBe(true)
        expect(result.success && result.data.index).toBe(2)
    })

    it('rejects a broadcast with no index', () => {
        const result = PROJECT_PAGE_SHOWN_UPDATE.safeParse({'@class': 'x', messageId: 'm'})

        expect(result.success).toBe(false)
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
