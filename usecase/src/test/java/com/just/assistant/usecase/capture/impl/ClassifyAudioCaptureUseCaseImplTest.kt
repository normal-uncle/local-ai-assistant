package com.just.assistant.usecase.capture.impl

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.just.assistant.ai.inference.InferenceConfig
import com.just.assistant.ai.inference.InferenceEngine
import com.just.assistant.local.model.ModelFileStore
import com.just.assistant.local.model.ModelStatusPrefs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ClassifyAudioCaptureUseCaseImplTest {
    private class FakeEngine(
        private val response: String,
        var shouldThrow: Boolean = false,
    ) : InferenceEngine {
        private var loaded = false
        var lastAudioCount = -1
        var lastConfig: InferenceConfig? = null

        override suspend fun load(modelFile: File, config: InferenceConfig) {
            loaded = true
            lastConfig = config
        }

        override fun unload() {
            loaded = false
        }

        override fun isReady(): Boolean = loaded

        override suspend fun generate(prompt: String, images: List<ByteArray>): String =
            generate(prompt, images, emptyList())

        override suspend fun generate(
            prompt: String,
            images: List<ByteArray>,
            audios: List<ByteArray>,
        ): String {
            if (shouldThrow) error("inference failed")
            lastAudioCount = audios.size
            return response
        }
    }

    private lateinit var fileStore: ModelFileStore
    private lateinit var prefs: ModelStatusPrefs

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        fileStore = ModelFileStore(ctx)
        prefs = ModelStatusPrefs(ctx)
        fileStore.delete("v1")
    }

    private suspend fun setupReadyModel() {
        prefs.setSelectedVariant("v1")
        fileStore.fileFor("v1").writeText("dummy")
    }

    @Test
    fun returns_parsed_result_and_loads_with_audio_and_one_clip() =
        runTest {
            setupReadyModel()
            val raw = """{"type":"REMINDER","title":"우유 사기","datetime_iso":null}"""
            val engine = FakeEngine(raw)
            val useCase = ClassifyAudioCaptureUseCaseImpl(engine, fileStore, prefs)
            val r = useCase(byteArrayOf(1, 2, 3), "장보기")
            assertNotNull(r)
            assertEquals("우유 사기", r!!.title)
            assertEquals(1, engine.lastAudioCount)
            assertTrue("오디오 분류는 enableAudio=true 로 로드해야 함", engine.lastConfig!!.enableAudio)
            assertEquals(
                "오디오 분류는 CPU 강제 — GPU 메인 백엔드는 init은 성공하나 오디오 추론 시점에 OpenCL 예외 (SD8G1 검증)",
                false,
                engine.lastConfig!!.preferGpu,
            )
        }

    @Test
    fun returns_null_when_no_variant_selected() =
        runTest {
            val useCase = ClassifyAudioCaptureUseCaseImpl(FakeEngine("{}"), fileStore, prefs)
            assertNull(useCase(byteArrayOf(1), null))
        }

    @Test
    fun returns_null_when_model_file_missing() =
        runTest {
            prefs.setSelectedVariant("v1")
            val useCase = ClassifyAudioCaptureUseCaseImpl(FakeEngine("{}"), fileStore, prefs)
            assertNull(useCase(byteArrayOf(1), null))
        }

    @Test
    fun returns_null_when_inference_throws() =
        runTest {
            setupReadyModel()
            val useCase = ClassifyAudioCaptureUseCaseImpl(FakeEngine("{}", shouldThrow = true), fileStore, prefs)
            assertNull(useCase(byteArrayOf(1), null))
        }
}
