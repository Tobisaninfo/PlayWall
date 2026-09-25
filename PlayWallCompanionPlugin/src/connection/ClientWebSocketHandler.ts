import {randomUUID} from 'node:crypto'
import {WebSocket} from 'ws'
import {z} from 'zod'
import {
    buildAllPadsStopRequest,
    buildGlobalChangeVolumeRequest,
    buildPadPlayRequest,
    buildPadStopRequest,
    buildProjectGetRequest,
    buildProjectPageShowRequest,
    ERROR_MESSAGE,
    ERROR_MESSAGE_CLASS,
    PAD_REPLACE_UPDATE,
    PAD_REPLACE_UPDATE_CLASS,
    PAD_STATUS_UPDATE,
    PAD_STATUS_UPDATE_CLASS,
    PAD_SWAP_UPDATE,
    PAD_SWAP_UPDATE_CLASS,
    PAGE_CRUD_UPDATE_CLASSES,
    parseEnvelope,
    PROJECT_GET_RESPONSE,
    PROJECT_LOADED_UPDATE,
    PROJECT_LOADED_UPDATE_CLASS,
    PROJECT_PAGE_SHOWN_UPDATE,
    PROJECT_PAGE_SHOWN_UPDATE_CLASS,
    PROJECT_SETTINGS_UPDATE,
    PROJECT_SETTINGS_UPDATE_CLASS,
    type PadDto,
    type ProjectDto,
    type ProjectMetadata,
} from './protocol.js'

export type ConnectionStatus = 'connecting' | 'connected' | 'disconnected'

function rawDataToString(data: Buffer | ArrayBuffer | Buffer[]): string {
    if (Array.isArray(data)) {
        return Buffer.concat(data).toString('utf8')
    }
    if (data instanceof ArrayBuffer) {
        return Buffer.from(data).toString('utf8')
    }
    return data.toString('utf8')
}

const PAGE_CRUD_UPDATE_CLASS_SET: ReadonlySet<string> = new Set(PAGE_CRUD_UPDATE_CLASSES)

export interface ClientWebSocketHandlerOptions {
    host: string
    port: number
    useTls: boolean
    reconnectDelaySeconds: number
    onStatusChange: (status: ConnectionStatus) => void
    onProjectLoaded: (project: ProjectDto) => void
    onProjectCleared: () => void
    onPageShown: (index: number) => void
    /** Any project setting (incl. global volume) changed on the desktop client. */
    onProjectSettingsChanged: (metadata: ProjectMetadata) => void
    onPadStatus: (padId: string, status: string) => void
    onPadStatuses: (statuses: Record<string, string>) => void
    /** A pad was moved or duplicated onto another pad's slot via drag & drop on the desktop client. */
    onPadReplaced: (newPad: PadDto, targetPadId: string) => void
    /** Two pads swapped places via drag & drop on the desktop client. */
    onPadsSwapped: (padId1: string, padId2: string) => void
    /**
     * A page was added, deleted, inserted, reordered, replaced, or had its settings (name/color)
     * changed on the desktop client. Carries the freshly re-fetched project — unlike
     * `onProjectLoaded`, this must not reset any playback/navigation state, just the project data.
     */
    onPagesChanged: (project: ProjectDto) => void
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
            this.handleMessage(rawDataToString(data))
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

    /**
     * Immediately stops every currently playing pad. Errors are logged, not thrown; no local state
     * needs updating here — each stopped pad's own `PadStatusUpdate` broadcast (already handled via
     * `onPadStatus`) drives its variable/feedback update, same as a single `stopPad`.
     */
    stopAllPads(): void {
        this.sendRequest(buildAllPadsStopRequest(), GENERIC_RESPONSE).catch((error: unknown) => {
            this.options.log('warn', `Failed to stop all pads: ${String(error)}`)
        })
    }

    /** Tells the server to show the given (0-based) page. Errors are logged, not thrown. */
    showPage(index: number): void {
        this.sendRequest(buildProjectPageShowRequest(index), GENERIC_RESPONSE).catch((error: unknown) => {
            this.options.log('warn', `Failed to show page ${index}: ${String(error)}`)
        })
    }

