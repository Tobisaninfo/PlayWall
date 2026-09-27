import {defineConfig} from 'vitest/config'

export default defineConfig({
    test: {
        // "default" keeps the readable terminal output; "junit" writes to the same
        // target/surefire-reports/ directory (and naming convention, TESTS-*.xml) the Jenkinsfile's
        // "junit testResults: '**/target/surefire-reports/*.xml'" step already scans for every Java
        // module's Surefire results, so these tests show up in the same report without any pipeline
        // changes.
        reporters: ['default', 'junit'],
        outputFile: {
            junit: 'target/surefire-reports/TESTS-vitest.xml',
        },
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
