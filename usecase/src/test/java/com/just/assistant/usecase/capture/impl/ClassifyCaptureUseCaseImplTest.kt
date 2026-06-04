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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ClassifyCaptureUseCaseImplTest {
    private class FakeEngine(
        private val response: String,
        var shouldThrow: Boolean = false,
    ) : InferenceEngine {
        private var loaded = false
        var loadCount = 0

        override suspend fun load(
            modelFile: File,
            config: InferenceConfig,
        ) {
            loadCount++
            loaded = true
        }

        override fun unload() {
            loaded = false
        }

        override fun isReady(): Boolean = loaded

        override suspend fun generate(
            prompt: String,
            images: List<ByteArray>,
        ): String {
            if (shouldThrow) error("inference failed")
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
    fun returns_parsed_result_on_successful_inference() =
        runTest {
            setupReadyModel()
            val raw = """{"type":"EVENT","title":"치과","datetime_iso":"2026-05-23T15:00:00"}"""
            val useCase = ClassifyCaptureUseCaseImpl(FakeEngine(raw), fileStore, prefs)
            val r = useCase("내일 3시 치과")
            assertNotNull(r)
            assertEquals("치과", r!!.title)
        }

    @Test
    fun returns_null_when_no_variant_selected() =
        runTest {
            val useCase = ClassifyCaptureUseCaseImpl(FakeEngine("{}"), fileStore, prefs)
            assertNull(useCase("hi"))
        }

    @Test
    fun returns_null_when_model_file_missing() =
        runTest {
            prefs.setSelectedVariant("v1")
            val useCase = ClassifyCaptureUseCaseImpl(FakeEngine("{}"), fileStore, prefs)
            assertNull(useCase("hi"))
        }

    @Test
    fun returns_null_when_inference_throws() =
        runTest {
            setupReadyModel()
            val engine = FakeEngine("{}", shouldThrow = true)
            val useCase = ClassifyCaptureUseCaseImpl(engine, fileStore, prefs)
            assertNull(useCase("hi"))
        }

    @Test
    fun returns_null_when_response_not_json() =
        runTest {
            setupReadyModel()
            val useCase = ClassifyCaptureUseCaseImpl(FakeEngine("그냥 잡담"), fileStore, prefs)
            assertNull(useCase("hi"))
        }

    @Test
    fun loads_engine_only_once_across_calls() =
        runTest {
            setupReadyModel()
            val engine = FakeEngine("""{"type":"MEMO","title":"t","body":""}""")
            val useCase = ClassifyCaptureUseCaseImpl(engine, fileStore, prefs)
            repeat(3) { useCase("hi") }
            assertEquals(1, engine.loadCount)
        }
}
