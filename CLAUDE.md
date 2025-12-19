# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Task Tracker** is a modern macOS desktop task management application built with Compose Multiplatform Desktop and Kotlin. It features a glassmorphism UI design, menu bar integration, and local JSON storage. The project is structured as a multi-module Gradle build with four modules:

1. **app** - Main desktop application (Compose Desktop UI)
2. **shared** - Data models, repository pattern, and JSON persistence
3. **mcp-server** - Model Context Protocol server for AI tool integration
4. **notifier** - Task notification service with Perplexity AI integration

## Build and Run Commands

### Development
```bash
# Run the desktop app in development mode
./run.sh
# or
./gradlew run

# Build all service modules (shared, mcp-server, notifier)
./build-all.sh

# Run individual modules
./gradlew :shared:build
./gradlew :mcp-server:jar
./gradlew :notifier:jar

# Clean build artifacts
./gradlew clean
```

### Distribution
```bash
# Create distributable DMG package for macOS
./gradlew packageDmg
# Output: build/compose/binaries/main/dmg/
```

### Running Services
```bash
# Run MCP server (requires shared module built)
java -jar mcp-server/build/libs/mcp-server-1.0.0.jar

# Run notifier (requires PERPLEXITY_API_KEY environment variable)
java -jar notifier/build/libs/notifier-1.0.0.jar
```

## Architecture

### MVVM Pattern with Repository
The app follows Model-View-ViewModel architecture:
- **Model**: `Task` data class in `shared/src/main/kotlin/com/tasktracker/data/model/Task.kt`
- **View**: Compose UI components in `app/src/main/kotlin/com/tasktracker/ui/`
- **ViewModel**: `TaskViewModel` in `app/src/main/kotlin/com/tasktracker/viewmodel/TaskViewModel.kt`
- **Repository**: `TaskRepository` in `shared/src/main/kotlin/com/tasktracker/data/repository/TaskRepository.kt`

### Reactive State Management
- Uses `StateFlow<List<Task>>` for reactive state updates
- UI automatically recomposes when tasks change
- Coroutines handle async operations (file I/O on `Dispatchers.IO`)

### Data Flow
1. UI action triggers ViewModel method
2. ViewModel launches coroutine → calls Repository
3. Repository updates StateFlow + persists via JsonTaskStorage
4. StateFlow emission triggers Compose recomposition
5. UI updates automatically

### Storage
- **Location**: `~/Library/Application Support/TaskTracker/tasks.json`
- **Format**: Pretty-printed JSON with versioning
- **Pattern**: Atomic writes using temp file + rename
- **Implementation**: `JsonTaskStorage` in `shared/src/main/kotlin/com/tasktracker/data/repository/JsonTaskStorage.kt`

## Module Responsibilities

### app
Main desktop application with Compose UI:
- Entry point: `Main.kt` - Sets up window, DI, and menu bar
- UI components: `ui/components/` - TaskItem, TaskList, AddTaskDialog
- Theme: `ui/theme/` - Colors and glassmorphic style components
- ViewModel: `TaskViewModel.kt` - State management

### shared
Shared data models and business logic:
- Models: `data/model/` - Task, TaskStatus, InstantSerializer
- Repository: `TaskRepository.kt` - Core business logic
- Storage: `JsonTaskStorage.kt` - File persistence layer

### mcp-server
Model Context Protocol server for AI integration:
- Server: `server/McpServer.kt` - MCP protocol implementation
- Tools: `protocol/McpToolDefinitions.kt` - AI tool schemas
- JSON-RPC message handling for AI communication

### notifier
Task notification service with AI:
- App: `TaskNotifierApp.kt` - Main notification logic
- MCP Client: `mcp/McpClient.kt` - Communicates with MCP server
- Perplexity: `api/PerplexityClient.kt` - AI API integration
- macOS: `platform/MacOsNotifier.kt` - Native notifications

## Key Files

| File | Purpose |
|------|---------|
| `app/src/main/kotlin/com/tasktracker/Main.kt` | Application lifecycle, window setup, DI |
| `app/src/main/kotlin/com/tasktracker/viewmodel/TaskViewModel.kt` | UI state management |
| `app/src/main/kotlin/com/tasktracker/ui/window/MainWindow.kt` | Root Compose layout |
| `app/src/main/kotlin/com/tasktracker/ui/components/TaskItem.kt` | Animated task card with glassmorphism |
| `shared/src/main/kotlin/com/tasktracker/data/model/Task.kt` | Task data model |
| `shared/src/main/kotlin/com/tasktracker/data/repository/TaskRepository.kt` | Business logic layer |
| `shared/src/main/kotlin/com/tasktracker/data/repository/JsonTaskStorage.kt` | JSON file persistence |
| `mcp-server/src/main/kotlin/com/tasktracker/mcp/server/McpServer.kt` | MCP protocol server |
| `notifier/src/main/kotlin/com/tasktracker/notifier/TaskNotifierApp.kt` | Notification service |

## Design System

### Glassmorphism Components
Reusable glassmorphic components in `ui/theme/GlassmorphicStyles.kt`:
- `GlassPanel()` - Main container with frosted glass effect
- `GlassCard()` - Card component with translucency

### Visual Properties
- Translucent white backgrounds (94% opacity)
- Subtle white borders with soft shadows
- 16dp/8dp rounded corners
- Purple accent: `#6B4EFF`
- Smooth animations (200-300ms transitions)
- LazyColumn for efficient list rendering

## Technical Notes

### Requirements
- JDK 17+ (enforced in `run.sh`)
- macOS 10.14+ (Mojave or later)
- Kotlin 1.9.21
- Compose Multiplatform Desktop 1.5.11

### Dependencies
- `kotlinx-serialization-json` (1.6.2) - JSON handling
- `kotlinx-datetime` (0.5.0) - Date/time
- `kotlinx-coroutines` (1.7.3) - Async programming
- JNA (5.13.0) - Native macOS integration for menu bar
- OkHttp3 (4.12.0) - HTTP client (notifier module)
- Material 3 - Design foundation

### Build Configuration
- Gradle Kotlin DSL
- JVM toolchain: JDK 17
- Main class: `com.tasktracker.MainKt`
- Bundle ID: `com.tasktracker.app`
- Parallel builds and build caching enabled in `gradle.properties`

### Menu Bar Integration
Menu bar functionality in `ui/menubar/MenuBarManager.kt`:
- Uses JNA platform bindings for macOS system tray
- Double-click to open main window
- Right-click menu: Show All Tasks, Quit
- Window can be hidden (Cmd+W) while app stays in menu bar

### Compose UI Patterns
- Declarative UI with immutable state
- Key-based list items for efficient recomposition
- `collectAsState()` for StateFlow observation
- Coroutine scopes tied to Composable lifecycle

### Keyboard Shortcuts
- `Cmd+N` - Add new task (when window focused)
- `Cmd+Q` - Quit application
- `Cmd+W` - Hide window (app stays in menu bar)
