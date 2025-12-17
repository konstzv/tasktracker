#!/bin/bash

echo "Killing Gradle daemons..."
pkill -f gradle
sleep 2

echo "Building shared module..."
./gradlew :shared:build --no-daemon || exit 1

echo "Building MCP server..."
./gradlew :mcp-server:jar --no-daemon || exit 1

echo "Building notifier..."
./gradlew :notifier:jar --no-daemon || exit 1

echo ""
echo "✅ Build complete!"
echo ""
echo "MCP Server JAR: mcp-server/build/libs/mcp-server-1.0.0.jar"
echo "Notifier JAR: notifier/build/libs/notifier-1.0.0.jar"
echo ""
echo "Next steps:"
echo "1. export PERPLEXITY_API_KEY='your-key-here'"
echo "2. java -jar notifier/build/libs/notifier-1.0.0.jar"
