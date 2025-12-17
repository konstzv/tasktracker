plugins {
    kotlin("jvm") version "1.9.21" apply false
    kotlin("plugin.serialization") version "1.9.21" apply false
    id("org.jetbrains.compose") version "1.5.11" apply false
}

allprojects {
    group = "com.tasktracker"
    version = "1.0.0"

    repositories {
        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
