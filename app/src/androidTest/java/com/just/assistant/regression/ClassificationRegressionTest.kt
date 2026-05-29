package com.just.assistant.regression

import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.just.assistant.ai.golden.GoldenCaseLoader
import com.just.assistant.ai.inference.InferenceConfig
import com.just.assistant.ai.inference.LiteRtLmInferenceEngine
import com.just.assistant.ai.prompt.ClassificationPrompt
import com.just.assistant.ai.prompt.ClassificationResult
import com.just.assistant.repository.model.NoteType
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ClassificationRegressionTest {
    @Serializable
    private data class CaseResult(
        val id: String,
        val input: String,
        val expectedType: NoteType,
        val actualType: NoteType?,
        val hasDatetime: Boolean,
        val latencyMs: Long,
        val correctType: Boolean,
    )

    @Serializable
    private data class Report(
        val variantId: String,
        val totalCases: Int,
        val correctType: Int,
        val accuracyType: Double,
        val correctDatetime: Int,
        val accuracyDatetime: Double,
        val p50LatencyMs: Long,
        val p95LatencyMs: Long,
        val cases: List<CaseResult>,
    )

    @Test
    fun regression_runs_all_golden_cases_and_writes_report() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<android.content.Context>()

            val modelsDir = File(context.filesDir, "models")
            val modelFile = modelsDir.listFiles()?.firstOrNull { it.name.endsWith(".litertlm") }
            assumeTrue(
                "No .litertlm model found in ${modelsDir.absolutePath}. Run onboarding first.",
                modelFile != null,
            )
            val variantId = modelFile!!.nameWithoutExtension

            val engine = LiteRtLmInferenceEngine(context)
            engine.load(modelFile, InferenceConfig())

            val loader = GoldenCaseLoader(context)
            val cases = loader.load()

            val results = mutableListOf<CaseResult>()
            for (case in cases) {
                val start = System.currentTimeMillis()
                val raw =
                    try {
                        engine.generate(ClassificationPrompt.build(case.input))
                    } catch (t: Throwable) {
                        ""
                    }
                val parsed = ClassificationResult.parse(raw)
                val latency = System.currentTimeMillis() - start

                results +=
                    CaseResult(
                        id = case.id,
                        input = case.input,
                        expectedType = case.expectedType,
                        actualType = parsed?.type,
                        hasDatetime = parsed?.datetimeIso != null,
                        latencyMs = latency,
                        correctType = parsed?.type == case.expectedType,
                    )
            }
            engine.unload()

            val correctType = results.count { it.correctType }
            val correctDatetime =
                results.count { it.expectedType == NoteType.EVENT && it.hasDatetime } +
                    results.count { it.expectedType != NoteType.EVENT && !it.hasDatetime }
            val latencies = results.map { it.latencyMs }.sorted()
            val p50 = latencies[(latencies.size * 0.5).toInt().coerceAtMost(latencies.size - 1)]
            val p95 = latencies[(latencies.size * 0.95).toInt().coerceAtMost(latencies.size - 1)]

            val report =
                Report(
                    variantId = variantId,
                    totalCases = results.size,
                    correctType = correctType,
                    accuracyType = correctType.toDouble() / results.size,
                    correctDatetime = correctDatetime,
                    accuracyDatetime = correctDatetime.toDouble() / results.size,
                    p50LatencyMs = p50,
                    p95LatencyMs = p95,
                    cases = results,
                )

            // Still write the file (best-effort — survives only if AGP doesn't uninstall).
            val outDir = File(context.filesDir, "regression").apply { mkdirs() }
            val outFile = File(outDir, "report.json")
            val jsonText = Json { prettyPrint = false }.encodeToString(report)
            outFile.writeText(jsonText)

            // Dump JSON to logcat in base64-encoded ~2KB chunks bracketed by markers.
            // Script extracts on host from the AGP-preserved logcat file.
            val b64 = Base64.encodeToString(jsonText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            val chunkSize = 2000
            val chunks = b64.chunked(chunkSize)
            println("[RegressionReportBegin] chunks=${chunks.size}")
            chunks.forEachIndexed { idx, chunk ->
                println("[RegressionReportChunk] $idx $chunk")
            }
            println("[RegressionReportEnd]")
            println(
                "[Regression] variant=$variantId accuracy=${"%.3f".format(report.accuracyType)} p50=${p50}ms p95=${p95}ms",
            )
        }
}
