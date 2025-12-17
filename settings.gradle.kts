pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "tasktracker"

// Subprojects
include(":app")
include(":shared")
include(":mcp-server")
include(":notifier")
