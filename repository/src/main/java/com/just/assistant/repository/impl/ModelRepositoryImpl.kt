package com.just.assistant.repository.impl

import com.just.assistant.local.model.ModelFileStore
import com.just.assistant.local.model.ModelStatusPrefs
import com.just.assistant.remote.catalog.ModelCatalogService
import com.just.assistant.remote.catalog.dto.VariantDto
import com.just.assistant.remote.download.DownloadEvent
import com.just.assistant.remote.download.ModelDownloader
import com.just.assistant.repository.di.ModelRepository
import com.just.assistant.repository.model.ModelDownloadProgress
import com.just.assistant.repository.model.ModelStatus
import com.just.assistant.repository.model.ModelVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRepositoryImpl
    @Inject
    constructor(
        private val service: ModelCatalogService,
        private val downloader: ModelDownloader,
        private val fileStore: ModelFileStore,
        private val prefs: ModelStatusPrefs,
    ) : ModelRepository {
        private val progress = MutableStateFlow<ModelDownloadProgress?>(null)

        @Volatile
        private var memoryCache: VariantDto? = null

        override val status: Flow<ModelStatus> =
            prefs.rawStatus.map {
                when (it) {
                    "DOWNLOADING" -> ModelStatus.DOWNLOADING
                    "READY" -> ModelStatus.READY
                    "FAILED" -> ModelStatus.FAILED
                    else -> ModelStatus.NOT_READY
                }
            }

        override val downloadProgress: Flow<ModelDownloadProgress?> = progress.asStateFlow()

        override suspend fun fetchAndSelectRecommendedVariant(catalogUrl: String): ModelVariant {
            val catalog = service.fetch(catalogUrl)
            val recommended =
                catalog.variants.firstOrNull { it.recommended }
                    ?: catalog.variants.firstOrNull()
                    ?: error("catalog has no variants")
            memoryCache = recommended
            prefs.setSelectedVariant(recommended.id)
            return recommended.toDomain()
        }

        override suspend fun selectedVariant(): ModelVariant? = memoryCache?.toDomain()

        override suspend fun downloadSelected() {
            val variant = memoryCache ?: error("no selected variant; call fetchAndSelectRecommendedVariant first")
            prefs.setStatus("DOWNLOADING")
            val file = fileStore.fileFor(variant.id)
            downloader.download(variant.url, file, variant.sha256).collect { event ->
                when (event) {
                    is DownloadEvent.Progress -> {
                        progress.value = ModelDownloadProgress(variant.id, event.bytesWritten, event.totalBytes)
                    }
                    is DownloadEvent.Completed -> {
                        progress.value = ModelDownloadProgress(variant.id, event.totalBytes, event.totalBytes)
                        prefs.setStatus("READY")
                    }
                    is DownloadEvent.Failed -> {
                        prefs.setStatus("FAILED")
                    }
                }
            }
        }

        private fun VariantDto.toDomain(): ModelVariant =
            ModelVariant(
                id = id,
                url = url,
                sha256 = sha256,
                sizeMb = sizeMb,
                minRamGb = minRamGb,
                recommended = recommended,
            )
    }
