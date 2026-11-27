# PlayWall

PlayWall is a JavaFX desktop client with a Spring Boot server backend for controlling interactive audio
presentations during live events. Media files are arranged as tiles ("pads") on one or more pages and can be
triggered manually, via keyboard shortcut, or via a MIDI controller, with support for fading, ramp-in, and
end-of-file warnings.

## License
This project is licensed under GPLv3.

## Features

### Pads

![pads.png](doc/screenshots/pad-grid.png)

- Start, pause, and stop playback via the pad
- Select a media file via file dialog or drag & drop
- Set a title per pad
- Set an individual color per pad
- Show playback progress as a bar on the pad
- Stop all running pads with a single action

### Playback & Volume

![pads.png](doc/screenshots/pad-grid.png)

- Adjust playback speed per track
- Set global volume for all pads
- Set volume per pad
- Loop playback of a track
- Select a sound card as output device
- Show a warning shortly before a track ends
- Set the duration of the ramp-in phase

### Project Management

![project-management.png](doc/screenshots/project-management.png)

- Create, duplicate, and delete projects
- Export and import projects
- Undo/redo project changes

### Project Settings

![project-settings.png](doc/screenshots/project-settings.png)

- Define grid layout in rows and columns
- Configure project display options
- Set a default color for pads in the project

### Page Management

![page-settings.png](doc/screenshots/page-settings.png)

- Create, rename, duplicate, and delete pages
- Reorder pages
- Set a color per page
- Import and export pages between projects

### Drag & Drop

![pad-drag.png](doc/screenshots/pad-drag.png)

- Move or duplicate pads via drag & drop
- Swap two pads via drag & drop
- Add media files via drag & drop from the file system

### Fading

![fading.png](doc/screenshots/fading.png)

- Global fading settings at the project level
- Individual fading settings at the pad level
- Fade in on start and fade out on stop

### Keyboard & MIDI Control

![fading.png](doc/screenshots/mapping.png)

- Settings area for managing keyboard and MIDI mappings
- Record a key combination instead of entering it manually
- Select a MIDI device
- Learn MIDI keys and assign them to an action
- Trigger pads, page switching, global volume, and stop-all via keyboard or MIDI
- Show the status of pads, pages, and volume via LED feedback on the MIDI controller

### Companion Plugin

- Control PlayWall using Bitfocus Companion Plugin
- Trigger pads, page switching, global volume, and stop-all

### Handling Missing Media Files

![missing-media.png](doc/screenshots/missing-media.png)

- Automatically detect missing media files
- Display a list of missing files
- Show a notice about missing media on startup

## Architecture

PlayWall consists of a JavaFX desktop client and a Spring Boot server, communicating over WebSocket and REST. Projects
are stored as files on disk (in `{appdata}/PlayWall/de.tobias.playwall.server.v8/`) — there is no database.

| Module                    | Role                                                      |
|---------------------------|-----------------------------------------------------------|
| `PlayWallCommon`          | Shared DTOs, network message types, sealed class protocol |
| `PlayWallServerCommon`    | Shared server interfaces                                  |
| `PlayWallServer`          | Spring Boot REST + WebSocket backend                      |
| `PlayWallClient`          | JavaFX desktop UI                                         |
| `PlayWallNativeAudio`     | JNI wrapper for native audio                              |
| `PlayWallNativeAudioRust` | Rust audio engine (rodio + symphonia via JNI)             |
| `PlayWallIconOverview`    | Internal developer tool for browsing available UI icons   |
| `coverage`                | JaCoCo aggregated test coverage report                    |

The client uses a custom dependency injection container (`AppContext`) instead of a framework like Spring.

Key technologies: Java 25, JavaFX, ControlsFX, Spring Boot, Lombok, MapStruct, and a Rust-based audio engine
integrated via JNI.

## System Requirements

### Linux

The Linux build ships a jlinked JavaFX runtime and a Rust audio library. The minimum glibc is the highest of three
requirements, and differs by CPU architecture:

- JavaFX's prebuilt native libraries (`libglassgtk3.so` etc.): x86_64 2.17, aarch64 2.38 (JavaFX 25.0.4).
- The bundled JDK: 2.17.
- The Rust audio library (`libPlayWallNativeAudioRust.so`): 2.34 on both architectures. Measured on the
  8.4.0-SNAPSHOT builds by reading the highest `GLIBC_x.y` symbol version it references.

| Architecture | Minimum glibc | Determined by               | Supported distros (examples)                                          |
|--------------|---------------|-----------------------------|-----------------------------------------------------------------------|
| x86_64       | 2.34          | Rust audio library          | Debian 12+, Ubuntu 22.04+, Arch-based rolling releases (e.g. CachyOS) |
| aarch64      | 2.38          | JavaFX 25.0.4 (Gluon build) | Debian 13+, Ubuntu 23.10+ / 24.04+, Arch-based rolling releases       |

