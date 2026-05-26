#!/usr/bin/env bash
# Pull the ClassificationRegressionTest report from the device and print a summary.
#
# Usage: scripts/fetch-regression-report.sh [output-dir]
#   default output dir: ./regression-reports/
#
# Requires: adb in PATH, device connected, app installed, regression test ran at least once.

set -euo pipefail

OUT_DIR="${1:-./regression-reports}"
mkdir -p "$OUT_DIR"

DEVICE_PATH="files/regression/report.json"
LOCAL_PATH="$OUT_DIR/report-$(date +%Y%m%d-%H%M%S).json"

if ! adb shell run-as com.just.assistant test -f "$DEVICE_PATH"; then
    echo "ERROR: $DEVICE_PATH not present on device. Run :ai:connectedDebugAndroidTest first." >&2
    exit 1
fi

# Pull via run-as (needed for sandbox files). cat into local file.
adb shell run-as com.just.assistant cat "$DEVICE_PATH" > "$LOCAL_PATH"

echo "Saved: $LOCAL_PATH"
echo ""
echo "=== Summary ==="

# Use jq if available, otherwise fall back to grep.
if command -v jq >/dev/null 2>&1; then
    jq '{variantId, totalCases, accuracyType, accuracyDatetime, p50LatencyMs, p95LatencyMs}' "$LOCAL_PATH"
    echo ""
    echo "=== Misclassified ==="
    jq '.cases | map(select(.correctType == false)) | map({id, input, expectedType, actualType})' "$LOCAL_PATH"
else
    echo "(install jq for prettier output)"
    grep -E '"(variantId|totalCases|accuracyType|accuracyDatetime|p50|p95)"' "$LOCAL_PATH" | head -10
fi
