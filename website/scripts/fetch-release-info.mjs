#!/usr/bin/env node
import {mkdir, writeFile} from 'node:fs/promises'
import {fileURLToPath} from 'node:url'

// TEMP: set to a snapshot version (e.g. '8.4.0-SNAPSHOT') to fetch download links from the snapshots repository
// instead of the latest release. Set back to null for the regular release flow.
const TEST_SNAPSHOT_VERSION = null

const REPOSITORY_BASE = 'https://maven.thecodelabs.de/artifactory'
const RELEASE_REPOSITORY = `${REPOSITORY_BASE}/TheCodeLabs-release/de/tobias/playwall`
const SNAPSHOT_REPOSITORY = `${REPOSITORY_BASE}/TheCodeLabs-snapshots/de/tobias/playwall`

const isSnapshot = TEST_SNAPSHOT_VERSION !== null
const REPOSITORY = isSnapshot ? SNAPSHOT_REPOSITORY : RELEASE_REPOSITORY

const CLIENT_BASE = `${REPOSITORY}/PlayWallClient`
const COMPANION_BASE = `${REPOSITORY}/PlayWallCompanionPlugin`
// Proxy locations in nginx.conf (snapshots have their own location, they are not in the release repository)
const CLIENT_PROXY_BASE = isSnapshot ? '/downloads-snapshots' : '/downloads'
const COMPANION_PROXY_BASE = isSnapshot ? '/downloads-companion-snapshots' : '/downloads-companion'

const PLATFORMS = {
    'windows-amd64': {classifier: 'installer', extension: 'exe'},
    'macos-arm64': {classifier: 'installer', extension: 'dmg'},
    'linux-amd64': {classifier: 'installer', extension: 'amd64.tar.gz'},
    'linux-arm64': {classifier: 'installer', extension: 'aarch64.tar.gz'},
}

// Not available for older releases: hidden on the download page instead of failing the build.
const OPTIONAL_PLATFORMS = {
    'linux-amd64-appimage': {classifier: 'installer', extension: 'x86_64.AppImage'},
    'linux-arm64-appimage': {classifier: 'installer', extension: 'aarch64.AppImage'},
}

function fail(message) {
    console.error(`\n✖ fetch-release-info: ${message}\n`)
    process.exit(1)
}

async function fetchText(url) {
    const response = await fetch(url)
    if (!response.ok) {
        fail(`GET ${url} -> ${response.status} ${response.statusText}`)
    }
    return response.text()
}

async function isReachable(url) {
    const response = await fetch(url, {method: 'HEAD'})
    return response.ok
}

async function assertReachable(url) {
    const response = await fetch(url, {method: 'HEAD'})
    if (!response.ok) {
        fail(`Download-Artefakt nicht erreichbar: ${url} -> ${response.status} ${response.statusText}`)
    }
}

/**
 * Snapshot files carry a timestamp in their name (PlayWallClient-8.4.0-20261127.193920-50-installer.dmg), which is only
 * listed in the version's maven-metadata.xml. Returns the file name for the artifact, or null if it is not listed.
 */
function resolveSnapshotFileName(metadataXml, artifactId, classifier, extension) {
    const entries = [...metadataXml.matchAll(/<snapshotVersion>([\s\S]*?)<\/snapshotVersion>/g)].map((m) => m[1])
    for (const entry of entries) {
        const entryClassifier = entry.match(/<classifier>([^<]+)<\/classifier>/)?.[1]?.trim() ?? null
        const entryExtension = entry.match(/<extension>([^<]+)<\/extension>/)?.[1]?.trim()
        const value = entry.match(/<value>([^<]+)<\/value>/)?.[1]?.trim()
        if (entryExtension === extension && entryClassifier === (classifier ?? null) && value) {
            return `${artifactId}-${value}${classifier ? `-${classifier}` : ''}.${extension}`
        }
    }
    return null
}

/**
 * Returns the path of the artifact below the artifact's repository folder (e.g. "8.4.0/PlayWallClient-8.4.0-installer.exe"),
 * or null if it does not exist (snapshots only; releases are verified via HEAD request by the callers).
 */
