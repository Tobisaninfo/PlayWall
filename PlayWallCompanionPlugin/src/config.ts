import {Regex, type SomeCompanionConfigField} from '@companion-module/base'

export type ModuleConfig = {
    host: string
    port: number
    useTls: boolean
    reconnectDelaySeconds: number
    pageSyncMode: 'sync' | 'async'
}

export function GetConfigFields(): SomeCompanionConfigField[] {
    return [
        {
            type: 'textinput',
            id: 'host',
            label: 'PlayWall Server IP/Hostname',
            width: 8,
            default: '127.0.0.1',
            regex: Regex.HOSTNAME,
        },
        {
            type: 'number',
            id: 'port',
            label: 'Server Port',
            width: 4,
            min: 1,
            max: 65535,
            default: 10023,
        },
        {
            type: 'checkbox',
            id: 'useTls',
            label: 'Use secure connection (wss://)',
            width: 4,
            default: false,
        },
        {
            type: 'number',
            id: 'reconnectDelaySeconds',
            label: 'Reconnect delay (seconds)',
            width: 4,
            min: 1,
            max: 60,
            default: 2,
            tooltip: 'Base delay before reconnecting after a lost connection. Backs off exponentially up to 30s.',
        },
        {
            type: 'dropdown',
            id: 'pageSyncMode',
            label: 'Page navigation mode',
            width: 8,
            choices: [
                {id: 'sync', label: 'Synchronous — mirrors the page shown in PlayWall'},
                {id: 'async', label: 'Asynchronous — independent of PlayWall'},
            ],
            default: 'sync',
            tooltip: 'Synchronous: changing the page in Companion also changes it in PlayWall, and vice versa. Asynchronous: Companion tracks its own page, unrelated to what PlayWall shows.',
        },
    ]
}
