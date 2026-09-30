import {existsSync, readdirSync, readFileSync} from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {describe, expect, it} from 'vitest'
import * as protocol from './protocol.js'

/**
 * Guards against PlayWallCommon's protocol drifting away from what `connection/protocol.ts`
 * (Companion's zod mirror of it) expects, WITHOUT anyone having to remember to add a test for
 * each new message. Everything below is discovered from `protocol.ts`'s own exports at test-run
 * time, via two conventions the module already follows:
 *
 *  1. Every `build*Request` function returns an object carrying `'@class'` - that value alone
 *     tells us which PlayWallCommon request class to check its fields against.
 *  2. Every exported zod message schema (`PROJECT_LOADED_UPDATE`, `PAD_UPDATE`, ...) has a sibling
 *     string constant named `<SAME_NAME>_CLASS` naming the PlayWallCommon class it validates.
 *
 * Add a new request builder, a new `*_CLASS`/`*_CLASSES` constant, or a new schema (following
 * convention 2) in protocol.ts, and this file picks it up automatically - nothing here needs
 * touching. Nested DTOs (`PadDto`, `ProjectDto`, ...) are followed recursively by resolving each
 * Java field's declared type against every class under PlayWallCommon, so adding a field that
 * points at a new nested DTO is covered too.
 *
 * This is intentionally one-directional: a PlayWallCommon message/field Companion doesn't use is
 * not a failure here - only "protocol.ts still claims to speak a shape PlayWallCommon no longer
 * has" is.
 */

const JAVA_ROOT = path.resolve(
    path.dirname(fileURLToPath(import.meta.url)),
    '../../../PlayWallCommon/src/main/java',
)

/** The base envelope fields are validated once via `BASE_ENVELOPE`, not per-message. */
const ENVELOPE_FIELDS = new Set(['@class', 'messageId'])

function javaSourceFor(fqcn: string): string {
    const filePath = path.join(JAVA_ROOT, ...fqcn.split('.')) + '.java'
    if (!existsSync(filePath)) {
        throw new Error(
            `PlayWallCommon class "${fqcn}" no longer exists at ${filePath}. It was likely renamed, ` +
            `moved, or deleted in PlayWallCommon - update the matching constant/import in protocol.ts.`,
        )
    }
    return readFileSync(filePath, 'utf8')
}

/**
 * Maps every Java simple class name under PlayWallCommon (e.g. "PadDto") to its FQCN, so a
 * field's declared type (itself just a simple name in the source) can be resolved to a file.
 * Built once; throws if a name isn't unique, since that would make the mapping ambiguous.
 */
function buildJavaClassIndex(): Map<string, string> {
    const index = new Map<string, string>()
    for (const entry of readdirSync(JAVA_ROOT, {withFileTypes: true, recursive: true})) {
        if (!entry.isFile() || !entry.name.endsWith('.java')) continue

        const simpleName = entry.name.slice(0, -'.java'.length)
        const fullPath = path.join(entry.parentPath, entry.name)
        const fqcn = path.relative(JAVA_ROOT, fullPath).slice(0, -'.java'.length).split(path.sep).join('.')

        const existing = index.get(simpleName)
        if (existing && existing !== fqcn) {
            throw new Error(
                `Ambiguous Java class name "${simpleName}": found at both ${existing} and ${fqcn}. ` +
                `protocolJavaSync.test.ts's class index assumes unique simple names under PlayWallCommon.`,
            )
        }
        index.set(simpleName, fqcn)
    }
    return index
}

/** Splits a parameter/argument list on top-level commas only, ignoring commas nested inside `<...>` generics. */
function splitTopLevelParams(input: string): string[] {
    const parts: string[] = []
    let depth = 0
    let current = ''
    for (const ch of input) {
        if (ch === '<') depth++
        else if (ch === '>') depth--
        if (ch === ',' && depth === 0) {
            parts.push(current)
            current = ''
        } else {
            current += ch
        }
    }
    parts.push(current)
    return parts
}

/** Splits a Java declaration like `Map<UUID, JsonNode> mappings` into its type and its name. */
function splitTypeAndName(declaration: string): { type: string; name: string } {
    const match = /^(.*\S)\s+(\w+)$/.exec(declaration.trim())
    if (!match) {
        throw new Error(`Could not parse Java field/record-component declaration "${declaration}"`)
    }
    return {type: match[1].trim(), name: match[2]}
}

