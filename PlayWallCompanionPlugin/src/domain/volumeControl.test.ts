import {describe, expect, it} from 'vitest'
import {
    DEFAULT_VOLUME,
    computeTargetVolume,
    formatVolume,
    formatVolumePercent,
    resolveDelta,
    volumeVariableValues
} from './volumeControl.js'

describe('resolveDelta', () => {
    it('maps "5" to 0.05 and "10" to 0.1', () => {
        expect(resolveDelta('5')).toBe(0.05)
        expect(resolveDelta('10')).toBe(0.1)
    })
})

describe('computeTargetVolume', () => {
    it('increases the current volume by the given delta', () => {
        expect(computeTargetVolume('increase', 0.05, 0.5)).toBe(0.55)
    })

    it('decreases the current volume by the given delta', () => {
        expect(computeTargetVolume('decrease', 0.05, 0.5)).toBe(0.45)
    })

    it('clamps an increase at 1.0, matching GlobalVolumeActionHandler', () => {
        expect(computeTargetVolume('increase', 0.1, 0.95)).toBe(1)
        expect(computeTargetVolume('increase', 0.1, 1.0)).toBe(1)
    })

    it('clamps a decrease at 0.0', () => {
        expect(computeTargetVolume('decrease', 0.1, 0.05)).toBe(0)
        expect(computeTargetVolume('decrease', 0.1, 0)).toBe(0)
    })

    it('rounds away floating-point step noise (0.1 + 0.2 !== 0.3 in IEEE 754)', () => {
        expect(computeTargetVolume('increase', 0.2, 0.1)).toBe(0.3)
    })
})

describe('formatVolume', () => {
    it('falls back to DEFAULT_VOLUME for null/undefined', () => {
        expect(formatVolume(null)).toBe(String(DEFAULT_VOLUME))
        expect(formatVolume(undefined)).toBe(String(DEFAULT_VOLUME))
    })

    it('rounds floating-point noise from values PlayWall itself can send (e.g. its volume slider)', () => {
        expect(formatVolume(0.33000000000000007)).toBe('0.33')
    })

    it('formats an already-clean value as-is', () => {
        expect(formatVolume(0.75)).toBe('0.75')
    })

    it('keeps an explicit 0 rather than falling back to the default', () => {
        expect(formatVolume(0)).toBe('0')
    })
})

describe('formatVolumePercent', () => {
    it('falls back to 100% for null/undefined', () => {
        expect(formatVolumePercent(null)).toBe('100%')
        expect(formatVolumePercent(undefined)).toBe('100%')
    })

    it('formats a fraction as a rounded whole percentage', () => {
        expect(formatVolumePercent(0.75)).toBe('75%')
        expect(formatVolumePercent(1)).toBe('100%')
    })

    it('keeps an explicit 0 rather than falling back to the default', () => {
        expect(formatVolumePercent(0)).toBe('0%')
    })
})

describe('volumeVariableValues', () => {
    it('combines both formatted values for the same volume', () => {
        expect(volumeVariableValues(0.5)).toEqual({current_volume: '0.5', current_volume_percent: '50%'})
    })

    it('combines both fallback values when no volume is known yet', () => {
        expect(volumeVariableValues(undefined)).toEqual({current_volume: '1', current_volume_percent: '100%'})
    })
})
