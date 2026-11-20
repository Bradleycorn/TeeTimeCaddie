#!/bin/bash
#
# Runs the iOS unit tests with code coverage and prints a summary for the app target.
#
# Usage: ios/scripts/coverage.sh [destination]
#   destination  xcodebuild -destination value (default: the first available iPhone simulator)
#
# Coverage is limited to TeeTimeCaddie.app (see the TeeTimeCaddie scheme's Test action). The shared
# KMP code is not instrumented here; its coverage comes from `./gradlew koverHtmlReportCoverage`.
#
# Output, in build/reports/ios/:
#   TeeTimeCaddie.xcresult  open in Xcode for line-by-line coverage
#   coverage.txt            per-file summary

set -euo pipefail

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
if [ $# -ge 1 ]; then
  DESTINATION="$1"
else
  # Simulator names vary by machine (some include the OS version), so pick one by id.
  SIM_ID="$(xcrun simctl list devices available | grep -m1 -E '^ +iPhone' | grep -oE '[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}')" || {
    echo "No available iPhone simulator found; pass a destination." >&2
    exit 1
  }
  DESTINATION="platform=iOS Simulator,id=$SIM_ID"
fi
OUT_DIR="$ROOT/build/reports/ios"
RESULT="$OUT_DIR/TeeTimeCaddie.xcresult"

mkdir -p "$OUT_DIR"
# xcodebuild refuses to write over an existing result bundle.
rm -rf "$RESULT"

xcodebuild test \
  -project "$ROOT/ios/TeeTimeCaddie/TeeTimeCaddie.xcodeproj" \
  -scheme TeeTimeCaddie \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination "$DESTINATION" \
  -only-testing:TeeTimeCaddieTests \
  -enableCodeCoverage YES \
  -resultBundlePath "$RESULT" \
  -quiet

{
  xcrun xccov view --report --only-targets "$RESULT"
  echo
  xcrun xccov view --report --files-for-target TeeTimeCaddie.app "$RESULT"
} | tee "$OUT_DIR/coverage.txt"

echo
echo "Summary: $OUT_DIR/coverage.txt"
echo "Line-by-line: open \"$RESULT\""
