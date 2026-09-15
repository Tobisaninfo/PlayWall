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

SASS is compiled automatically during the build via the Maven dart-sass plugin (`src/main/sass/` →
`target/classes/style/`). No manual SASS compilation is needed.

## Architecture Overview

PlayWall is a **JavaFX desktop client + Spring Boot server** application for managing interactive audio-visual
presentations (projects, pages, pads).

### Module Structure

| Module                    | Role                                                      |
|---------------------------|-----------------------------------------------------------|
| `PlayWallCommon`          | Shared DTOs, network message types, sealed class protocol |
| `PlayWallServerCommon`    | Shared server interfaces                                  |
| `PlayWallServer`          | Spring Boot REST + WebSocket backend                      |
| `PlayWallClient`          | JavaFX 25 desktop UI                                      |
| `PlayWallNativeAudio`     | JNI wrapper for native audio                              |
| `PlayWallNativeAudioRust` | Rust audio engine (rodio + symphonia via JNI)             |
| `coverage`                | JaCoCo aggregated coverage                                |

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

### Key Technologies

- **Java 25** with preview features and sealed classes enabled
- **Lombok** (`@Data`, `@AllArgsConstructor`, etc.) for boilerplate; **MapStruct** for DTO mapping
- **JavaFX 25** + ControlsFX; UI styling via SASS
- **Spring Boot 4** for the server
- **Rust** (rodio + symphonia) via JNI for the audio engine
- **jpackage** for cross-platform installers; macOS DMG signing/notarization via CI secrets
