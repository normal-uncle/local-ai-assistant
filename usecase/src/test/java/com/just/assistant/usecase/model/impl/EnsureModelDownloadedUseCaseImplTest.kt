package com.just.assistant.usecase.model.impl

import com.just.assistant.repository.di.ModelRepository
import com.just.assistant.repository.model.ModelDownloadProgress
import com.just.assistant.repository.model.ModelStatus
import com.just.assistant.repository.model.ModelVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EnsureModelDownloadedUseCaseImplTest {
    private class FakeRepo(initialStatus: ModelStatus) : ModelRepository {
        var fetchedUrl: String? = null
        var downloadCalled = false
        private val statusFlow = MutableStateFlow(initialStatus)

        override val status: Flow<ModelStatus> = statusFlow
        override val downloadProgress: Flow<ModelDownloadProgress?> = flowOf(null)

        override suspend fun fetchAndSelectRecommendedVariant(catalogUrl: String): ModelVariant {
            fetchedUrl = catalogUrl
            return ModelVariant("v1", "u", "sha", 1, 4, true)
        }

        override suspend fun selectedVariant(): ModelVariant? = ModelVariant("v1", "u", "sha", 1, 4, true)

        override suspend fun downloadSelected() {
            downloadCalled = true
            statusFlow.value = ModelStatus.READY
        }
    }

    @Test
    fun no_op_when_status_is_ready() =
        runTest {
            val repo = FakeRepo(ModelStatus.READY)
            EnsureModelDownloadedUseCaseImpl(repo).invoke("https://x/c.json")
            assertEquals(null, repo.fetchedUrl)
            assertEquals(false, repo.downloadCalled)
        }

    @Test
    fun fetches_and_downloads_when_not_ready() =
        runTest {
            val repo = FakeRepo(ModelStatus.NOT_READY)
            EnsureModelDownloadedUseCaseImpl(repo).invoke("https://x/c.json")
            assertEquals("https://x/c.json", repo.fetchedUrl)
            assertEquals(true, repo.downloadCalled)
        }
}
