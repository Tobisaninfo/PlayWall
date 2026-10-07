import {beforeEach, describe, expect, it, vi} from 'vitest'
import {
    ALL_PADS_STOP_REQUEST_CLASS,
    ERROR_MESSAGE_CLASS,
    GLOBAL_CHANGE_VOLUME_REQUEST_CLASS,
    PAD_PLAY_REQUEST_CLASS,
    PAD_REPLACE_UPDATE_CLASS,
    PAD_STATUS_UPDATE_CLASS,
    PAD_STOP_REQUEST_CLASS,
    PAD_SWAP_UPDATE_CLASS,
    PAD_UPDATE_CLASS,
    PROJECT_GET_CURRENT_REQUEST_CLASS,
    PROJECT_GET_CURRENT_RESPONSE_CLASS,
    PROJECT_LOADED_UPDATE_CLASS,
    PROJECT_NOT_LOADED_ERROR_CLASS,
    PROJECT_PAGE_SHOW_REQUEST_CLASS,
    PROJECT_PAGE_SHOWN_UPDATE_CLASS,
    PROJECT_SETTINGS_UPDATE_CLASS
} from './protocol.js'

/**
 * A minimal stand-in for `ws`'s `WebSocket`, covering only what `ClientWebSocketHandler` actually
 * uses (`readyState`, `on`, `send`, `close`, `removeAllListeners`). `vi.hoisted` is needed because
 * `vi.mock` factories run before the rest of this file, so the class can't just be a normal
 * module-level declaration referenced from the factory.
 */
const {FakeWebSocket} = vi.hoisted(() => {
    class FakeWebSocket {
        static readonly CONNECTING = 0
        static readonly OPEN = 1
        static readonly CLOSING = 2
        static readonly CLOSED = 3
        static instances: FakeWebSocket[] = []

        readyState: number = FakeWebSocket.CONNECTING
        readonly sent: string[] = []
        private readonly listeners = new Map<string, ((...args: unknown[]) => void)[]>()

        constructor() {
            FakeWebSocket.instances.push(this)
        }

        on(event: string, listener: (...args: unknown[]) => void): this {
            const list = this.listeners.get(event) ?? []
            list.push(listener)
            this.listeners.set(event, list)
            return this
        }

        send(data: string): void {
            this.sent.push(data)
        }

        /** Simulates the server pushing a message down the socket (e.g. an unsolicited broadcast). */
        emit(event: string, ...args: unknown[]): void {
            for (const listener of this.listeners.get(event) ?? []) {
                listener(...args)
            }
        }

        close(): void {
        }

        removeAllListeners(): void {
        }
    }

    return {FakeWebSocket}
})

vi.mock('ws', () => ({WebSocket: FakeWebSocket}))

const {ClientWebSocketHandler} = await import('./ClientWebSocketHandler.js')
type ClientWebSocketHandlerOptionsType = ConstructorParameters<typeof ClientWebSocketHandler>[0]

function createHandler(overrides: Partial<ClientWebSocketHandlerOptionsType> = {}) {
    const options: ClientWebSocketHandlerOptionsType = {
        host: 'localhost',
        port: 10023,
        useTls: false,
        reconnectDelaySeconds: 1,
        onStatusChange: vi.fn(),
        onProjectLoaded: vi.fn(),
        onProjectCleared: vi.fn(),
        onPageShown: vi.fn(),
        onProjectSettingsChanged: vi.fn(),
        onPadStatus: vi.fn(),
        onPadStatusById: vi.fn(),
        onPadReplaced: vi.fn(),
        onPadsSwapped: vi.fn(),
        onPadUpdated: vi.fn(),
        onPagesChanged: vi.fn(),
        log: vi.fn(),
        ...overrides,
    }
    return new ClientWebSocketHandler(options)
}

beforeEach(() => {
    FakeWebSocket.instances.length = 0
})

