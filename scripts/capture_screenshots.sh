#!/usr/bin/env bash
set -e

# ==============================================================================
# AKSA ANDROID SCREENSHOT CAPTURE SCRIPT FOR FIGMA HANDOFF
# ==============================================================================

OUTPUT_DIR="artifacts/figma-screenshots"
mkdir -p "$OUTPUT_DIR"

echo "=================================================="
echo "AKSA SCREENSHOT CAPTURE UTILITY"
echo "Target Directory: $OUTPUT_DIR"
echo "=================================================="

# Check ADB
if ! command -v adb &> /dev/null; then
    echo "❌ Error: ADB is not installed or not in PATH."
    exit 1
fi

DEVICES=$(adb devices | grep -v "List" | grep "device$" || true)
if [ -z "$DEVICES" ]; then
    echo "❌ No authorized Android device connected via ADB."
    echo "Please connect your phone, enable USB Debugging, and re-run this script."
    exit 1
fi

echo "✅ Detected Android device:"
adb devices -l

# Check APK
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "📦 Building debug APK first..."
    ./gradlew assembleDebug
fi

echo "📲 Installing latest debug APK to device..."
adb install -r "$APK_PATH"

echo "🚀 Launching AKSA..."
adb shell am force-stop com.aksa.app
adb shell am start -n com.aksa.app/.MainActivity

echo ""
echo "Capture Instructions:"
echo "1. On the device, navigate to the desired screen or use the '🛠️ Mode Developer Preview'."
echo "2. Select an option below to capture and save the screenshot with the standardized name."
echo ""

capture_screen() {
    local filename="$1"
    local desc="$2"
    echo "📸 Capturing: $desc -> $OUTPUT_DIR/$filename"
    adb exec-out screencap -p > "$OUTPUT_DIR/$filename"
    echo "✅ Saved $OUTPUT_DIR/$filename ($(wc -c < "$OUTPUT_DIR/$filename") bytes)"
}

if [ "$1" == "--all" ]; then
    echo "Auto-capture mode triggered..."
    sleep 3
    capture_screen "01-splash-screen.png" "Splash Screen"
    exit 0
fi

echo "Ready for captures. Run with screenshot name or capture current screen:"
echo "Usage: $0 [filename.png] [description]"
if [ -n "$1" ]; then
    capture_screen "$1" "${2:-Manual Capture}"
else
    echo "To capture the current device screen now, specify filename:"
    echo "Example: ./scripts/capture_screenshots.sh 05-home-default.png 'Home Default'"
fi
