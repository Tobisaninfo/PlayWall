import {describe, expect, it} from 'vitest'
import {changelogVersions} from './changelog'

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
})