function artifactPath({version, artifactId, classifier, extension, snapshotMetadata}) {
    if (!isSnapshot) {
        return `${version}/${artifactId}-${version}${classifier ? `-${classifier}` : ''}.${extension}`
    }
    const fileName = resolveSnapshotFileName(snapshotMetadata, artifactId, classifier, extension)
    return fileName ? `${version}/${fileName}` : null
}

async function resolveVersion() {
    if (isSnapshot) {
        console.log(`TEST: verwende Snapshot ${TEST_SNAPSHOT_VERSION} statt dem aktuellen Release`)
        return TEST_SNAPSHOT_VERSION
    }

    const metadataUrl = `${CLIENT_BASE}/maven-metadata.xml`
    console.log(`Lese ${metadataUrl} ...`)
    const metadataXml = await fetchText(metadataUrl)
    const releaseMatch = metadataXml.match(/<release>([^<]+)<\/release>/)
    if (!releaseMatch) {
        fail(`Konnte <release> nicht aus maven-metadata.xml lesen:\n${metadataXml}`)
    }
    return releaseMatch[1].trim()
}

async function loadSnapshotMetadata(base, version) {
    if (!isSnapshot) {
        return null
    }
    const url = `${base}/${version}/maven-metadata.xml`
    console.log(`Lese ${url} ...`)
    return fetchText(url)
}

async function resolveCompanionUrl(version) {
    let metadata = null
    if (isSnapshot) {
        const response = await fetch(`${COMPANION_BASE}/${version}/maven-metadata.xml`)
        if (!response.ok) {
            console.warn(`Companion Plugin für ${version} nicht gefunden (${response.status}), Download wird ausgeblendet`)
            return null
        }
        metadata = await response.text()
    }

    const path = artifactPath({
        version,
        artifactId: 'PlayWallCompanionPlugin',
        classifier: null,
        extension: 'tgz',
        snapshotMetadata: metadata,
    })
    if (!path || !(await isReachable(`${COMPANION_BASE}/${path}`))) {
        console.warn(`Companion Plugin für ${version} nicht gefunden, Download wird ausgeblendet`)
        return null
    }
    return `${COMPANION_PROXY_BASE}/${path}`
}

async function main() {
    const version = await resolveVersion()
    console.log(`Verwendete Version: ${version}`)

    const snapshotMetadata = await loadSnapshotMetadata(CLIENT_BASE, version)

    const platforms = {}
    for (const [platformId, {classifier, extension}] of Object.entries(PLATFORMS)) {
        const path = artifactPath({version, artifactId: 'PlayWallClient', classifier, extension, snapshotMetadata})
        if (!path) {
            fail(`${platformId}: kein Eintrag für ${classifier}.${extension} in der maven-metadata.xml von ${version}`)
        }
        const url = `${CLIENT_BASE}/${path}`
        console.log(`Prüfe ${platformId}: ${url}`)
        await assertReachable(url)
        platforms[platformId] = `${CLIENT_PROXY_BASE}/${path}`
    }

    for (const [platformId, {classifier, extension}] of Object.entries(OPTIONAL_PLATFORMS)) {
        const path = artifactPath({version, artifactId: 'PlayWallClient', classifier, extension, snapshotMetadata})
        const url = path ? `${CLIENT_BASE}/${path}` : null
        console.log(`Prüfe optional ${platformId}: ${url ?? '(nicht in maven-metadata.xml)'}`)
        if (url && (await isReachable(url))) {
            platforms[platformId] = `${CLIENT_PROXY_BASE}/${path}`
        } else {
            console.warn(`${platformId} für ${version} nicht gefunden, Download wird ausgeblendet`)
        }
    }

    const companion = await resolveCompanionUrl(version)

    const outFile = fileURLToPath(new URL('../assets/release.json', import.meta.url))
    await mkdir(new URL('../assets/', import.meta.url), {recursive: true})
    await writeFile(outFile, `${JSON.stringify({version, platforms, companion}, null, 2)}\n`)

    console.log(`\n✔ release.json geschrieben: ${outFile}`)
}

main().catch((error) => fail(error instanceof Error ? error.stack : String(error)))
