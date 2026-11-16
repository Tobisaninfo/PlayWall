import {release} from '~/utils/release.ts'

export default defineEventHandler(() => ({version: release.version}))
