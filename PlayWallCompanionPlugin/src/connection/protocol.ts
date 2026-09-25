import {randomUUID} from 'node:crypto'
import {z} from 'zod'

/**
 * PlayWall's wire format stamps every message with the fully qualified Java class name in "@class"
 * (Jackson `@JsonTypeInfo(use = Id.CLASS)`), see PlayWallCommon's BaseMessage.
 */
const BASE_ENVELOPE = z.looseObject({
    '@class': z.string(),
    messageId: z.string(),
})

export type BaseEnvelope = z.infer<typeof BASE_ENVELOPE>

export function parseEnvelope(raw: unknown): BaseEnvelope {
    return BASE_ENVELOPE.parse(raw)
}

// Fully qualified Java class names used as "@class" discriminators, see PlayWallCommon.
export const PROJECT_GET_REQUEST_CLASS = 'de.tobias.playwall.common.api.project.request.ProjectGetRequest'
export const PROJECT_GET_RESPONSE_CLASS = 'de.tobias.playwall.common.api.project.request.ProjectGetResponse'
export const PROJECT_LOADED_UPDATE_CLASS = 'de.tobias.playwall.common.api.project.update.ProjectLoadedUpdate'
export const PROJECT_PAGE_SHOWN_UPDATE_CLASS = 'de.tobias.playwall.common.api.project.update.ProjectPageShownUpdate'
export const PROJECT_PAGE_SHOW_REQUEST_CLASS = 'de.tobias.playwall.common.api.project.request.ProjectPageShowRequest'
export const GLOBAL_CHANGE_VOLUME_REQUEST_CLASS = 'de.tobias.playwall.common.api.project.request.GlobalChangeVolumeRequest'
export const PROJECT_SETTINGS_UPDATE_CLASS = 'de.tobias.playwall.common.api.project.update.ProjectSettingsUpdate'
export const PAD_STATUS_UPDATE_CLASS = 'de.tobias.playwall.common.api.pad.update.PadStatusUpdate'
export const PAD_REPLACE_UPDATE_CLASS = 'de.tobias.playwall.common.api.pad.update.PadReplaceUpdate'
export const PAD_SWAP_UPDATE_CLASS = 'de.tobias.playwall.common.api.pad.update.PadSwapUpdate'
export const PAD_PLAY_REQUEST_CLASS = 'de.tobias.playwall.common.api.pad.request.PadPlayRequest'
export const PAD_STOP_REQUEST_CLASS = 'de.tobias.playwall.common.api.pad.request.PadStopRequest'
export const ALL_PADS_STOP_REQUEST_CLASS = 'de.tobias.playwall.common.api.pad.request.AllPadsStopRequest'
export const ERROR_MESSAGE_CLASS = 'de.tobias.playwall.common.net.ErrorMessage'
export const PROJECT_NOT_LOADED_ERROR_CLASS = 'de.tobias.playwall.common.api.project.ProjectNotLoadedError'

/**
 * Broadcast whenever the set of pages, their order, or a page's own settings (name/color) change.
 * Their payload shapes differ (some carry a full page, some a page-id → new-position renumbering
 * map) — Companion doesn't model them individually, it just re-fetches the whole project when any of
 * these arrive (see ClientWebSocketHandler's `onPagesChanged`).
 */
export const PAGE_CRUD_UPDATE_CLASSES = [
    'de.tobias.playwall.common.api.page.update.PageAddUpdate',
    'de.tobias.playwall.common.api.page.update.PageDeleteUpdate',
    'de.tobias.playwall.common.api.page.update.PageInsertUpdate',
    'de.tobias.playwall.common.api.page.update.PageReorderUpdate',
    'de.tobias.playwall.common.api.page.update.PageReplaceUpdate',
    'de.tobias.playwall.common.api.page.update.PageSettingsUpdate',
] as const

/**
 * Mirrors PlayWallCommon's `Color` enum names (RED1..GRAY6). Kept as a bare string here; the actual
 * RGB values are looked up in `domain/padColor.ts`.
 */
const PROJECT_METADATA = z.looseObject({
    id: z.string(),
    name: z.string(),
    defaultColor: z.string().nullish(),
    playColor: z.string().nullish(),
    introColor: z.string().nullish(),
    numberOfHorizontalPads: z.number(),
    numberOfVerticalPads: z.number(),
    volume: z.number().nullish(),
})

export type ProjectMetadata = z.infer<typeof PROJECT_METADATA>

