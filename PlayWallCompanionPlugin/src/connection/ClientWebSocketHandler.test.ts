import {beforeEach, describe, expect, it, vi} from 'vitest'
import {
    ALL_PADS_STOP_REQUEST_CLASS,
    GLOBAL_CHANGE_VOLUME_REQUEST_CLASS,
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
        onPadStatuses: vi.fn(),
        onPadReplaced: vi.fn(),
        onPadsSwapped: vi.fn(),
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
        expect(Object.keys(message).sort()).toEqual(['@class', 'messageId'])

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
