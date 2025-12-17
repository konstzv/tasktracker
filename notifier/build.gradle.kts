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
    // Shared module for Task models
    implementation(project(":shared"))

    // HTTP Client for Perplexity API
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JSON
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // Configuration
    implementation("com.typesafe:config:1.4.3")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.14")
}

application {
    mainClass.set("com.tasktracker.notifier.MainKt")
}

kotlin {
    jvmToolchain(17)
}

// Create fat JAR
tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.tasktracker.notifier.MainKt"
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map {
        if (it.isDirectory) it else zipTree(it)
    })
}
