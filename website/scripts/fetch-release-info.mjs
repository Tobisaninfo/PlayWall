#!/usr/bin/env node
import {mkdir, writeFile} from 'node:fs/promises'
import {fileURLToPath} from 'node:url'

const ARTIFACT_BASE = 'https://maven.thecodelabs.de/artifactory/TheCodeLabs-release/de/tobias/playwall/PlayWallClient'
const METADATA_URL = `${ARTIFACT_BASE}/maven-metadata.xml`
const DOWNLOAD_PROXY_BASE = '/downloads'

const PLATFORM_SUFFIXES = {
    'windows-amd64': 'installer.exe',
    'macos-arm64': 'installer.dmg',
    'linux-amd64': 'installer.amd64.tar.gz',
    'linux-arm64': 'installer.aarch64.tar.gz',
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

async function assertReachable(url) {
    const response = await fetch(url, {method: 'HEAD'})
    if (!response.ok) {
        fail(`Download-Artefakt nicht erreichbar: ${url} -> ${response.status} ${response.statusText}`)
    }
}

async function main() {
    console.log(`Lese ${METADATA_URL} ...`)
    const metadataXml = await fetchText(METADATA_URL)

    const releaseMatch = metadataXml.match(/<release>([^<]+)<\/release>/)
    if (!releaseMatch) {
        fail(`Konnte <release> nicht aus maven-metadata.xml lesen:\n${metadataXml}`)
    }
    const version = releaseMatch[1].trim()
    console.log(`Aktuelles Release: ${version}`)

    const platforms = {}
    for (const [platformId, suffix] of Object.entries(PLATFORM_SUFFIXES)) {
        const artifactPath = `${version}/PlayWallClient-${version}-${suffix}`
        const url = `${ARTIFACT_BASE}/${artifactPath}`
        console.log(`Prüfe ${platformId}: ${url}`)
        await assertReachable(url)
        platforms[platformId] = `${DOWNLOAD_PROXY_BASE}/${artifactPath}`
    }

    const outFile = fileURLToPath(new URL('../assets/release.json', import.meta.url))
    await mkdir(new URL('../assets/', import.meta.url), {recursive: true})
    await writeFile(outFile, `${JSON.stringify({version, platforms}, null, 2)}\n`)

    console.log(`\n✔ release.json geschrieben: ${outFile}`)
}

main().catch((error) => fail(error instanceof Error ? error.stack : String(error)))
