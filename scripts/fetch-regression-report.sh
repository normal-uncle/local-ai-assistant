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

SEARCH_ROOT="app/build/outputs/androidTest-results/connected/debug"

# Pick the most recent logcat file matching the regression test.
# `find -print0` + sort by mtime via stat to handle device dirs with spaces in name.
LOGCAT_FILE=$(find "$SEARCH_ROOT" -type f \
    -name "logcat-com.just.assistant.regression.ClassificationRegressionTest-*.txt" \
    -print 2>/dev/null \
    | while IFS= read -r f; do
        printf "%s\t%s\n" "$(stat -f %m "$f" 2>/dev/null || stat -c %Y "$f")" "$f"
      done \
    | sort -rn \
    | head -1 \
    | cut -f2-)

if [ -z "$LOGCAT_FILE" ]; then
    echo "ERROR: No regression test logcat file found under $SEARCH_ROOT." >&2
    echo "       Run ':app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.just.assistant.regression.ClassificationRegressionTest' first." >&2
    exit 1
fi
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
