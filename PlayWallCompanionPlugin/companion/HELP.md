# PlayWall

Connects to a [PlayWall](https://github.com/Tobisaninfo/PlayWall) server over its WebSocket API.

## Setup

1. Enter the IP address/hostname and port of the machine running the PlayWall server (default port: `10023`).
2. The connection reconnects automatically if it is lost; the base reconnect delay is configurable and increases with
   backoff on repeated failures.

The connection status is exposed as the `connection_status` variable.
