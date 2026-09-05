import {randomUUID} from 'node:crypto'
import {WebSocket} from 'ws'
import {parseEnvelope} from './protocol.js'

export type ConnectionStatus = 'connecting' | 'connected' | 'disconnected'

export interface ClientWebSocketHandlerOptions {
    host: string
    port: number
    useTls: boolean
    reconnectDelaySeconds: number
    onStatusChange: (status: ConnectionStatus) => void
    log: (level: 'debug' | 'warn', message: string) => void
}

const MAX_RECONNECT_DELAY_MS = 30_000

/**
 * Wraps a single WebSocket connection to a PlayWall server and reconnects automatically with
 * exponential backoff if the connection is lost.
 */
export class ClientWebSocketHandler {
    private readonly clientId = randomUUID()

    private socket: WebSocket | undefined
    private reconnectTimer: ReturnType<typeof setTimeout> | undefined
    private reconnectAttempt = 0
    private destroyed = false

    constructor(private readonly options: ClientWebSocketHandlerOptions) {
    }

    connect(): void {
        this.destroyed = false
        this.openSocket()
    }

    destroy(): void {
        this.destroyed = true
        this.clearReconnectTimer()

        this.socket?.removeAllListeners()
        this.socket?.close()
        this.socket = undefined
    }

    private openSocket(): void {
        this.options.onStatusChange('connecting')

        const protocol = this.options.useTls ? 'wss' : 'ws'
        const url = `${protocol}://${this.options.host}:${this.options.port}/websocket`

        const socket = new WebSocket(url, {headers: {clientId: this.clientId}})
        this.socket = socket

        socket.on('open', () => {
            this.reconnectAttempt = 0
            this.options.onStatusChange('connected')
        })

        socket.on('message', (data) => {
            this.handleMessage(data.toString())
        })

        socket.on('close', () => {
            if (!this.destroyed) {
                this.options.onStatusChange('disconnected')
                this.scheduleReconnect()
            }
        })

        socket.on('error', (error) => {
            this.options.log('warn', `WebSocket error: ${error.message}`)
        })
    }

    private handleMessage(raw: string): void {
        let envelope
        try {
            envelope = parseEnvelope(JSON.parse(raw))
        } catch (error) {
            this.options.log('warn', `Received malformed message from PlayWall server: ${String(error)}`)
            return
        }

        this.options.log('debug', `Received message: ${envelope['@class']}`)
    }

    private scheduleReconnect(): void {
        this.clearReconnectTimer()

        const baseDelayMs = Math.max(1, this.options.reconnectDelaySeconds) * 1000
        const backoffDelayMs = Math.min(baseDelayMs * 2 ** this.reconnectAttempt, MAX_RECONNECT_DELAY_MS)
        this.reconnectAttempt += 1

        this.reconnectTimer = setTimeout(() => {
            if (!this.destroyed) {
                this.openSocket()
            }
        }, backoffDelayMs)
    }

    private clearReconnectTimer(): void {
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer)
            this.reconnectTimer = undefined
        }
    }
}
