# Changelog

## 8.2.0 - 2026-09-14

### Features

- PW-109 - Page settings - color
- PW-131 - Select sound card
- PW-132 - Fading
- PW-134 - Pad drag & drop
- PW-146 - Keyboard mapping
- PW-147 - MIDI mapping
- PW-158 - Extended audio playback control methods
- PW-160 - Adjust playback speed
- PW-161 - EoF warning animation should not be shown when looping
- PW-166 - Autosave
- PW-168 - New loop icon
- PW-173 - EoF animation should fade smoothly instead of blinking hard
- PW-174 - Replace media - allow selecting an entire folder
- PW-175 - Replace media - automatically close toast when no missing media remain
- PW-176 - Style launch dialog
- PW-182 - Linux build

### Bugfixes

- PW-148 - Toasts are misplaced after resizing the main window
- PW-163 - Log viewer cannot be opened in the packaged version
- PW-164 - Stop fade-out is also executed while paused
- PW-165 - Stop storing the project name in multiple places
- PW-167 - Make debug logging enableable in the packaged version
- PW-177 - Pad is not marked with an error when the linked file is deleted
- PW-179 - Style connection error dialog
- PW-199 - Mapping tab list does not refresh
- PW-202 - Scroll view behavior of mapping view

## 8.1.1 - 2026-05-28

### Bugfixes

- PW-129 - Pad play color does not work after saving pad settings
- PW-130 - Missing media file: show folder button

## 8.1.0 - 2026-05-25

### Features

- PW-77 - Ramp-in / intro
- PW-78 - End-of-file warning
- PW-79 - Add media files via drag & drop
- PW-80 - Project management
- PW-96 - Application settings - basics
- PW-99 - Sign macOS application
- PW-100 - Handling of missing media files
- PW-110 - Page import/export from other projects
- PW-119 - Remote logging server and central view
- PW-121 - Integrate Scenic View
- PW-123 - Revise styling
- PW-125 - Search in project management

### Bugfixes

- PW-103 - Alerts: Text in the header of default dialogs is not German
- PW-105 - Server system tray: Port number of connected clients is displayed with a thousands separator
- PW-106 - With too many pages, the menu bar is no longer reachable
- PW-108 - Project settings: Number of pads per row/column - no text is shown on undo/redo
- PW-111 - Arrange pages - drop zone flickers
- PW-112 - Pad progress display: "elapsed / total time" does not fit the width
- PW-114 - Windows: NativeAudio DLL is locked when the server is quit via the system tray
- PW-116 - Saving pad settings bypasses the global volume
- PW-120 - Undo operations possible from a closed project
- PW-128 - Long title pushes out progress bar

## 8.0.0 - 2026-02-16

### Features

- PW-7 - Pad basics
- PW-9 - Pad settings - basics
- PW-12 - Create "New project"
- PW-13 - Playback progress
- PW-18 - Project settings basics
- PW-52 - Undo / Redo
- PW-53 - Application volume
- PW-54 - Page management
- PW-60 - Pad colors

### Bugfixes

- PW-17 - RAM fills up during audio playback
- PW-33 - Text is cut off in dialogs
- PW-45 - Quitting the client does not stop audio playback
- PW-46 - Loop only works when the pad is played again
- PW-51 - Error messages from the server are no longer shown in the client
- PW-64 - "Pad time" remains after deletion
- PW-71 - PadNewMedia undo: Pad title is not restored
