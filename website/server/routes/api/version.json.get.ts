import {release} from '../../../utils/release'

export default defineEventHandler(() => ({version: release.version}))
