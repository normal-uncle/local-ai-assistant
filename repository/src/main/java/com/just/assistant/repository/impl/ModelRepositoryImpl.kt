package com.just.assistant.repository.impl

import com.just.assistant.local.device.DeviceProfiler
import com.just.assistant.local.model.ModelFileStore
import com.just.assistant.local.model.ModelStatusPrefs
import com.just.assistant.remote.catalog.ModelCatalogService
import com.just.assistant.remote.catalog.dto.VariantDto
import com.just.assistant.remote.download.DownloadEvent
import com.just.assistant.remote.download.ModelDownloader
import com.just.assistant.repository.di.ModelRepository
import com.just.assistant.repository.model.DeviceProfile
import com.just.assistant.repository.model.ModelDownloadProgress
import com.just.assistant.repository.model.ModelStatus
import com.just.assistant.repository.model.ModelVariant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
        private val profiler: DeviceProfiler,
    ) : ModelRepository {
        private val progress = MutableStateFlow<ModelDownloadProgress?>(null)

        @Volatile
        private var memoryCache: VariantDto? = null

        override val status: Flow<ModelStatus> =
            prefs.rawStatus.combine(prefs.selectedVariantId) { raw, variantId ->
                when (raw) {
                    "DOWNLOADING" -> ModelStatus.DOWNLOADING
                    // 사용자가 저장공간 정리 등으로 파일만 지운 경우 prefs가 READY로 남을 수 있음
                    "READY" ->
                        if (variantId != null && fileStore.exists(variantId)) {
                            ModelStatus.READY
                        } else {
                            ModelStatus.NOT_READY
                        }
                    "FAILED" -> ModelStatus.FAILED
                    else -> ModelStatus.NOT_READY
                }
            }

        override val downloadProgress: Flow<ModelDownloadProgress?> = progress.asStateFlow()

        override val selectedVariantId: Flow<String?> = prefs.selectedVariantId

        override suspend fun fetchAndSelectRecommendedVariant(catalogUrl: String): ModelVariant {
            val catalog = service.fetch(catalogUrl)
            require(catalog.variants.isNotEmpty()) { "catalog has no variants" }
            val capability = profiler.probe()
            val profile = DeviceProfile(totalRamGb = capability.totalRamGb)
            val chosen = VariantSelector.select(catalog.variants, profile)
            memoryCache = chosen
            prefs.setSelectedVariant(chosen.id)
            return chosen.toDomain()
        }

        override suspend fun selectedVariant(): ModelVariant? = memoryCache?.toDomain()

        override suspend fun downloadSelected() {
            val variant = memoryCache ?: error("no selected variant; call fetchAndSelectRecommendedVariant first")
            val file = fileStore.fileFor(variant.id)
            if (file.exists() &&
                fileStore.sha256(variant.id)?.equals(variant.sha256, ignoreCase = true) == true
            ) {
                progress.value = ModelDownloadProgress(variant.id, file.length(), file.length())
                prefs.setStatus("READY")
                return
            }
            prefs.setStatus("DOWNLOADING")
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
