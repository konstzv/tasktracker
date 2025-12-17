import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("org.jetbrains.compose") version "1.5.11"
}

group = "com.tasktracker"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Shared module dependency
    implementation(project(":shared"))

    // Compose Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    // Kotlinx Serialization for JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Date/Time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")

    // JNA for native macOS integration
    implementation("net.java.dev.jna:jna:5.13.0")
    implementation("net.java.dev.jna:jna-platform:5.13.0")
}

compose.desktop {
    application {
        mainClass = "com.tasktracker.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "TaskTracker"
            packageVersion = "1.0.0"

            macOS {
                bundleID = "com.tasktracker.app"
                // iconFile.set(project.file("src/main/resources/icon.icns"))
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}
