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
