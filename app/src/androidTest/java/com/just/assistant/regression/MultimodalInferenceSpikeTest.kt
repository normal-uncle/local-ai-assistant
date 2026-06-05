package com.just.assistant.regression

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.just.assistant.ai.inference.InferenceConfig
import com.just.assistant.ai.inference.LiteRtLmInferenceEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream
import java.io.File

/**
 * GATING spike: 현재 .litertlm 모델이 vision 입력을 실제로 추론하는지 확인.
 * 테스트 안에서 알려진 텍스트를 그린 비트맵을 모델에 넣고, 응답에 그 텍스트 토큰이 반영되는지 본다.
 */
@RunWith(AndroidJUnit4::class)
class MultimodalInferenceSpikeTest {
    @Test
    fun model_reads_text_from_image() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<android.content.Context>()
            val modelsDir = File(context.filesDir, "models")
            val modelFile = modelsDir.listFiles()?.firstOrNull { it.name.endsWith(".litertlm") }
            assumeTrue(
                "No .litertlm model in ${modelsDir.absolutePath} — adb push 필요",
                modelFile != null,
            )

            val imageBytes = renderTextImage(listOf("2026-06-10", "15:00", "회의", "강남"))

            val engine = LiteRtLmInferenceEngine(context)
            engine.load(modelFile!!, InferenceConfig(maxTokens = 4096, preferGpu = false, enableVision = true))
            val response =
                try {
                    engine.generate(
                        prompt = "이 이미지에 적힌 글자를 그대로 읽어서 적어주세요.",
                        images = listOf(imageBytes),
                    )
                } finally {
                    engine.unload()
                }

            Log.i("Spike", "multimodal response = $response")
            val tokens = listOf("2026", "15:00", "15", "회의", "강남")
            val hit = tokens.any { response.contains(it) }
            assertTrue(
                "응답이 그린 텍스트를 전혀 반영하지 않음 (vision 미지원 의심). response=$response",
                response.isNotEmpty() && hit,
            )
        }

    private fun renderTextImage(lines: List<String>): ByteArray {
        val size = 768
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val paint =
            Paint().apply {
                color = Color.BLACK
                textSize = 64f
                isAntiAlias = true
            }
        var y = 140f
        for (line in lines) {
            canvas.drawText(line, 60f, y, paint)
            y += 110f
        }
        return ByteArrayOutputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            bitmap.recycle()
            out.toByteArray()
        }
    }
}
