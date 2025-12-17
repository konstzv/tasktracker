# TaskTracker Notifier

Periodic macOS notifications with AI-powered task insights using Perplexity API.

## Setup

### 1. Build the JAR
```bash
./gradlew :notifier:jar
```

### 2. Set Your Perplexity API Key

Get your API key from: https://www.perplexity.ai/settings/api

Then set it as an environment variable:

**Option A: Add to your shell profile** (Recommended)
```bash
# Add to ~/.zshrc or ~/.bash_profile
echo 'export PERPLEXITY_API_KEY="your-api-key-here"' >> ~/.zshrc
source ~/.zshrc
```

**Option B: Add to the wrapper script**
```bash
# Edit notifier/run-notifier.sh and uncomment/update this line:
export PERPLEXITY_API_KEY='your-api-key-here'
```

### 3. Test Manually

First run creates the config file:
```bash
java -jar notifier/build/libs/notifier-1.0.0.jar
```

You should see a macOS notification with task insights!

### 4. Set Up Cron Job

```bash
crontab -e
```

Add one of these lines:

```bash
# Every 1 minute (for testing)
* * * * * /Users/konst/StudioProjects/tasktracker/notifier/run-notifier.sh

# Every 30 minutes (recommended)
*/30 * * * * /Users/konst/StudioProjects/tasktracker/notifier/run-notifier.sh

# Every hour
0 * * * * /Users/konst/StudioProjects/tasktracker/notifier/run-notifier.sh
```

**Important for cron:** Environment variables aren't automatically available in cron jobs. You have two options:

**Option A:** Edit the wrapper script and add the export line:
```bash
nano notifier/run-notifier.sh
# Uncomment and update: export PERPLEXITY_API_KEY='your-key'
```

**Option B:** Set the variable in crontab itself:
```bash
crontab -e
# Add this at the top:
PERPLEXITY_API_KEY=your-api-key-here
*/30 * * * * /Users/konst/StudioProjects/tasktracker/notifier/run-notifier.sh
```

### 5. Monitor Logs

```bash
tail -f ~/Library/Logs/tasktracker-notifier.log
```

## Configuration

Config file location: `~/.tasktracker/notifier.conf`

You can customize:
- `mcp.serverJarPath` - Path to MCP server JAR
- `notifications.enabled` - Enable/disable notifications
- `schedule.intervalMinutes` - Documentation only (actual interval set by cron)

## Troubleshooting

**No notification appears:**
- Check logs: `tail ~/Library/Logs/tasktracker-notifier.log`
- Verify API key is set: `echo $PERPLEXITY_API_KEY`
- Test manually: `java -jar notifier/build/libs/notifier-1.0.0.jar`

**"PERPLEXITY_API_KEY environment variable not set":**
- Make sure you've exported the variable in your current shell
- For cron, add the export to the wrapper script

**MCP server not found:**
- Build the MCP server first: `./gradlew :mcp-server:jar`
- Check the path in `~/.tasktracker/notifier.conf`

## How It Works

```
Cron Schedule → run-notifier.sh → notifier.jar
                                       ↓
                            Spawns MCP Server (subprocess)
                                       ↓
                            Gets tasks via JSON-RPC
                                       ↓
                            Sends to Perplexity API
                                       ↓
                            AI analyzes & generates insights
                                       ↓
                            Shows macOS notification
```

## Files

- `notifier-1.0.0.jar` - Executable JAR
- `run-notifier.sh` - Wrapper script for cron
- `~/.tasktracker/notifier.conf` - Configuration file
- `~/Library/Logs/tasktracker-notifier.log` - Log file
