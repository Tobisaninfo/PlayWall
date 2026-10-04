import changelogMarkdownEn from '../../CHANGELOG.md?raw'
import changelogMarkdownDe from '../../CHANGELOG_de.md?raw'

export interface ChangelogVersion {
    version: string
    date: string | null
    features: string[]
    bugfixes: string[]
}

const TICKET_PREFIX = /^PW-\d+ - /

const VERSION_HEADING = /^##\s+(\S+)(?:\s*-\s*(\d{4}-\d{2}-\d{2}))?\s*$/

function stripTicketPrefix(line: string): string {
    return line.replace(TICKET_PREFIX, '')
}

export function parseChangelog(markdown: string): ChangelogVersion[] {
    const versions: ChangelogVersion[] = []
    let current: ChangelogVersion | null = null
    let section: 'features' | 'bugfixes' | null = null

    for (const rawLine of markdown.split('\n')) {
        const line = rawLine.trim()

        const versionMatch = line.match(VERSION_HEADING)
        if (versionMatch?.[1]) {
            current = {version: versionMatch[1], date: versionMatch[2] ?? null, features: [], bugfixes: []}
            versions.push(current)
            section = null
            continue
        }

        const sectionMatch = line.match(/^###\s+(Features|Bugfixes)\s*$/i)
        const sectionName = sectionMatch?.[1]
        if (sectionName) {
            section = sectionName.toLowerCase() as 'features' | 'bugfixes'
            continue
        }

        const itemMatch = line.match(/^-\s+(.+)$/)
        const itemText = itemMatch?.[1]
        if (itemText && current && section) {
            current[section].push(stripTicketPrefix(itemText.trim()))
        }
    }

    return versions
}

export const changelogVersions: ChangelogVersion[] = parseChangelog(changelogMarkdownEn)

export const changelogVersionsDe: ChangelogVersion[] = parseChangelog(changelogMarkdownDe)

export function getChangelogVersions(locale: string): ChangelogVersion[] {
    return locale === 'de' ? changelogVersionsDe : changelogVersions
}