describe('ClientWebSocketHandler.stopAllPads', () => {
    it('sends an AllPadsStopRequest with no extra fields once the socket is open', () => {
        const handler = createHandler()
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()
        socket!.readyState = FakeWebSocket.OPEN

        handler.stopAllPads()

        expect(socket!.sent).toHaveLength(1)
        const message = JSON.parse(socket!.sent[0]) as Record<string, unknown>
        expect(message['@class']).toBe(ALL_PADS_STOP_REQUEST_CLASS)
        expect(Object.keys(message).sort()).toEqual(['@class', 'isImmediately', 'messageId'])

        handler.destroy()
    })

    it('logs a warning instead of throwing when there is no open connection', async () => {
        const log = vi.fn()
        const handler = createHandler({log})
        // Deliberately never calling connect(): this.socket stays undefined, mirroring a pad-related
        // action fired before the module has ever connected to the server.

        expect(() => handler.stopAllPads()).not.toThrow()
        await Promise.resolve()
        await Promise.resolve()

        expect(log).toHaveBeenCalledWith('warn', expect.stringContaining('Failed to stop all pads'))

        handler.destroy()
    })
})

describe('ClientWebSocketHandler.changeGlobalVolume', () => {
    it('sends a GlobalChangeVolumeRequest with the given volume once the socket is open', () => {
        const handler = createHandler()
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()
        socket!.readyState = FakeWebSocket.OPEN

        handler.changeGlobalVolume(0.65)

        expect(socket!.sent).toHaveLength(1)
        const message = JSON.parse(socket!.sent[0]) as Record<string, unknown>
        expect(message['@class']).toBe(GLOBAL_CHANGE_VOLUME_REQUEST_CLASS)
        expect(message.volume).toBe(0.65)
        expect(Object.keys(message).sort()).toEqual(['@class', 'messageId', 'volume'])

        handler.destroy()
    })

    it('logs a warning instead of throwing when there is no open connection', async () => {
        const log = vi.fn()
        const handler = createHandler({log})

        expect(() => handler.changeGlobalVolume(0.5)).not.toThrow()
        await Promise.resolve()
        await Promise.resolve()

        expect(log).toHaveBeenCalledWith('warn', expect.stringContaining('Failed to change the global volume to 0.5'))

        handler.destroy()
    })
})

describe('ClientWebSocketHandler ProjectSettingsUpdate handling', () => {
    it('forwards a broadcast volume change to onProjectSettingsChanged', () => {
        const onProjectSettingsChanged = vi.fn()
        const handler = createHandler({onProjectSettingsChanged})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()

        const broadcast = {
            '@class': PROJECT_SETTINGS_UPDATE_CLASS,
            messageId: 'server-generated-id',
            projectMetadata: {
                id: 'project-1',
                name: 'Test project',
                numberOfHorizontalPads: 5,
                numberOfVerticalPads: 4,
                volume: 0.65,
            },
        }
        socket!.emit('message', JSON.stringify(broadcast))

        expect(onProjectSettingsChanged).toHaveBeenCalledTimes(1)
        expect(onProjectSettingsChanged.mock.calls[0]?.[0]).toMatchObject({volume: 0.65})

        handler.destroy()
    })

    it('logs a warning instead of calling onProjectSettingsChanged for a malformed broadcast', () => {
        const onProjectSettingsChanged = vi.fn()
        const log = vi.fn()
        const handler = createHandler({onProjectSettingsChanged, log})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()

        socket!.emit(
            'message',
            JSON.stringify({'@class': PROJECT_SETTINGS_UPDATE_CLASS, messageId: 'server-generated-id'}),
        )

        expect(onProjectSettingsChanged).not.toHaveBeenCalled()
        expect(log).toHaveBeenCalledWith('warn', expect.stringContaining('Received malformed ProjectSettingsUpdate'))

        handler.destroy()
    })
})

