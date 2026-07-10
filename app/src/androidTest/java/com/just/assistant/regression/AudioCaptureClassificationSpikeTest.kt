package com.just.assistant.regression

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.just.assistant.ai.inference.LiteRtLmInferenceEngine
import com.just.assistant.local.model.ModelFileStore
import com.just.assistant.local.model.ModelStatusPrefs
import com.just.assistant.usecase.capture.impl.ClassifyAudioCaptureUseCaseImpl
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * GATING spike: 음성 캡처의 프로덕션 경로 전체(AudioClassificationPrompt → 엔진 → JSON 파싱)가
 * 실모델에서 동작하는지 확인. 디바이스 TTS로 일정성 문장을 합성해 분류 결과를 검증한다.
 */
@RunWith(AndroidJUnit4::class)
class AudioCaptureClassificationSpikeTest {
    @Test
    fun classifies_spoken_schedule_from_audio() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<android.content.Context>()
            val fileStore = ModelFileStore(context)
            val modelsDir = File(context.filesDir, "models")
            val modelFile = modelsDir.listFiles()?.firstOrNull { it.name.endsWith(".litertlm") }
            assumeTrue("No .litertlm model in ${modelsDir.absolutePath} — adb push 필요", modelFile != null)

            val audioBytes = synthesizeSpeechWav(context, "내일 오후 세시에 강남에서 회의")
            assumeTrue("TTS 합성 실패 — 스파이크 스킵", audioBytes != null)

            val prefs = ModelStatusPrefs(context)
            prefs.setSelectedVariant(modelFile!!.name.removeSuffix(".litertlm"))

            val engine = LiteRtLmInferenceEngine(context)
            val useCase = ClassifyAudioCaptureUseCaseImpl(engine, fileStore, prefs)
            val result =
                try {
                    useCase(audioBytes!!, caption = null)
                } finally {
                    engine.unload()
                }
            Log.i("Spike", "audio classification = $result")
            assertNotNull("오디오 분류가 null (JSON 파싱 실패 또는 추론 실패)", result)
            assertTrue("제목이 비어 있음: $result", result!!.title.isNotBlank())
        }

    /** [text]를 ko-KR TTS로 WAV 합성. TTS 미지원/실패 시 null. */
    private fun synthesizeSpeechWav(
        context: android.content.Context,
        text: String,
    ): ByteArray? {
        val initLatch = CountDownLatch(1)
        var initStatus = TextToSpeech.ERROR
        val tts =
            TextToSpeech(context) { status ->
                initStatus = status
                initLatch.countDown()
            }
        try {
            if (!initLatch.await(10, TimeUnit.SECONDS) || initStatus != TextToSpeech.SUCCESS) return null
            if (tts.setLanguage(Locale.KOREAN) < TextToSpeech.LANG_AVAILABLE) return null

            val outFile = File(context.cacheDir, "spike_capture_tts.wav")
            val doneLatch = CountDownLatch(1)
            var success = false
            tts.setOnUtteranceProgressListener(
                object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit

                    override fun onDone(utteranceId: String?) {
                        success = true
                        doneLatch.countDown()
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) = doneLatch.countDown()
                },
            )
            tts.synthesizeToFile(text, null, outFile, "spike")
            if (!doneLatch.await(20, TimeUnit.SECONDS) || !success) return null
            return outFile.readBytes().also { outFile.delete() }
        } finally {
            tts.shutdown()
        }
    }
}
