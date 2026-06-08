package com.just.assistant.usecase.chat.impl

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.just.assistant.ai.inference.ChatSession
import com.just.assistant.ai.inference.InferenceConfig
import com.just.assistant.ai.inference.InferenceEngine
import com.just.assistant.local.model.ModelFileStore
import com.just.assistant.local.model.ModelStatusPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
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
class StartChatSessionUseCaseImplTest {
    private class FakeSession : ChatSession {
        override fun send(message: String): Flow<String> = flowOf("ok")
        override fun close() {}
    }

    private class FakeEngine(
        var startThrows: Boolean = false,
    ) : InferenceEngine {
        var loaded = false
        var startCount = 0

        override suspend fun load(modelFile: File, config: InferenceConfig) { loaded = true }
        override fun unload() { loaded = false }
        override fun isReady(): Boolean = loaded
        override suspend fun generate(prompt: String, images: List<ByteArray>): String = ""
        override fun startChat(): ChatSession {
            startCount++
            if (startThrows) error("start failed")
            return FakeSession()
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
    fun returns_session_when_model_ready() =
        runTest {
            setupReadyModel()
            val engine = FakeEngine()
            val useCase = StartChatSessionUseCaseImpl(engine, fileStore, prefs)
            val s = useCase()
            assertNotNull(s)
            assertTrue(engine.loaded)
            assertTrue(engine.startCount == 1)
        }

    @Test
    fun returns_null_when_no_variant() =
        runTest {
            val useCase = StartChatSessionUseCaseImpl(FakeEngine(), fileStore, prefs)
            assertNull(useCase())
        }

    @Test
    fun returns_null_when_model_file_missing() =
        runTest {
            prefs.setSelectedVariant("v1")
            val useCase = StartChatSessionUseCaseImpl(FakeEngine(), fileStore, prefs)
            assertNull(useCase())
        }

    @Test
    fun returns_null_when_start_throws() =
        runTest {
            setupReadyModel()
            val useCase = StartChatSessionUseCaseImpl(FakeEngine(startThrows = true), fileStore, prefs)
            assertNull(useCase())
        }
}