describe('ClientWebSocketHandler.showPage', () => {
    it('sends a ProjectPageShowRequest with the given (0-based) index once the socket is open', () => {
        const handler = createHandler()
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()
        socket!.readyState = FakeWebSocket.OPEN

        handler.showPage(3)

        expect(socket!.sent).toHaveLength(1)
        const message = JSON.parse(socket!.sent[0]) as Record<string, unknown>
        expect(message['@class']).toBe(PROJECT_PAGE_SHOW_REQUEST_CLASS)
        expect(message.index).toBe(3)
        expect(Object.keys(message).sort()).toEqual(['@class', 'index', 'messageId'])

        handler.destroy()
    })

    it('logs a warning instead of throwing when there is no open connection', async () => {
        const log = vi.fn()
        const handler = createHandler({log})

        expect(() => handler.showPage(2)).not.toThrow()
        await Promise.resolve()
        await Promise.resolve()

        expect(log).toHaveBeenCalledWith('warn', expect.stringContaining('Failed to show page 2'))

        handler.destroy()
    })
})

describe('ClientWebSocketHandler ProjectPageShownUpdate handling', () => {
    it('forwards a broadcast page change to onPageShown', () => {
        const onPageShown = vi.fn()
        const handler = createHandler({onPageShown})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()

        socket!.emit(
            'message',
            JSON.stringify({'@class': PROJECT_PAGE_SHOWN_UPDATE_CLASS, messageId: 'server-generated-id', index: 4}),
        )

        expect(onPageShown).toHaveBeenCalledWith(4)

        handler.destroy()
    })

    it('logs a warning instead of calling onPageShown for a malformed broadcast', () => {
        const onPageShown = vi.fn()
        const log = vi.fn()
        const handler = createHandler({onPageShown, log})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()

        socket!.emit(
            'message',
            JSON.stringify({'@class': PROJECT_PAGE_SHOWN_UPDATE_CLASS, messageId: 'server-generated-id'}),
        )

        expect(onPageShown).not.toHaveBeenCalled()
        expect(log).toHaveBeenCalledWith('warn', expect.stringContaining('Received malformed ProjectPageShownUpdate'))

        handler.destroy()
    })
})

describe('ClientWebSocketHandler page-CRUD refetch', () => {
    it('re-fetches the project and calls onPagesChanged when a page-CRUD broadcast arrives', async () => {
        const onPagesChanged = vi.fn()
        const handler = createHandler({onPagesChanged})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()
        socket!.readyState = FakeWebSocket.OPEN

        // Any of the six page-CRUD classes triggers the same generic re-fetch; PageAddUpdate stands in.
        socket!.emit(
            'message',
            JSON.stringify({
                '@class': 'de.tobias.playwall.common.api.page.update.PageAddUpdate',
                messageId: 'server-broadcast-id',
            }),
        )

        expect(socket!.sent).toHaveLength(1)
        const refetchRequest = JSON.parse(socket!.sent[0]) as { messageId: string; '@class': string }
        expect(refetchRequest['@class']).toBe(PROJECT_GET_CURRENT_REQUEST_CLASS)

        const project = {
            metadata: {id: 'project-1', name: 'Test project', numberOfHorizontalPads: 5, numberOfVerticalPads: 4},
            pages: [],
        }
        socket!.emit(
            'message',
            JSON.stringify({
                '@class': PROJECT_GET_CURRENT_RESPONSE_CLASS,
                messageId: refetchRequest.messageId,
                project
            }),
        )
        // sendRequest()'s Promise resolves synchronously, but the .then() handler that calls
        // onPagesChanged only runs on a later microtask.
        await Promise.resolve()

        expect(onPagesChanged).toHaveBeenCalledTimes(1)
        expect(onPagesChanged.mock.calls[0]?.[0]).toMatchObject({metadata: {id: 'project-1'}})

        handler.destroy()
    })
})

