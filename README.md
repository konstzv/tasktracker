# Task Tracker

A modern macOS task management application built with Compose Multiplatform Desktop and Kotlin.

## Features

- ✨ **Glassmorphism Design** - Beautiful frosted glass UI with transparency and elegant styling
- 📋 **Task Management** - Add, complete, and delete tasks with ease
- 🎯 **Menu Bar Integration** - Quick access from the macOS menu bar
- 💾 **JSON Storage** - Tasks persist locally in `~/Library/Application Support/TaskTracker/`
- 🎨 **Smooth Animations** - Polished transitions and state changes
- ⚡ **Fast & Lightweight** - Native performance with Kotlin JVM

## Task Features

Each task includes:
- **Title** (required)
- **Description** (optional)
- **Creation date** (auto-generated)
- **Status** (pending/completed)
- **State change date** (auto-updated when status changes)

## Requirements

- macOS 10.14+ (Mojave or later)
- JDK 17 or higher

## Building from Source

### Prerequisites

Make sure you have JDK 17+ installed. You can verify with:

```bash
java -version
```

### Build and Run

1. Clone or download the project
2. Open terminal in the project directory
3. Run the application:

```bash
./gradlew run
```

### Create DMG Package

To create a distributable DMG for macOS:

```bash
./gradlew packageDmg
```

The DMG will be created in `build/compose/binaries/main/dmg/`

## Usage

### Main Window

1. **Add Task**: Click the purple `+` button in the bottom-right corner
2. **Complete Task**: Click the checkbox next to any task
3. **Delete Task**: Hover over a task and click the red delete icon
4. **Close Window**: Click the close button (app stays in menu bar)

### Menu Bar

- **Double-click** the menu bar icon to open the main window
- **Right-click** (or click) to see the menu:
  - Show All Tasks
  - Quit

## Project Structure

```
tasktracker/
├── src/main/kotlin/com/tasktracker/
│   ├── Main.kt                          # Application entry point
│   ├── data/
│   │   ├── model/
│   │   │   ├── Task.kt                  # Task data model
│   │   │   ├── TaskStatus.kt            # Status enum
│   │   │   └── InstantSerializer.kt     # Date/time serialization
│   │   └── repository/
│   │       ├── TaskRepository.kt        # Task operations
│   │       └── JsonTaskStorage.kt       # JSON persistence
│   ├── ui/
│   │   ├── theme/
│   │   │   ├── Colors.kt                # Color palette
│   │   │   └── GlassmorphicStyles.kt    # Reusable glass components
│   │   ├── components/
│   │   │   ├── TaskItem.kt              # Single task card
│   │   │   ├── TaskList.kt              # Task list view
│   │   │   └── AddTaskDialog.kt         # New task dialog
│   │   ├── menubar/
│   │   │   └── MenuBarManager.kt        # Menu bar integration
│   │   └── window/
│   │       └── MainWindow.kt            # Main application window
│   └── viewmodel/
│       └── TaskViewModel.kt             # State management
```

## Technology Stack

- **Kotlin** - Modern JVM language
- **Compose Multiplatform Desktop** - Declarative UI framework
- **kotlinx.serialization** - JSON serialization
- **kotlinx.datetime** - Date/time handling
- **kotlinx.coroutines** - Async operations
- **Material 3** - Design system foundation

## Data Storage

Tasks are saved to:
```
~/Library/Application Support/TaskTracker/tasks.json
```

The file is created automatically on first use. All changes are saved immediately.

## Design

The app features a **glassmorphism** design aesthetic with:
- Translucent white backgrounds (94% opacity)
- Subtle white borders and soft shadows
- 16dp rounded corners
- Purple accent color (#6B4EFF)
- Smooth 200-300ms animations

## Keyboard Shortcuts

- `Cmd+N` - Add new task (when window is focused)
- `Cmd+Q` - Quit application
- `Cmd+W` - Hide window

## Troubleshooting

### App won't start
- Ensure JDK 17+ is installed
- Try cleaning the build: `./gradlew clean`

### Tasks not saving
- Check permissions for `~/Library/Application Support/`
- Look for error messages in the terminal

### Menu bar icon not appearing
- Restart the app
- Check System Preferences > General > Menu Bar (macOS settings)

## License

This project is open source and available for personal and commercial use.

## Future Enhancements

Planned features for future versions:
- Task categories and tags
- Due dates and reminders
- Search and filter
- Dark mode support
- Keyboard shortcuts
- Task editing
- Cloud sync
- Drag & drop reordering
