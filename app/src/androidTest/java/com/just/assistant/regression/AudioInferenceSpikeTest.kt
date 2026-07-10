package com.just.assistant.regression

import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
import java.io.File
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * GATING spike: 현재 .litertlm 모델이 audio 입력을 실제로 추론하는지 확인.
 * 디바이스 TTS로 알려진 문장을 WAV로 합성해 모델에 넣고, 응답에 그 토큰이 반영되는지 본다.
 * (모델 헤더에 tf_lite_audio_encoder_hw / tf_lite_audio_adapter 섹션 존재 확인됨 — 2026-07-09)
 */
@RunWith(AndroidJUnit4::class)
class AudioInferenceSpikeTest {
    @Test
    fun model_transcribes_speech_from_audio() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<android.content.Context>()
            val modelsDir = File(context.filesDir, "models")
            val modelFile = modelsDir.listFiles()?.firstOrNull { it.name.endsWith(".litertlm") }
            assumeTrue(
                "No .litertlm model in ${modelsDir.absolutePath} — adb push 필요",
                modelFile != null,
            )

            val audioBytes = synthesizeSpeechWav(context, "내일 오후 세시에 강남에서 회의")
            assumeTrue("TTS 합성 실패 — 오디오 스파이크 스킵", audioBytes != null)

            val engine = LiteRtLmInferenceEngine(context)
            engine.load(modelFile!!, InferenceConfig(maxTokens = 4096, preferGpu = false, enableAudio = true))
            // 주의: "들리는 말을 받아 적어줘" 같은 전사 지시는 텍스트 모드 거부("저는 오디오를
            // 들을 수 없습니다")를 확률적으로 유발한다. 정답이 고정된 내용 질문이 가장 강건하다.
            val response =
                try {
                    engine.generate(
                        prompt = "오디오를 듣고 질문에 답해. 회의 장소는 어디야? 한 단어로만 답해.",
                        images = emptyList(),
                        audios = listOf(audioBytes!!),
                    )
                } finally {
                    engine.unload()
                }

            Log.i("Spike", "audio response = $response")
            assertTrue(
                "응답이 합성한 음성 내용을 반영하지 않음 (audio 미지원 의심). response=$response",
                response.contains("강남"),
            )
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

            val outFile = File(context.cacheDir, "spike_tts.wav")
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