describe('ClientWebSocketHandler connect() -> fetchCurrentProject on open', () => {
    it('reports the currently loaded project, its pad statuses, and its shown page after connecting', async () => {
        const onProjectLoaded = vi.fn()
        const onPadStatusById = vi.fn()
        const onPageShown = vi.fn()
        const handler = createHandler({onProjectLoaded, onPadStatusById: onPadStatusById, onPageShown})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()
        socket!.readyState = FakeWebSocket.OPEN

        socket!.emit('open')

        expect(socket!.sent).toHaveLength(1)
        const request = JSON.parse(socket!.sent[0]) as { messageId: string; '@class': string }
        expect(request['@class']).toBe(PROJECT_GET_CURRENT_REQUEST_CLASS)

        const project = {
            metadata: {id: 'project-1', name: 'Test project', numberOfHorizontalPads: 5, numberOfVerticalPads: 4},
            pages: [],
        }
        socket!.emit(
            'message',
            JSON.stringify({
                '@class': PROJECT_GET_CURRENT_RESPONSE_CLASS,
                messageId: request.messageId,
                project,
                currentPageIndex: 2,
                padStatusById: {'pad-1': 'PLAYING'},
            }),
        )
        await Promise.resolve()

        expect(onProjectLoaded).toHaveBeenCalledWith(project)
        expect(onPadStatusById).toHaveBeenCalledWith({'pad-1': 'PLAYING'})
        expect(onPageShown).toHaveBeenCalledWith(2)

        handler.destroy()
    })

    it('defaults to page 0 when the response carries no current page (e.g. a freshly created project)', async () => {
        const onPageShown = vi.fn()
        const handler = createHandler({onPageShown})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()
        socket!.readyState = FakeWebSocket.OPEN

        socket!.emit('open')
        const request = JSON.parse(socket!.sent[0]) as { messageId: string }
        socket!.emit(
            'message',
            JSON.stringify({
                '@class': PROJECT_GET_CURRENT_RESPONSE_CLASS,
                messageId: request.messageId,
                project: {
                    metadata: {
                        id: 'project-1',
                        name: 'Test project',
                        numberOfHorizontalPads: 1,
                        numberOfVerticalPads: 1
                    },
                    pages: [],
                },
            }),
        )
        await Promise.resolve()

        expect(onPageShown).toHaveBeenCalledWith(0)

        handler.destroy()
    })

    it('reports that no project is loaded when the server responds with an error', async () => {
        const onProjectCleared = vi.fn()
        const onProjectLoaded = vi.fn()
        const handler = createHandler({onProjectCleared, onProjectLoaded})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()
        socket!.readyState = FakeWebSocket.OPEN

        socket!.emit('open')
        const request = JSON.parse(socket!.sent[0]) as { messageId: string }
        socket!.emit(
            'message',
            JSON.stringify({
                '@class': ERROR_MESSAGE_CLASS,
                messageId: request.messageId,
                message: 'Es ist kein Projekt geladen.',
                error: {'@class': PROJECT_NOT_LOADED_ERROR_CLASS},
            }),
        )
        // fetchCurrentProject() chains .then().catch() off sendRequest()'s promise, so the rejection
        // needs to propagate through an extra microtask hop compared to a plain single .catch(); a
        // macrotask flush is a simpler, more robust wait than counting exact microtask hops.
        await new Promise((resolve) => setTimeout(resolve, 0))

        expect(onProjectCleared).toHaveBeenCalledTimes(1)
        expect(onProjectLoaded).not.toHaveBeenCalled()

        handler.destroy()
    })

    it('just logs a warning (without clearing/loading anything) if the request fails for another reason', async () => {
        const onProjectCleared = vi.fn()
        const onProjectLoaded = vi.fn()
        const log = vi.fn()
        const handler = createHandler({onProjectCleared, onProjectLoaded, log})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()
        socket!.readyState = FakeWebSocket.OPEN

        socket!.emit('open')
        // Simulate the connection dropping before a response ever arrives.
        socket!.emit('close')
        await new Promise((resolve) => setTimeout(resolve, 0))

        expect(onProjectCleared).not.toHaveBeenCalled()
        expect(onProjectLoaded).not.toHaveBeenCalled()
        expect(log).toHaveBeenCalledWith('warn', expect.stringContaining('Failed to fetch the currently loaded project'))

        handler.destroy()
    })
})

