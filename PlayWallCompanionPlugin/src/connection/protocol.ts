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
export const ERROR_MESSAGE_CLASS = 'de.tobias.playwall.common.net.ErrorMessage'
export const PROJECT_NOT_LOADED_ERROR_CLASS = 'de.tobias.playwall.common.api.project.ProjectNotLoadedError'

const PROJECT_METADATA = z.looseObject({
    id: z.string(),
    name: z.string(),
})

/**
 * Only the fields Companion currently needs are validated; `pages` is not inspected.
 */
const PROJECT_DTO = z.looseObject({
    metadata: PROJECT_METADATA,
    pages: z.array(z.unknown()),
})

export type ProjectDto = z.infer<typeof PROJECT_DTO>

export const PROJECT_GET_RESPONSE = BASE_ENVELOPE.extend({
    project: PROJECT_DTO,
})

export const PROJECT_LOADED_UPDATE = BASE_ENVELOPE.extend({
    project: PROJECT_DTO,
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
