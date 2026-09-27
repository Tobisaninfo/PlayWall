# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build all modules
mvn clean install

# Run the server (port 10023)
mvn spring-boot:run -pl PlayWallServer

# Run the client
mvn javafx:run -pl PlayWallClient

# Run tests for a specific module
mvn test -pl PlayWallClient

# Run UI tests headlessly (for CI)
mvn test -Pheadless-ui-tests

# Run audio tests (require hardware)
mvn test -Paudio-tests

# Build with coverage report
mvn clean install -Pcode-coverage

# Build native Rust audio library (macOS only)
mvn install -Pbuild-rust -pl PlayWallNativeAudioRust

# Build platform installers (DMG/EXE)
mvn clean install -Ppackage-executables
```

### Companion Plugin Commands

The Bitfocus Companion module lives in `PlayWallCompanionPlugin/` (Node/TypeScript, wrapped into the
Maven reactor via `frontend-maven-plugin`, but usually worked on directly with `npm`):

```bash
cd PlayWallCompanionPlugin

npm install               # first-time setup
npm run build              # sync-colors + tsc -> dist/
npm run dev                 # sync-colors + tsc --watch
npm test                    # vitest run (fast, no coverage)
npm run test:coverage       # vitest run --coverage (v8 -> lcov, for Sonar)
npm run package              # build + companion-module-build -> target/playwall.tgz
```

`mvn test -pl PlayWallCompanionPlugin` / `mvn clean install` run the equivalent npm scripts
automatically as part of the Maven lifecycle (see "Companion Plugin Architecture" below) — no need to
`cd` into the module for a full reactor build.

SASS is compiled automatically during the build via the Maven dart-sass plugin (`src/main/sass/` →
`target/classes/style/`). No manual SASS compilation is needed.

## Architecture Overview

PlayWall is a **JavaFX desktop client + Spring Boot server** application for managing interactive audio-visual
presentations (projects, pages, pads).

### Module Structure

| Module                    | Role                                                                                |
|---------------------------|-------------------------------------------------------------------------------------|
| `PlayWallCommon`          | Shared DTOs, network message types, sealed class protocol                           |
| `PlayWallServerCommon`    | Shared server interfaces                                                            |
| `PlayWallServer`          | Spring Boot REST + WebSocket backend                                                |
| `PlayWallClient`          | JavaFX 25 desktop UI                                                                |
| `PlayWallNativeAudio`     | JNI wrapper for native audio                                                        |
| `PlayWallNativeAudioRust` | Rust audio engine (rodio + symphonia via JNI)                                       |
| `PlayWallCompanionPlugin` | Bitfocus Companion module (TypeScript); connects to `PlayWallServer` over WebSocket |
| `coverage`                | JaCoCo aggregated coverage                                                          |

### Communication Protocol

Client ↔ Server communicate over **WebSocket + REST**. The `PlayWallCommon` module defines the sealed class hierarchy:

- `RequestMessage` / `ResponseMessage` — command/response pairs
- `UpdateMessage` — server-push state changes
- `ErrorMessage` — typed error responses

All messages use Jackson 3 for JSON serialization.

### Client Architecture

The client uses a **custom dependency injection container** (`AppContext`) instead of Spring. Key packages:

- `appcontext/` — IoC container; register/resolve beans here
- `domain/` — business logic for projects, pages, pads, settings
- `view/` — JavaFX FXML controllers; each controller is a bean in AppContext
- `net/` — WebSocket client, handles real-time updates from server
- `event/` — internal event bus for decoupled UI ↔ domain communication

### Server Architecture

Standard Spring Boot layering:

- `api/` — REST controllers + DTOs (organized by domain: projects, pages, pads, settings, history)
- `project/` — domain services for project/pad content management
- `net/` — WebSocket configuration and message handlers
- `config/` — Spring beans and configuration

Projects are stored as files in `{appdata}/PlayWall/de.tobias.playwall.server.v8/` — there is no database.

### Companion Plugin Architecture

`PlayWallCompanionPlugin/` is a `@companion-module/base` module: it opens its own WebSocket connection to
`PlayWallServer` (independent of the JavaFX client) and exposes actions/feedbacks/variables/presets to
Bitfocus Companion. Key files under `src/`:

- `main.ts` — the `ModuleInstance` class (`InstanceBase<ModuleSchema>`); wires the connection's callbacks
  (`onProjectLoaded`, `onPadStatus`, `onPadUpdated`, etc.) to store updates and `checkFeedbacks`/
  `setVariableValues` calls. `init`/`configUpdated`/`destroy` are the Companion lifecycle entry points.
- `connection/protocol.ts` — Zod schemas + builder functions for every request/response/broadcast in
  PlayWall's WS protocol (mirrors the sealed classes in `PlayWallCommon`). Each broadcast is matched by its
  Jackson `@class` FQCN string.
- `connection/ClientWebSocketHandler.ts` — owns the `ws` socket, request/response correlation (`messageId`), and
  dispatches incoming broadcasts to the callbacks passed in by `main.ts`. Six page-CRUD
  broadcast types are all handled the same way (refetch the whole project); pad broadcasts are more
  granular (`PadStatusUpdate` = play/stop status, `PadReplaceUpdate`/`PadSwapUpdate` = pad moved/swapped,
  `PadUpdate` = a pad's own name/colors edited via Pad Settings — these are distinct broadcasts, don't
  conflate them).
- `domain/` — pure, framework-free logic: selectors/resolvers (page/pad lookup), stores (project, playback,
  page-navigation), color helpers (`pageColor.ts`, `padColor.ts`), `projectMutations.ts` (immutable
  update/replace/swap helpers), `volumeControl.ts` (volume math incl. `roundVolume`).
- `actions.ts` / `feedbacks.ts` / `variables.ts` / `presets.ts` / `config.ts` / `ids.ts` — the Companion
  module surface, keyed by the ids in `ids.ts`.
- `domain/modernColorPalette.generated.ts` — generated by `npm run sync-colors` from PlayWall's
  `ModernColor` palette; gitignored, never hand-edit it.

**Testing conventions** (Vitest, see any `*.test.ts` file for examples):

- `InstanceBase`'s constructor only checks that `internal` looks like `{id: string, _isInstanceContext:
  true}`, so tests construct a **real** `ModuleInstance` with a minimal stub context (adding only the
  host-API methods the code path under test calls, e.g. `setVariableValues`/`checkFeedbacks`) and invoke
  its `private` handlers via an `(instance as unknown as {...})` cast rather than mocking the whole class.
- `InstanceBase.prototype.checkFeedbacks(id1, id2, ...)` wraps its args into a single array before calling
  through — assert `toHaveBeenCalledWith([id1, id2])`, not `(id1, id2)`.
- `ClientWebSocketHandler` tests mock the `ws` package with a hoisted `FakeWebSocket` double and simulate
  server broadcasts via `emit('message', json)`.

### Key Technologies

- **Java 25** with preview features and sealed classes enabled
- **Lombok** (`@Data`, `@AllArgsConstructor`, etc.) for boilerplate; **MapStruct** for DTO mapping
- **JavaFX 25** + ControlsFX; UI styling via SASS
- **Spring Boot 4** for the server
- **Rust** (rodio + symphonia) via JNI for the audio engine
- **jpackage** for cross-platform installers; macOS DMG signing/notarization via CI secrets
