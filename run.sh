#!/bin/bash

# Simple build script for Task Tracker
# This script helps compile and run the app manually

echo "🚀 Task Tracker Build Script"
echo "=============================="
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Error: Java is not installed"
    echo "Please install JDK 17 or higher from:"
    echo "https://www.oracle.com/java/technologies/downloads/"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Error: Java 17 or higher is required"
    echo "Current version: Java $JAVA_VERSION"
    echo "Please install JDK 17+ from:"
    echo "https://www.oracle.com/java/technologies/downloads/"
    exit 1
fi

echo "✅ Java version: $JAVA_VERSION"
echo ""

# Check for Gradle wrapper
if [ ! -f "./gradlew" ]; then
    echo "⚠️  Gradle wrapper not found"
    echo "Attempting to create wrapper..."

    if command -v gradle &> /dev/null; then
        gradle wrapper --gradle-version 8.5
        echo "✅ Gradle wrapper created"
    else
        echo "❌ Error: Gradle is not installed and wrapper is missing"
        echo ""
        echo "Please install Gradle from: https://gradle.org/install/"
        echo "Or use IntelliJ IDEA which includes Gradle"
        exit 1
    fi
fi

# Make wrapper executable
chmod +x ./gradlew

echo "📦 Building and running Task Tracker..."
echo ""

# Run the application
./gradlew run

echo ""
echo "✅ Done!"
