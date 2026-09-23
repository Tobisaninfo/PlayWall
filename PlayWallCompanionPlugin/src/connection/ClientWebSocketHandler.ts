import {randomUUID} from 'node:crypto'
import {WebSocket} from 'ws'
import {z} from 'zod'
import {
    buildPadPlayRequest,
    buildPadStopRequest,
    buildProjectGetRequest,
    ERROR_MESSAGE,
    ERROR_MESSAGE_CLASS,
    PAD_REPLACE_UPDATE,
    PAD_REPLACE_UPDATE_CLASS,
    PAD_STATUS_UPDATE,
    PAD_STATUS_UPDATE_CLASS,
    PAD_SWAP_UPDATE,
    PAD_SWAP_UPDATE_CLASS,
    parseEnvelope,
    PROJECT_GET_RESPONSE,
    PROJECT_LOADED_UPDATE,
    PROJECT_LOADED_UPDATE_CLASS,
    PROJECT_PAGE_SHOWN_UPDATE,
    PROJECT_PAGE_SHOWN_UPDATE_CLASS,
    type PadDto,
    type ProjectDto,
} from './protocol.js'

export type ConnectionStatus = 'connecting' | 'connected' | 'disconnected'

export interface ClientWebSocketHandlerOptions {
    host: string
    port: number
    useTls: boolean
    reconnectDelaySeconds: number
    onStatusChange: (status: ConnectionStatus) => void
    onProjectLoaded: (project: ProjectDto) => void
    onProjectCleared: () => void
    onPageShown: (index: number) => void
    onPadStatus: (padId: string, status: string) => void
    onPadStatuses: (statuses: Record<string, string>) => void
    /** A pad was moved or duplicated onto another pad's slot via drag & drop on the desktop client. */
    onPadReplaced: (newPad: PadDto, targetPadId: string) => void
    /** Two pads swapped places via drag & drop on the desktop client. */
    onPadsSwapped: (padId1: string, padId2: string) => void
    log: (level: 'debug' | 'warn', message: string) => void
}

/** A generic PlayWall ResponseMessage carries no fields beyond the base envelope. */
const GENERIC_RESPONSE = z.looseObject({})

const MAX_RECONNECT_DELAY_MS = 30_000
const REQUEST_TIMEOUT_MS = 5_000

/**
 * Rejection reason for a request that came back as PlayWall's generic ErrorMessage, see
 * PlayWallCommon's ErrorMessage/ServerError.
 */
export class ServerErrorResponse extends Error {
    constructor(
        message: string,
        readonly errorClass: string,
    ) {
        super(message)
        this.name = 'ServerErrorResponse'
    }
}

