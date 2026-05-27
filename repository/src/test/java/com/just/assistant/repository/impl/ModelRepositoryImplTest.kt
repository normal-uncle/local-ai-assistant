package com.just.assistant.repository.impl

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.just.assistant.local.device.DeviceProfiler
import com.just.assistant.local.model.ModelFileStore
import com.just.assistant.local.model.ModelStatusPrefs
import com.just.assistant.remote.catalog.ModelCatalogService
import com.just.assistant.remote.catalog.dto.CatalogDto
import com.just.assistant.remote.catalog.dto.VariantDto
import com.just.assistant.remote.download.DownloadEvent
import com.just.assistant.remote.download.ModelDownloader
import com.just.assistant.repository.model.ModelStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class ModelRepositoryImplTest {
    private lateinit var fileStore: ModelFileStore
    private lateinit var prefs: ModelStatusPrefs
    private lateinit var repo: ModelRepositoryImpl

    private class FakeService(private val catalog: CatalogDto) : ModelCatalogService {
        var requestedUrl: String? = null

        override suspend fun fetch(url: String): CatalogDto {
            requestedUrl = url
            return catalog
        }
    }

    private class FakeDownloader : ModelDownloader {
        override fun download(
            url: String,
            destination: File,
            expectedSha256: String,
        ): Flow<DownloadEvent> {
            destination.writeBytes(ByteArray(10))
            return flowOf(
                DownloadEvent.Progress(10L, 10L),
                DownloadEvent.Completed(10L),
            )
        }
    }

    private val sampleCatalog =
        CatalogDto(
            version = "2026-05-21",
            variants =
                listOf(
                    VariantDto(
                        id = "gemma4-e2b-q4",
                        url = "https://example.com/e2b.task",
                        sha256 = "abc",
                        sizeMb = 1500,
                        minRamGb = 6,
                        recommended = true,
                    ),
                ),
        )

    @Before
    fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<android.content.Context>()
        fileStore = ModelFileStore(ctx)
        prefs = ModelStatusPrefs(ctx)
        fileStore.delete("gemma4-e2b-q4")
        val profiler = DeviceProfiler(ctx)
        repo = ModelRepositoryImpl(FakeService(sampleCatalog), FakeDownloader(), fileStore, prefs, profiler)
    }

    @After
    fun tearDown() {
        fileStore.delete("gemma4-e2b-q4")
    }

    @Test
    fun fetchAndSelectRecommendedVariant_returns_recommended_and_persists() =
        runTest {
            val v = repo.fetchAndSelectRecommendedVariant("https://x/catalog.json")
            assertEquals("gemma4-e2b-q4", v.id)
            val persisted = repo.selectedVariant()
            assertNotNull(persisted)
            assertEquals("gemma4-e2b-q4", persisted!!.id)
        }

    @Test
    fun status_is_NOT_READY_initially() =
        runTest {
            assertEquals(ModelStatus.NOT_READY, repo.status.first())
        }

    @Test
    fun downloadSelected_transitions_status_to_READY() =
        runTest {
            repo.fetchAndSelectRecommendedVariant("https://x/catalog.json")
            repo.downloadSelected()
            assertEquals(ModelStatus.READY, repo.status.first())
        }
}
