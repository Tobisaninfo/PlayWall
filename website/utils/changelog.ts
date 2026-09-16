// Vites "?raw"-Suffix inlined den Dateiinhalt zur Buildzeit als String —
// funktioniert sowohl im Server- als auch im Client-Bundle, anders als
// node:fs (das im Browser nicht existiert). CHANGELOG.md bleibt dabei
// unverändert und weiterhin inklusive Ticket-IDs die interne Quelle;
// nur die öffentliche Anzeige unten entfernt das "PW-<id> - "-Präfix.
import changelogMarkdown from '../../CHANGELOG.md?raw'

export interface ChangelogVersion {
    version: string
    features: string[]
    bugfixes: string[]
}

const TICKET_PREFIX = /^PW-\d+ - /

function stripTicketPrefix(line: string): string {
    return line.replace(TICKET_PREFIX, '')
}

function parseChangelog(markdown: string): ChangelogVersion[] {
    const versions: ChangelogVersion[] = []
    let current: ChangelogVersion | null = null
    let section: 'features' | 'bugfixes' | null = null

    for (const rawLine of markdown.split('\n')) {
        const line = rawLine.trim()

        const versionMatch = line.match(/^##\s+(.+)$/)
        const versionTitle = versionMatch?.[1]
        if (versionTitle) {
            current = {version: versionTitle.trim(), features: [], bugfixes: []}
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

export const changelogVersions: ChangelogVersion[] = parseChangelog(changelogMarkdown)