/**
 * Only the fields Companion currently needs are validated; `content` is not inspected. Pad-level
 * colors are optional overrides of the project's colors (PadDto.java allows them to be null).
 */
const PAD_DTO = z.looseObject({
    id: z.string(),
    position: z.number(),
    name: z.string().nullish(),
    defaultColor: z.string().nullish(),
    playColor: z.string().nullish(),
    introColor: z.string().nullish(),
})

export type PadDto = z.infer<typeof PAD_DTO>

const PAGE_SETTINGS_DTO = z.looseObject({
    name: z.string().nullish(),
    color: z.string().nullish(),
})

const PAGE_DTO = z.looseObject({
    id: z.string(),
    position: z.number(),
    settings: PAGE_SETTINGS_DTO.nullish(),
    pads: z.array(PAD_DTO),
})

export type PageDto = z.infer<typeof PAGE_DTO>

const PROJECT_DTO = z.looseObject({
    metadata: PROJECT_METADATA,
    pages: z.array(PAGE_DTO),
})

export type ProjectDto = z.infer<typeof PROJECT_DTO>

export const PROJECT_GET_RESPONSE = BASE_ENVELOPE.extend({
    project: PROJECT_DTO,
    currentPageIndex: z.number().nullish(),
    // Keyed by pad id (UUID string). Only populated together with `currentPageIndex` — see
    // ProjectGetResponse.java.
    padStatuses: z.record(z.string(), z.string()).nullish(),
})

export const PROJECT_LOADED_UPDATE = BASE_ENVELOPE.extend({
    project: PROJECT_DTO,
})

export const PROJECT_PAGE_SHOWN_UPDATE = BASE_ENVELOPE.extend({
    index: z.number(),
})

/** Broadcast whenever any project setting (incl. global volume) changes; carries the full metadata. */
export const PROJECT_SETTINGS_UPDATE = BASE_ENVELOPE.extend({
    projectMetadata: PROJECT_METADATA,
})

export const PAD_STATUS_UPDATE = BASE_ENVELOPE.extend({
    padId: z.string(),
    status: z.string(),
})

/**
 * Broadcast when a pad is moved (dropped onto an empty slot) or replaced (dropped-with-modifier to
 * duplicate) via drag & drop on the desktop client: `targetPadId` is the id of the pad being
 * overwritten, `sourcePad` is its full new content (including its new `position`).
 */
export const PAD_REPLACE_UPDATE = BASE_ENVELOPE.extend({
    sourcePad: PAD_DTO,
    targetPadId: z.string(),
})

/** Broadcast when two pads swap places via drag & drop on the desktop client. */
export const PAD_SWAP_UPDATE = BASE_ENVELOPE.extend({
    pad1: z.string(),
    pad2: z.string(),
})

export const ERROR_MESSAGE = BASE_ENVELOPE.extend({
    message: z.string(),
    error: z.looseObject({'@class': z.string()}),
})

export type ErrorMessageEnvelope = z.infer<typeof ERROR_MESSAGE>

export function buildProjectGetRequest(): { '@class': string; messageId: string; projectId: null } {
    return {
        '@class': PROJECT_GET_REQUEST_CLASS,
        messageId: randomUUID(),
        projectId: null,
    }
}

export function buildProjectPageShowRequest(index: number): { '@class': string; messageId: string; index: number } {
    return {
        '@class': PROJECT_PAGE_SHOW_REQUEST_CLASS,
        messageId: randomUUID(),
        index,
    }
}

export function buildGlobalChangeVolumeRequest(volume: number): {
    '@class': string;
    messageId: string;
    volume: number
} {
    return {
        '@class': GLOBAL_CHANGE_VOLUME_REQUEST_CLASS,
        messageId: randomUUID(),
        volume,
    }
}

export function buildPadPlayRequest(padId: string): { '@class': string; messageId: string; padId: string } {
    return {
        '@class': PAD_PLAY_REQUEST_CLASS,
        messageId: randomUUID(),
        padId,
    }
}

export function buildPadStopRequest(padId: string): {
    '@class': string;
    messageId: string;
    padId: string;
    isImmediately: boolean
} {
    return {
        '@class': PAD_STOP_REQUEST_CLASS,
        messageId: randomUUID(),
        padId,
        isImmediately: false,
    }
}

export function buildAllPadsStopRequest(): { '@class': string; messageId: string } {
    return {
        '@class': ALL_PADS_STOP_REQUEST_CLASS,
        messageId: randomUUID(),
    }
}
