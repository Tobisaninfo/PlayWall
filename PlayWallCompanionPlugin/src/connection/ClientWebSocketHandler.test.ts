import {beforeEach, describe, expect, it, vi} from 'vitest'
import {ALL_PADS_STOP_REQUEST_CLASS} from './protocol.js'

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
