plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    application
}

group = "com.tasktracker"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Shared module dependency
    implementation(project(":shared"))

    // Kotlinx Serialization for JSON-RPC
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Date/Time (needed for Task model from shared module)
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.14")
}

application {
    mainClass.set("com.tasktracker.mcp.MainKt")
}

kotlin {
    jvmToolchain(17)
}

// Create executable JAR with all dependencies
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.tasktracker.mcp.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