interface PendingRequest {
    resolve: (value: unknown) => void
    reject: (reason: unknown) => void
    schema: z.ZodType
    timer: ReturnType<typeof setTimeout>
}

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

    private readonly pendingRequests = new Map<string, PendingRequest>()

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

        this.rejectAllPendingRequests(new Error('WebSocket connection was destroyed'))
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
            this.fetchCurrentProject()
        })

        socket.on('message', (data) => {
            this.handleMessage(data.toString())
        })

        socket.on('close', () => {
            this.rejectAllPendingRequests(new Error('WebSocket connection was closed'))

            if (!this.destroyed) {
                this.options.onStatusChange('disconnected')
                this.scheduleReconnect()
            }
        })

        socket.on('error', (error) => {
            this.options.log('warn', `WebSocket error: ${error.message}`)
        })
    }

    /**
     * Asks the server whether a project is currently open and, if so, reports it via
     * `onProjectLoaded`. Runs after every successful (re)connect so state missed while
     * disconnected is picked up too — this includes each pad's current status: a `PadStatusUpdate`
     * only broadcasts when a status *changes*, so without this a pad that hasn't changed since
     * connecting would otherwise never be known.
     */
    private fetchCurrentProject(): void {
        this.sendRequest(buildProjectGetRequest(), PROJECT_GET_RESPONSE)
            .then((response) => {
                this.options.onProjectLoaded(response.project)
                if (response.padStatuses) {
                    this.options.onPadStatuses(response.padStatuses)
                }
                this.options.onPageShown(response.currentPageIndex ?? 0)
            })
            .catch((error: unknown) => {
                if (error instanceof ServerErrorResponse) {
                    this.options.onProjectCleared()
                    this.options.log('debug', `No project currently loaded on the PlayWall server: ${error.message}`)
                    return
                }

                this.options.log('warn', `Failed to fetch the currently loaded project: ${String(error)}`)
            })
    }

    /** Plays the given pad. Errors (e.g. an invalid/empty pad) are logged, not thrown. */
    playPad(padId: string): void {
        this.sendRequest(buildPadPlayRequest(padId), GENERIC_RESPONSE).catch((error: unknown) => {
            this.options.log('warn', `Failed to play pad ${padId}: ${String(error)}`)
        })
    }

    /** Stops the given pad with a graceful fade-out. Errors are logged, not thrown. */
    stopPad(padId: string): void {
        this.sendRequest(buildPadStopRequest(padId), GENERIC_RESPONSE).catch((error: unknown) => {
            this.options.log('warn', `Failed to stop pad ${padId}: ${String(error)}`)
        })
    }

    private sendRequest<Schema extends z.ZodType>(message: {
        messageId: string
    }, schema: Schema): Promise<z.infer<Schema>> {
        return new Promise((resolve, reject) => {
            if (this.socket?.readyState !== WebSocket.OPEN) {
                reject(new Error('WebSocket is not connected'))
                return
            }

            const timer = setTimeout(() => {
                this.pendingRequests.delete(message.messageId)
                reject(new Error('Timed out waiting for a response from the PlayWall server'))
            }, REQUEST_TIMEOUT_MS)

            this.pendingRequests.set(message.messageId, {
                resolve: resolve as (value: unknown) => void,
                reject,
                schema,
                timer
            })
            this.socket.send(JSON.stringify(message))
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

        const pending = this.pendingRequests.get(envelope.messageId)
        if (pending) {
            this.pendingRequests.delete(envelope.messageId)
            clearTimeout(pending.timer)
            this.resolvePendingRequest(pending, envelope)
            return
        }

        switch (envelope['@class']) {
            case PROJECT_LOADED_UPDATE_CLASS: {
                const update = PROJECT_LOADED_UPDATE.safeParse(envelope)
                if (update.success) {
                    this.options.onProjectLoaded(update.data.project)
                } else {
                    this.options.log('warn', `Received malformed ProjectLoadedUpdate: ${String(update.error)}`)
                }
                break
            }
            case PROJECT_PAGE_SHOWN_UPDATE_CLASS: {
                const update = PROJECT_PAGE_SHOWN_UPDATE.safeParse(envelope)
                if (update.success) {
                    this.options.onPageShown(update.data.index)
                } else {
                    this.options.log('warn', `Received malformed ProjectPageShownUpdate: ${String(update.error)}`)
                }
                break
            }
            case PAD_STATUS_UPDATE_CLASS: {
                const update = PAD_STATUS_UPDATE.safeParse(envelope)
                if (update.success) {
                    this.options.onPadStatus(update.data.padId, update.data.status)
                } else {
                    this.options.log('warn', `Received malformed PadStatusUpdate: ${String(update.error)}`)
                }
                break
            }
            case PAD_REPLACE_UPDATE_CLASS: {
                const update = PAD_REPLACE_UPDATE.safeParse(envelope)
                if (update.success) {
                    this.options.onPadReplaced(update.data.sourcePad, update.data.targetPadId)
                } else {
                    this.options.log('warn', `Received malformed PadReplaceUpdate: ${String(update.error)}`)
                }
                break
            }
            case PAD_SWAP_UPDATE_CLASS: {
                const update = PAD_SWAP_UPDATE.safeParse(envelope)
                if (update.success) {
                    this.options.onPadsSwapped(update.data.pad1, update.data.pad2)
                } else {
                    this.options.log('warn', `Received malformed PadSwapUpdate: ${String(update.error)}`)
                }
                break
            }
            case ERROR_MESSAGE_CLASS: {
                const error = ERROR_MESSAGE.safeParse(envelope)
                this.options.log('warn', error.success
                    ? `Received an unsolicited error from the PlayWall server: ${error.data.message}`
                    : `Received a malformed ErrorMessage: ${String(error.error)}`)
                break
            }
        }
    }

    private resolvePendingRequest(pending: PendingRequest, envelope: Record<string, unknown>): void {
        if (envelope['@class'] === ERROR_MESSAGE_CLASS) {
            const error = ERROR_MESSAGE.safeParse(envelope)
            if (error.success) {
                pending.reject(new ServerErrorResponse(error.data.message, error.data.error['@class']))
            } else {
                pending.reject(new Error(`Received malformed ErrorMessage: ${String(error.error)}`))
            }
            return
        }

        const parsed = pending.schema.safeParse(envelope)
        if (parsed.success) {
            pending.resolve(parsed.data)
        } else {
            pending.reject(new Error(`Received unexpected response: ${String(parsed.error)}`))
        }
    }

    private rejectAllPendingRequests(reason: unknown): void {
        for (const pending of this.pendingRequests.values()) {
            clearTimeout(pending.timer)
            pending.reject(reason)
        }
        this.pendingRequests.clear()
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
