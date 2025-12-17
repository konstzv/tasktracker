#!/bin/bash

# TaskTracker Notifier Wrapper Script
# Run this script via cron to get periodic task notifications

# Make sure to export PERPLEXITY_API_KEY in your environment or add it here:
# export PERPLEXITY_API_KEY='your-api-key-here'

cd /Users/konst/StudioProjects/tasktracker
/usr/bin/java -jar notifier/build/libs/notifier-1.0.0.jar