describe('ClientWebSocketHandler ProjectLoadedUpdate handling', () => {
    it('forwards a broadcast "a new project was opened" to onProjectLoaded', () => {
        const onProjectLoaded = vi.fn()
        const handler = createHandler({onProjectLoaded})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()

        const project = {
            metadata: {
                id: 'project-2',
                name: 'Newly opened project',
                numberOfHorizontalPads: 3,
                numberOfVerticalPads: 3
            },
            pages: [],
        }
        socket!.emit('message', JSON.stringify({'@class': PROJECT_LOADED_UPDATE_CLASS, messageId: 'm', project}))

        expect(onProjectLoaded).toHaveBeenCalledWith(project)

        handler.destroy()
    })

    it('logs a warning instead of calling onProjectLoaded for a malformed broadcast', () => {
        const onProjectLoaded = vi.fn()
        const log = vi.fn()
        const handler = createHandler({onProjectLoaded, log})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()

        socket!.emit('message', JSON.stringify({'@class': PROJECT_LOADED_UPDATE_CLASS, messageId: 'm'}))

        expect(onProjectLoaded).not.toHaveBeenCalled()
        expect(log).toHaveBeenCalledWith('warn', expect.stringContaining('Received malformed ProjectLoadedUpdate'))

        handler.destroy()
    })
})

describe('ClientWebSocketHandler.playPad / stopPad', () => {
    it('sends a PadPlayRequest with the given padId once the socket is open', () => {
        const handler = createHandler()
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()
        socket!.readyState = FakeWebSocket.OPEN

        handler.playPad('pad-1')

        expect(socket!.sent).toHaveLength(1)
        const message = JSON.parse(socket!.sent[0]) as Record<string, unknown>
        expect(message['@class']).toBe(PAD_PLAY_REQUEST_CLASS)
        expect(message.padId).toBe('pad-1')

        handler.destroy()
    })

    it('sends a (graceful, non-immediate) PadStopRequest with the given padId once the socket is open', () => {
        const handler = createHandler()
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()
        socket!.readyState = FakeWebSocket.OPEN

        handler.stopPad('pad-1')

        expect(socket!.sent).toHaveLength(1)
        const message = JSON.parse(socket!.sent[0]) as Record<string, unknown>
        expect(message['@class']).toBe(PAD_STOP_REQUEST_CLASS)
        expect(message.padId).toBe('pad-1')
        expect(message.isImmediately).toBe(false)

        handler.destroy()
    })

    it('logs a warning instead of throwing when there is no open connection', async () => {
        const log = vi.fn()
        const handler = createHandler({log})

        expect(() => handler.playPad('pad-1')).not.toThrow()
        expect(() => handler.stopPad('pad-1')).not.toThrow()
        await Promise.resolve()
        await Promise.resolve()

        expect(log).toHaveBeenCalledWith('warn', expect.stringContaining('Failed to play pad pad-1'))
        expect(log).toHaveBeenCalledWith('warn', expect.stringContaining('Failed to stop pad pad-1'))

        handler.destroy()
    })
})

describe('ClientWebSocketHandler PadStatusUpdate handling', () => {
    it('forwards a broadcast pad status change to onPadStatus', () => {
        const onPadStatus = vi.fn()
        const handler = createHandler({onPadStatus})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()

        socket!.emit(
            'message',
            JSON.stringify({'@class': PAD_STATUS_UPDATE_CLASS, messageId: 'm', padId: 'pad-1', status: 'PLAYING'}),
        )

        expect(onPadStatus).toHaveBeenCalledWith('pad-1', 'PLAYING')

        handler.destroy()
    })

    it('logs a warning instead of calling onPadStatus for a malformed broadcast', () => {
        const onPadStatus = vi.fn()
        const log = vi.fn()
        const handler = createHandler({onPadStatus, log})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()

        socket!.emit('message', JSON.stringify({'@class': PAD_STATUS_UPDATE_CLASS, messageId: 'm', padId: 'pad-1'}))

        expect(onPadStatus).not.toHaveBeenCalled()
        expect(log).toHaveBeenCalledWith('warn', expect.stringContaining('Received malformed PadStatusUpdate'))

        handler.destroy()
    })
})