All four downloads (AppImage and `tar.gz`, x86_64 and aarch64) have the same requirements per architecture. System
libraries are **not** bundled: GTK 3, Mesa/OpenGL, ALSA (plus the PipeWire ALSA plugin on PipeWire systems), X11
libraries, freetype and fontconfig must come from the host.

**AppImage (x86_64 and aarch64, any distribution):** CI also publishes
`PlayWallClient-<version>-installer.<arch>.AppImage` with `<arch>` being `x86_64` or `aarch64`. It only wraps the
jlinked app image, so the same glibc and system library requirements as above apply. Typical packages (names vary
between releases; the website's installation guide shows the same commands):

| Distribution family       | Packages                                                                                                                       |
|---------------------------|--------------------------------------------------------------------------------------------------------------------------------|
| Arch-based (e.g. CachyOS) | `gtk3 alsa-lib pipewire-alsa freetype2 fontconfig mesa libxtst`                                                                |
| Debian / Ubuntu           | `libgtk-3-0t64 libasound2t64 libfreetype6 libfontconfig1 libgl1 libxtst6 libxi6 libxrender1` (`t64` suffix on 24.04 and newer) |
| Fedora                    | `gtk3 alsa-lib freetype fontconfig mesa-libGL libXtst libXi libXrender`                                                        |

Build it locally on a Linux machine of the target architecture with
`packaging/appimage/build-appimage.sh <PlayWall-x.y.z.<arch>.tar.gz> <out.AppImage> [x86_64|aarch64]`.

**Not supported:**

- x86_64 on Debian 11 and Ubuntu 20.04 (glibc 2.31), and anything older than glibc 2.34. The audio library fails to
  load with `version 'GLIBC_2.34' not found`.
- aarch64 on Debian 12 (glibc 2.36) and Ubuntu 22.04 (glibc 2.35). The app fails to start with
  `UnsatisfiedLinkError: ... libglassgtk3.so: ... version 'GLIBC_2.38' not found`.

The aarch64 requirement comes from Gluon's aarch64 build infrastructure, not from our build config, and can change with
future JavaFX patch releases (unrelated to the JavaFX major version). The x86_64 requirement comes from the Rust
toolchain and dependencies and can change when they are updated. If this becomes a blocker, re-check both, e.g. with
`objdump -T libPlayWallNativeAudioRust.so | grep GLIBC_ | sort -V | tail`, and the required glibc of the pinned
`javafx.version`, before assuming it is fixed.

### Windows

- Minimum: **Windows 10 64-bit (version 1903+) or Windows 11** — matches Temurin JDK 25's supported baseline. No 32-bit
  builds.
- JavaFX bundles its own MSVC/UCRT runtime DLLs (`vcruntime140.dll`, `msvcp140*.dll`, `ucrtbase.dll` + the
  `api-ms-win-*` forwarder stubs) directly in the app image, so no separate "Visual C++ Redistributable" install is
  required on the target machine.

### macOS

- Minimum: **macOS 11 (Big Sur) or later**, on both Intel (x86_64) and Apple Silicon (arm64) — verified via the
  `LC_BUILD_VERSION` load command embedded in JavaFX 25.0.4's native libraries (`libglass.dylib` etc.). Unlike Linux,
  this has been stable across recent JavaFX versions and both architectures.

## Linux-Specific Handling

Besides the requirements above, a few things in PlayWall only exist because of Linux. Windows and macOS did not need
them.

### Packaging & CI

| Topic               | Linux                                                                                                                                                                                 | Windows / macOS             |
|---------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------|
| Distribution format | No native installer. jpackage builds an `APP_IMAGE`, which the `linux` profile in `PlayWallClient/pom.xml` archives to `PlayWall-<version>.<arch>.tar.gz`. A PNG is used as the icon. | `.exe` / `.dmg` installer   |
| CPU architectures   | Built on x86_64 (`amd64`) and aarch64 (`ubuntu-latest`, `ubuntu-24.04-arm`).                                                                                                          | Windows x86_64, macOS arm64 |
| Native build deps   | The Rust audio engine needs `pkg-config` and `libasound2-dev` (ALSA headers) at build time. CI installs them before the Maven build.                                                  | None                        |
| AppImage            | CI additionally wraps each architecture's app image into an AppImage (see below).                                                                                                     | n/a                         |

#### AppImage

The `appimage`, `verify-appimage` and `publish-appimage` jobs in `.github/workflows/build.yaml` build, check and publish
`PlayWallClient-<version>-installer.<arch>.AppImage` for `x86_64` and `aarch64` (Artifactory coordinates like the other
installers). The package is not tied to a distribution.

- **What is packaged:** only the self-contained jpackage app image (own JDK, JavaFX, Rust audio library). System
  libraries are *not* bundled; the host must provide them, see [System Requirements](#linux) for the package names.
- **Per architecture:** the Linux tarball of each architecture is uploaded by the `build` job and wrapped on a runner of
  the same architecture (`ubuntu-latest` for x86_64, `ubuntu-24.04-arm` for aarch64), because `appimagetool` is a
  native binary.
- **Files:** `packaging/appimage/` holds `AppRun` (entry point), `playwall.desktop` and `build-appimage.sh`. The
  script downloads `appimagetool` and the Type-2 runtime in pinned versions for the target architecture and verifies
  their SHA-256 checksums. See the usage line in [System Requirements](#linux).
- **CI verification:** `verify-appimage` extracts the AppImage on Arch Linux (x86_64, container) and Ubuntu 24.04
  (x86_64 and aarch64) with that distribution's host libraries installed. It validates the `.desktop` file and runs
  `ldd` on every bundled `.so`, which also reveals a glibc that is too old; `libjvm.so` is ignored because the java
  launcher resolves it itself at runtime. A 30 s start under Xvfb is a non-blocking smoke test (no GPU/audio in CI).
- **No FUSE needed to build:** `appimagetool` runs with `APPIMAGE_EXTRACT_AND_RUN=1`.

### Runtime

#### UI scale (`glass.gtk.uiScale`)

The *UI scale* option in the program settings is only shown on Linux. JavaFX's GTK backend only honours
`glass.gtk.uiScale` when it is a real JVM argument at process start; setting it from inside the running JVM has no
effect. macOS and Windows read the display scale natively, so they have no such option.
`LinuxUiScaleService` therefore stores the value (`ui-scale` in the config folder) and makes it reach the next start:

| Install type                 | How the value reaches the JVM                                                                                                                                                                                                             |
|------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| jpackage app image (tarball) | The `java-options=-Dglass.gtk.uiScale=<n>%` line in the launcher's `lib/app/PlayWall.cfg` is rewritten. If the file is not writable (e.g. root-owned install), a warning is logged and the scale is not applied.                          |
| AppImage                     | The `.cfg` sits on a read-only squashfs mount, so the client writes `${XDG_CONFIG_HOME:-~/.config}/PlayWall/appimage-ui-scale` instead. `AppRun` reads it and exports `JAVA_TOOL_OPTIONS=-Dglass.gtk.uiScale=<n>%` before the app starts. |

The AppImage is detected via the `APPIMAGE` environment variable. File path and format must stay in sync between
`LinuxUiScaleService` and `AppRun`. Side effect: the embedded server process inherits `JAVA_TOOL_OPTIONS`, so its
output contains a harmless `Picked up JAVA_TOOL_OPTIONS: ...` line.

#### Embedded server

`ServerLauncher` only calls `setExecutable(true)` on the bundled server JDK's `bin/java` if it is not executable
already. Inside an AppImage the installation is read-only, so `chmod` fails even though the file is already executable.

#### Audio

- The Rust audio engine uses cpal's ALSA host. On PipeWire systems (e.g. CachyOS) this works through `pipewire-alsa`.
- ALSA/PipeWire can expose stale or disconnected device nodes that report no name or default config. The device
  enumeration in `output_devices.rs` skips them with a warning instead of failing (this once caused a panic).

#### Server system tray

`PlayWallServerMain` only sets `java.awt.headless=false` on Windows and macOS. On Linux Spring Boot's default headless
mode stays active, so the server shows no system tray icon there. The macOS-only Dock handling does not apply either.

## Getting Started

### Prerequisites

- JDK 25
- Maven

### Build

```bash
mvn clean install
```

### Run

```bash
# Run the server (port 10023)
mvn spring-boot:run -pl PlayWallServer

# Run the client
mvn javafx:run -pl PlayWallClient
```

### Test

```bash
# Run tests for a specific module
mvn test -pl PlayWallClient

# Run UI tests headlessly (for CI)
mvn test -Pheadless-ui-tests

# Run audio tests (require hardware)
mvn test -Paudio-tests

# Build with a coverage report
mvn clean install -Pcode-coverage
```

### Additional build profiles

```bash
# Build the native Rust audio library
mvn install -Pbuild-rust -pl PlayWallNativeAudioRust

# Build platform installers (DMG/EXE/app image), matching the release pipeline
mvn clean install -Pbuild-rust,custom-jdk,package-executables
```

Building a complete installer requires `build-rust` and `custom-jdk` in addition to `package-executables` — running
`package-executables` on its own produces an app image missing the bundled Rust audio library and server runtime.
On macOS, signing and notarizing the installer additionally requires the `sign-dmg` and `sign-rust` profiles
together with the signing credentials described in [macOS Signing](#macos-signing).

SASS is compiled automatically during the build via the Maven dart-sass plugin (`src/main/sass/` →
`target/classes/style/`); no manual SASS compilation is needed.

## Development

### Using custom components in SceneBuilder

- Open SceneBuilder
- Click on small gears icon next to `Library`
- Select `JAR/FXML Manager`

![scenebuilder_1.jpg](doc/scenebuilder_1.jpg)

#### Add the `PlayWallClient` target folder as root folder
- Click `Add root folder wth *.class files`
- Select the following folder: `<path_to_your_working_copy/PlayWallClient/target/classes`

#### Add TheCodeLabs maven repository
- Click `Manage repositories`
- Click `Add`
- Create a new repository for `https://maven.thecodelabs.de/artifactory/TheCodeLabs-release`

![scenebuilder_2.jpg](doc/scenebuilder_2.jpg)

#### Add additional JARs as repositories
- Click `Search repositories`
- Search for the following libraries, select and add them:
  - `de.thecodelabs:libJfx`
  - `de.thecodelabs:libUtils`
  - `org.controlsfx:controlsfx`

![scenebuilder_3.jpg](doc/scenebuilder_3.jpg)

## Debugging

### Client

Start the client with the `--debug` flag to enable two additional keyboard shortcuts:

```bash
mvn javafx:run -pl PlayWallClient -Dexec.args="--debug"
```

| Shortcut (Win/Linux) | Shortcut (macOS) | Action                                                                                                                                    |
|----------------------|------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `Ctrl+Shift+F12`     | `Cmd+Shift+F12`  | Open [ScenicView](https://github.com/JonathanGiles/scenic-view), a live inspector for the JavaFX scene graph (node tree, CSS, properties) |
| `Ctrl+Shift+F11`     | `Cmd+Shift+F11`  | Copy the currently loaded project as JSON to the system clipboard                                                                         |

The "Debug" checkbox in the program settings is unrelated to the shortcuts above — it only raises the log level
to `DEBUG` on the next launch (via a flag file in the config directory), which can also be filtered live in the
in-app log viewer.

### Server

The server exposes a debug-only REST endpoint that dumps the currently loaded project:

```
GET http://localhost:10023/debug/current-project
```

It returns the server-side `Project` domain model as JSON (no authentication, no parameters). There is no
`server.servlet.context-path` configured, so the endpoint is reachable directly under the server's root, alongside
port `10023` from [Run](#run).

## MacOS Signing

### Prerequisites

1. Create a macOS code signing certificate (Developer ID Application) in Apple Developer Portal and import it into
   Keychain.
2. Create an appstore connect API key

### Local development and release signing

1. Add the following to your `.m2/settings.xml` file:

```xml
<profiles>
    <profile>
        <id>macos-sign</id>
        <properties>
            <mac.sign.identity>[YOUR APPLICATION CERTIFICATE NAME]</mac.sign.identity>
            <mac.sign.keychain>[FULL PATH TO KEYCHAIN FILE]</mac.sign.keychain>
            <mac.notary.key>[FULL PATH TO APPSTORE CONNECT API KEY]</mac.notary.key>
            <mac.notary.key.id>[APPSTORE CONNECT KEY ID]</mac.notary.key.id>
            <mac.notary.issuer>[APPSTORE CONNECT ISSUER]</mac.notary.issuer>
        </properties>
    </profile>
</profiles>

<activeProfiles>
<activeProfile>macos-sign</activeProfile>
</activeProfiles>
```

### GitHub Actions

The following secrets must be set in the GitHub repository:

* `AC_ISSUER`: Appstore Connect API Key Issuer
* `AC_KEY`: Appstore Connect API Key (base64 encoded file)
* `AC_KEY_ID`: Appstore Connect API Key ID
* `CERT_PASSWORD`: Password for `DEVELOPER_ID_P12_BASE64`
* `DEVELOPER_ID_P12_BASE64`: Developer ID Application certificate and private key (base64 encoded file)
* `MAC_SIGN_IDENTITY`: Keychain entry name (e.g. `Developer ID Application: [Name] ([XXXXXXXXX])`)
* `MAVEN_PASSWORD`: Password for deploying to maven repository
* `MAVEN_USERNAME`: Username for deploying to maven repository
* `TEAM_ID`: Apple Developer Portal Team ID