/**
 * Extracts a Java class's own field names -> declared types (Lombok-style `private Type name;`
 * declarations, or a `record`'s components). Only the class's *own* members are returned -
 * inherited fields (e.g. `BaseMessage.messageId`) are intentionally excluded, since the zod
 * schemas validate those separately via `BASE_ENVELOPE`.
 */
function javaFields(source: string): Map<string, string> {
    const recordMatch = /\brecord\s+\w+\s*\(/.exec(source)
    if (recordMatch) {
        const start = recordMatch.index + recordMatch[0].length
        let depth = 1
        let i = start
        while (depth > 0 && i < source.length) {
            if (source[i] === '(') depth++
            else if (source[i] === ')') depth--
            i++
        }
        const params = source.slice(start, i - 1).trim()
        const fields = new Map<string, string>()
        if (params.length === 0) return fields
        for (const param of splitTopLevelParams(params)) {
            const {type, name} = splitTypeAndName(param)
            fields.set(name, type)
        }
        return fields
    }

    const fields = new Map<string, string>()
    const fieldRegex = /\bprivate\s+([^;{}]+?)\s*;/g
    let match: RegExpExecArray | null
    while ((match = fieldRegex.exec(source))) {
        const {type, name} = splitTypeAndName(match[1])
        fields.set(name, type)
    }
    return fields
}

/** `List<PageDto>` -> `PageDto`, `PadDto` -> `PadDto`, `boolean`/`UUID`/`Map<K, V>` -> undefined (not a nested DTO). */
function javaTypeInnerSimpleName(type: string): string | undefined {
    const trimmed = type.trim()
    const collectionMatch = /^(?:List|Set)<\s*([\w.]+)\s*>$/.exec(trimmed)
    if (collectionMatch) return collectionMatch[1].split('.').pop()
    if (/^[A-Z][\w.]*$/.test(trimmed) && !trimmed.includes('<')) return trimmed.split('.').pop()
    return undefined
}

interface ZodObjectLike {
    shape: Record<string, unknown>
    safeParse: (value: unknown) => unknown
}

function isZodObjectSchema(value: unknown): value is ZodObjectLike {
    return (
        typeof value === 'object' &&
        value !== null &&
        typeof (value as { shape?: unknown }).shape === 'object' &&
        typeof (value as { safeParse?: unknown }).safeParse === 'function'
    )
}

/** Unwraps a zod schema through `optional`/`nullable`/`array` layers down to an object schema's shape, if any. */
function tryResolveObjectShape(schema: unknown): Record<string, unknown> | undefined {
    let current = schema as { unwrap?: () => unknown; element?: unknown; shape?: Record<string, unknown> }
    while (current && typeof current === 'object') {
        if (typeof current.unwrap === 'function') {
            current = current.unwrap() as typeof current
            continue
        }
        if (current.element) {
            current = current.element as typeof current
            continue
        }
        break
    }
    return current?.shape
}

/**
 * Recursively checks that every field a zod object schema validates still exists (by name) on
 * the matching PlayWallCommon Java class - and, for any field zod models as a nested object,
 * that the nested shape matches whatever Java class that field's declared type resolves to.
 */
function checkSchemaAgainstJava(
    zodShape: Record<string, unknown>,
    javaFqcn: string,
    javaClassIndex: Map<string, string>,
    visited = new Set<string>(),
): void {
    if (visited.has(javaFqcn)) return
    visited.add(javaFqcn)

    const javaFieldTypes = javaFields(javaSourceFor(javaFqcn))
    const zodFieldNames = Object.keys(zodShape).filter((key) => !ENVELOPE_FIELDS.has(key))

    const missing = zodFieldNames.filter((field) => !javaFieldTypes.has(field))
    expect(
        missing,
        `protocol.ts validates field(s) [${missing.join(', ')}] for ${javaFqcn}, but PlayWallCommon's ` +
        `class no longer declares them (own fields: [${[...javaFieldTypes.keys()].join(', ')}]). Update ` +
        `the zod schema in protocol.ts to match PlayWallCommon's current protocol.`,
    ).toEqual([])

    for (const fieldName of zodFieldNames) {
        const javaType = javaFieldTypes.get(fieldName)
        if (!javaType) continue // already reported above

        const nestedShape = tryResolveObjectShape(zodShape[fieldName])
        const nestedFieldNames = nestedShape ? Object.keys(nestedShape).filter((k) => !ENVELOPE_FIELDS.has(k)) : []
        if (nestedFieldNames.length === 0) continue // not a nested object in zod, or nothing in it to check

        const simpleName = javaTypeInnerSimpleName(javaType)
        const nestedFqcn = simpleName ? javaClassIndex.get(simpleName) : undefined
        if (!nestedFqcn) {
            throw new Error(
                `protocol.ts models "${fieldName}" as a nested object, but its declared Java type ` +
                `"${javaType}" on ${javaFqcn} could not be resolved to a class anywhere under ` +
                `PlayWallCommon. If this is intentionally not a 1:1 DTO, adjust the schema or this test.`,
            )
        }
        checkSchemaAgainstJava(nestedShape!, nestedFqcn, javaClassIndex, visited)
    }
}

const protocolExports: Record<string, unknown> = protocol as unknown as Record<string, unknown>
const javaClassIndex = buildJavaClassIndex()

describe('protocol.ts *_CLASS(ES) constants still resolve to real PlayWallCommon classes', () => {
    const cases: Array<readonly [string, string]> = []
    for (const [name, value] of Object.entries(protocolExports)) {
        if (name.endsWith('_CLASS') && typeof value === 'string') {
            cases.push([name, value])
        } else if (name.endsWith('_CLASSES') && Array.isArray(value)) {
            value.forEach((fqcn: unknown, index: number) => {
                if (typeof fqcn === 'string') cases.push([`${name}[${index}]`, fqcn])
            })
        }
    }

    it('found at least one *_CLASS constant to check (sanity check for the discovery itself)', () => {
        expect(cases.length).toBeGreaterThan(0)
    })

    it.each(cases)('%s -> %s', (_name, fqcn) => {
        expect(() => javaSourceFor(fqcn)).not.toThrow()
    })
})

describe('protocol.ts build*Request functions only send fields PlayWallCommon actually has', () => {
    const builders = Object.entries(protocolExports).filter(
        (entry): entry is [string, (...args: unknown[]) => Record<string, unknown>] =>
            entry[0].startsWith('build') && typeof entry[1] === 'function',
    )

    it('found at least one build* function to check (sanity check for the discovery itself)', () => {
        expect(builders.length).toBeGreaterThan(0)
    })

    it.each(builders)('%s(...)', (name, buildFn) => {
        const dummyArgs = Array.from({length: buildFn.length}, () => 'protocol-sync-test-probe')
        const built = buildFn(...dummyArgs)

        const fqcn = built['@class']
        if (typeof fqcn !== 'string') {
            throw new Error(`${name}(...) did not return an '@class' string - cannot tell which PlayWallCommon class to check it against.`)
        }

        const fields = Object.keys(built).filter((key) => !ENVELOPE_FIELDS.has(key))
        const javaFieldTypes = javaFields(javaSourceFor(fqcn))
        const missing = fields.filter((field) => !javaFieldTypes.has(field))

        expect(missing, `${name}(...) sends field(s) [${missing.join(', ')}] that ${fqcn} no longer has.`).toEqual([])
    })
})

describe('protocol.ts message schemas (and any DTOs nested in them) match PlayWallCommon', () => {
    const schemas = Object.entries(protocolExports).filter(
        (entry): entry is [string, ZodObjectLike] => isZodObjectSchema(entry[1]),
    )

    it('found at least one schema to check (sanity check for the discovery itself)', () => {
        expect(schemas.length).toBeGreaterThan(0)
    })

    it.each(schemas)('%s', (name, schema) => {
        const classConstantName = `${name}_CLASS`
        const fqcn = protocolExports[classConstantName]
        if (typeof fqcn !== 'string') {
            throw new Error(
                `protocol.ts exports a schema named "${name}" but no matching string constant ` +
                `"${classConstantName}". Add one naming the PlayWallCommon class this schema validates, ` +
                `so protocolJavaSync.test.ts can check it automatically.`,
            )
        }
        checkSchemaAgainstJava(schema.shape, fqcn, javaClassIndex)
    })
})
