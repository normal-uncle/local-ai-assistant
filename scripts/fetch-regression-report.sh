#!/usr/bin/env bash
# Pull the ClassificationRegressionTest report from the AGP-preserved logcat artifact.
#
# Usage: scripts/fetch-regression-report.sh [output-dir]
#   default output dir: ./regression-reports/
#
# AGP wipes the test target's data directory after connectedAndroidTest, so we can't
# pull from /data/data/.../files/regression/report.json. Instead we read from the
# logcat file AGP keeps under app/build/outputs/androidTest-results/.

set -euo pipefail

OUT_DIR="${1:-./regression-reports}"
mkdir -p "$OUT_DIR"

LOGCAT_GLOB="app/build/outputs/androidTest-results/connected/debug/*/logcat-com.just.assistant.regression.ClassificationRegressionTest-*.txt"

# Pick the most recent logcat file matching the regression test.
# shellcheck disable=SC2206
matches=( $(ls -t $LOGCAT_GLOB 2>/dev/null) )
if [ ${#matches[@]} -eq 0 ]; then
    echo "ERROR: No regression test logcat file found." >&2
    echo "       Expected: $LOGCAT_GLOB" >&2
    echo "       Run ':app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.just.assistant.regression.ClassificationRegressionTest' first." >&2
    exit 1
fi

LOGCAT_FILE="${matches[0]}"
LOCAL_PATH="$OUT_DIR/report-$(date +%Y%m%d-%H%M%S).json"

# Extract chunked base64 between [RegressionReportBegin] and [RegressionReportEnd].
# Each chunk line: "[RegressionReportChunk] <index> <base64>"
# Concatenate in index order (lines already arrive in order; sort defensively).
chunks=$(grep -oE '\[RegressionReportChunk\] [0-9]+ [A-Za-z0-9+/=]+' "$LOGCAT_FILE" \
    | sort -k2 -n \
    | awk '{print $3}')

if [ -z "$chunks" ]; then
    echo "ERROR: No [RegressionReportChunk] lines found in $LOGCAT_FILE." >&2
    echo "       The test may have been skipped (no model) or failed before report write." >&2
    exit 1
fi

# Decode concatenated base64.
echo "$chunks" | tr -d '\n' | base64 --decode > "$LOCAL_PATH"

echo "Saved: $LOCAL_PATH (source: $LOGCAT_FILE)"
echo ""
echo "=== Summary ==="

if command -v jq >/dev/null 2>&1; then
    jq '{variantId, totalCases, accuracyType, accuracyDatetime, p50LatencyMs, p95LatencyMs}' "$LOCAL_PATH"
    echo ""
    echo "=== Misclassified ==="
    jq '.cases | map(select(.correctType == false)) | map({id, input, expectedType, actualType})' "$LOCAL_PATH"
else
    echo "(install jq for prettier output)"
    grep -E '"(variantId|totalCases|accuracyType|accuracyDatetime|p50|p95)"' "$LOCAL_PATH" | head -10
fi