    /** Sets PlayWall's global volume to the given absolute value (0-1). Errors are logged, not thrown. */
    changeGlobalVolume(volume: number): void {
        this.sendRequest(buildGlobalChangeVolumeRequest(volume), GENERIC_RESPONSE).catch((error: unknown) => {
            this.options.log('warn', `Failed to change the global volume to ${volume}: ${String(error)}`)
        })
    }

    /** Re-fetches the project after a page was added/removed/reordered/renamed, via `onPagesChanged`. */
    private refetchProject(): void {
        this.sendRequest(buildProjectGetRequest(), PROJECT_GET_RESPONSE)
            .then((response) => {
                this.options.onPagesChanged(response.project)
            })
            .catch((error: unknown) => {
                this.options.log('warn', `Failed to refresh the project after a page change: ${String(error)}`)
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

        if (PAGE_CRUD_UPDATE_CLASS_SET.has(envelope['@class'])) {
            this.refetchProject()
            return
        }

        this.dispatchBroadcast(envelope)
    }

    /**
     * Parses and dispatches a broadcast (non-response, non-page-CRUD) message by its `@class`. Kept
     * separate from `handleMessage` so each case is a single straight-line statement — the actual
     * parse-or-warn branching lives once, in `parseUpdate`, instead of being repeated (and adding to
     * `handleMessage`'s cognitive complexity) for every message type.
     */
    private dispatchBroadcast(envelope: Record<string, unknown>): void {
        switch (envelope['@class']) {
            case PROJECT_LOADED_UPDATE_CLASS:
                this.parseUpdate(envelope, PROJECT_LOADED_UPDATE, 'ProjectLoadedUpdate', (data) => this.options.onProjectLoaded(data.project))
                break
            case PROJECT_PAGE_SHOWN_UPDATE_CLASS:
                this.parseUpdate(envelope, PROJECT_PAGE_SHOWN_UPDATE, 'ProjectPageShownUpdate', (data) => this.options.onPageShown(data.index))
                break
            case PROJECT_SETTINGS_UPDATE_CLASS:
                this.parseUpdate(envelope, PROJECT_SETTINGS_UPDATE, 'ProjectSettingsUpdate', (data) => this.options.onProjectSettingsChanged(data.projectMetadata))
                break
            case PAD_STATUS_UPDATE_CLASS:
                this.parseUpdate(envelope, PAD_STATUS_UPDATE, 'PadStatusUpdate', (data) => this.options.onPadStatus(data.padId, data.status))
                break
            case PAD_REPLACE_UPDATE_CLASS:
                this.parseUpdate(envelope, PAD_REPLACE_UPDATE, 'PadReplaceUpdate', (data) => this.options.onPadReplaced(data.sourcePad, data.targetPadId))
                break
            case PAD_SWAP_UPDATE_CLASS:
                this.parseUpdate(envelope, PAD_SWAP_UPDATE, 'PadSwapUpdate', (data) => this.options.onPadsSwapped(data.pad1, data.pad2))
                break
            case ERROR_MESSAGE_CLASS:
                this.logUnsolicitedError(envelope)
                break
        }
    }

    /** Parses `envelope` against `schema`; calls `onSuccess` if it matches, otherwise logs a warning naming `messageName`. */
    private parseUpdate<Schema extends z.ZodType>(
        envelope: Record<string, unknown>,
        schema: Schema,
        messageName: string,
        onSuccess: (data: z.infer<Schema>) => void,
    ): void {
        const result = schema.safeParse(envelope)
        if (result.success) {
            onSuccess(result.data)
        } else {
            this.options.log('warn', `Received malformed ${messageName}: ${String(result.error)}`)
        }
    }

    private logUnsolicitedError(envelope: Record<string, unknown>): void {
        const error = ERROR_MESSAGE.safeParse(envelope)
        this.options.log('warn', error.success
            ? `Received an unsolicited error from the PlayWall server: ${error.data.message}`
            : `Received a malformed ErrorMessage: ${String(error.error)}`)
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
