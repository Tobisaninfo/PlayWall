import {describe, expect, it} from 'vitest'
import {changelogVersions, parseChangelog} from './changelog'

// Getestet gegen die echte CHANGELOG.md statt einer Fixture — ein Test, der
// gegen die tatsächliche Datei läuft, fängt echtes Format-Drift ab.

describe('changelogVersions', () => {
    it('parses every version block in CHANGELOG.md', () => {
        expect(changelogVersions.length).toBeGreaterThanOrEqual(4)
        expect(changelogVersions[0]?.version).toBe('8.2.0')
    })

    it('strips the "PW-<id> - " ticket prefix but keeps the rest of the line intact', () => {
        const entry = changelogVersions.find((v) => v.version === '8.2.0')
        expect(entry?.features).toContain('Seiteneinstellungen - Farbe')
        expect(entry?.features.every((f) => !f.startsWith('PW-'))).toBe(true)
    })

    it('leaves features empty for a version with only a Bugfixes section', () => {
        const entry = changelogVersions.find((v) => v.version === '8.1.1')
        expect(entry?.features).toEqual([])
        expect(entry?.bugfixes.length).toBeGreaterThan(0)
    })

    it('keeps versions in file order (newest first)', () => {
        const versions = changelogVersions.map((v) => v.version)
        expect(versions.indexOf('8.2.0')).toBeLessThan(versions.indexOf('8.1.1'))
        expect(versions.indexOf('8.1.1')).toBeLessThan(versions.indexOf('8.1.0'))
    })

    it('parses the release date from the "## <version> - <date>" heading', () => {
        const entry = changelogVersions.find((v) => v.version === '8.2.0')
        expect(entry?.date).toBe('2026-09-14')
    })

    it('has a date for every version currently in CHANGELOG.md', () => {
        expect(changelogVersions.every((v) => v.date !== null)).toBe(true)
    })

    it('still parses a version heading without a date as null (backward-compatible)', () => {
        const versions = parseChangelog('# Changelog\n\n## 9.0.0\n\n### Features\n\n- PW-1 - Test\n')
        expect(versions[0]).toEqual({version: '9.0.0', date: null, features: ['Test'], bugfixes: []})
    })
})
