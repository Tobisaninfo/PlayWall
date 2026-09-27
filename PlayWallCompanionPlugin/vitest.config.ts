import {defineConfig} from 'vitest/config'

export default defineConfig({
    test: {
        coverage: {
            provider: 'v8',
            // lcov is what Sonar's sonar.javascript.lcov.reportPaths expects; text keeps a readable
            // summary in the terminal/CI log.
            reporter: ['text', 'lcov'],
            reportsDirectory: 'target/coverage',
            // Report every source file, not just the ones touched by a test - otherwise untested files
            // simply wouldn't appear in the report instead of showing up as 0% covered.
            include: ['src/**/*.ts'],
            exclude: ['src/**/*.test.ts', 'src/domain/modernColorPalette.generated.ts'],
        },
    },
})