describe('ClientWebSocketHandler PadReplaceUpdate / PadSwapUpdate handling', () => {
    it('forwards a broadcast pad replace to onPadReplaced', () => {
        const onPadReplaced = vi.fn()
        const handler = createHandler({onPadReplaced})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()

        const sourcePad = {
            id: 'pad-2',
            position: 1,
            name: 'New pad',
            defaultColor: null,
            playColor: null,
            introColor: null
        }
        socket!.emit(
            'message',
            JSON.stringify({'@class': PAD_REPLACE_UPDATE_CLASS, messageId: 'm', sourcePad, targetPadId: 'pad-1'}),
        )

        expect(onPadReplaced).toHaveBeenCalledWith(sourcePad, 'pad-1')

        handler.destroy()
    })

    it('forwards a broadcast pad swap to onPadsSwapped', () => {
        const onPadsSwapped = vi.fn()
        const handler = createHandler({onPadsSwapped})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()

        socket!.emit(
            'message',
            JSON.stringify({'@class': PAD_SWAP_UPDATE_CLASS, messageId: 'm', pad1: 'pad-1', pad2: 'pad-2'}),
        )

        expect(onPadsSwapped).toHaveBeenCalledWith('pad-1', 'pad-2')

        handler.destroy()
    })

    it('logs a warning instead of calling onPadReplaced for a malformed broadcast', () => {
        const onPadReplaced = vi.fn()
        const log = vi.fn()
        const handler = createHandler({onPadReplaced, log})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()

        socket!.emit('message', JSON.stringify({'@class': PAD_REPLACE_UPDATE_CLASS, messageId: 'm'}))

        expect(onPadReplaced).not.toHaveBeenCalled()
        expect(log).toHaveBeenCalledWith('warn', expect.stringContaining('Received malformed PadReplaceUpdate'))

        handler.destroy()
    })
})

describe('ClientWebSocketHandler PadUpdate handling', () => {
    it('forwards a broadcast pad settings change (e.g. edited colors) to onPadUpdated', () => {
        const onPadUpdated = vi.fn()
        const handler = createHandler({onPadUpdated})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()

        const pad = {
            id: 'pad-1',
            position: 0,
            name: 'Renamed',
            defaultColor: 'RED1',
            playColor: 'BLUE1',
            introColor: null,
        }
        socket!.emit('message', JSON.stringify({'@class': PAD_UPDATE_CLASS, messageId: 'm', pad}))

        expect(onPadUpdated).toHaveBeenCalledWith(pad)

        handler.destroy()
    })

    it('logs a warning instead of calling onPadUpdated for a malformed broadcast', () => {
        const onPadUpdated = vi.fn()
        const log = vi.fn()
        const handler = createHandler({onPadUpdated, log})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)
        expect(socket).toBeDefined()

        socket!.emit('message', JSON.stringify({'@class': PAD_UPDATE_CLASS, messageId: 'm'}))

        expect(onPadUpdated).not.toHaveBeenCalled()
        expect(log).toHaveBeenCalledWith('warn', expect.stringContaining('Received malformed PadUpdate'))

        handler.destroy()
    })
})

describe('ClientWebSocketHandler handshake rejection', () => {
    it('reports the server-provided reason as a rejected status and does not overwrite it with disconnected', () => {
        const onStatusChange = vi.fn()
        const handler = createHandler({onStatusChange})
        handler.connect()
        const socket = FakeWebSocket.instances.at(-1)!
        const request = {destroy: vi.fn()}

        socket.emit('unexpected-response', request, {
            statusCode: 400,
            headers: {'x-reject-reason': 'Version mismatch: client 1, server 2'},
        })
        socket.emit('close')

        expect(onStatusChange).toHaveBeenCalledWith('rejected', 'Version mismatch: client 1, server 2')
        expect(onStatusChange).not.toHaveBeenCalledWith('disconnected')
        expect(request.destroy).toHaveBeenCalled()

        handler.destroy()
    })

    it('falls back to the HTTP status when the server gives no reason', () => {
        const onStatusChange = vi.fn()
        const handler = createHandler({onStatusChange})
        handler.connect()

        FakeWebSocket.instances.at(-1)!.emit('unexpected-response', {destroy: vi.fn()}, {statusCode: 502, headers: {}})

        expect(onStatusChange).toHaveBeenCalledWith('rejected', 'Server responded with HTTP 502')

        handler.destroy()
    })
})
