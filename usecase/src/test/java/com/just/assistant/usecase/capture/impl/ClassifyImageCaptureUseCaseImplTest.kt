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
class ClassifyImageCaptureUseCaseImplTest {
    private class FakeEngine(
        private val response: String,
        var shouldThrow: Boolean = false,
    ) : InferenceEngine {
        private var loaded = false
        var lastImageCount = -1
        var lastConfig: InferenceConfig? = null

        override suspend fun load(modelFile: File, config: InferenceConfig) {
            loaded = true
            lastConfig = config
        }

        override fun unload() {
            loaded = false
        }

        override fun isReady(): Boolean = loaded

        override suspend fun generate(prompt: String, images: List<ByteArray>): String {
            if (shouldThrow) error("inference failed")
            lastImageCount = images.size
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
    fun returns_parsed_result_and_loads_with_vision_and_one_image() =
        runTest {
            setupReadyModel()
            val raw = """{"type":"EVENT","title":"회의","datetime_iso":"2026-06-10T15:00:00"}"""
            val engine = FakeEngine(raw)
            val useCase = ClassifyImageCaptureUseCaseImpl(engine, fileStore, prefs)
            val r = useCase(byteArrayOf(1, 2, 3), "다음주 회의")
            assertNotNull(r)
            assertEquals("회의", r!!.title)
            assertEquals(1, engine.lastImageCount)
            assertTrue("이미지 분류는 enableVision=true 로 로드해야 함", engine.lastConfig!!.enableVision)
        }

    @Test
    fun returns_null_when_no_variant_selected() =
        runTest {
            val useCase = ClassifyImageCaptureUseCaseImpl(FakeEngine("{}"), fileStore, prefs)
            assertNull(useCase(byteArrayOf(1), null))
        }

    @Test
    fun returns_null_when_model_file_missing() =
        runTest {
            prefs.setSelectedVariant("v1")
            val useCase = ClassifyImageCaptureUseCaseImpl(FakeEngine("{}"), fileStore, prefs)
            assertNull(useCase(byteArrayOf(1), null))
        }

    @Test
    fun returns_null_when_inference_throws() =
        runTest {
            setupReadyModel()
            val useCase = ClassifyImageCaptureUseCaseImpl(FakeEngine("{}", shouldThrow = true), fileStore, prefs)
            assertNull(useCase(byteArrayOf(1), null))
        }
}
