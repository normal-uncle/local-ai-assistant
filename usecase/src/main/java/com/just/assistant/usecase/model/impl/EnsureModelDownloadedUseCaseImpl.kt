package com.just.assistant.usecase.model.impl

import com.just.assistant.repository.di.ModelRepository
import com.just.assistant.repository.model.ModelStatus
import com.just.assistant.usecase.model.di.EnsureModelDownloadedUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class EnsureModelDownloadedUseCaseImpl
    @Inject
    constructor(
        private val repository: ModelRepository,
    ) : EnsureModelDownloadedUseCase {
        override suspend fun invoke(catalogUrl: String) {
            val current = repository.status.first()
            if (current == ModelStatus.READY) return
            repository.fetchAndSelectRecommendedVariant(catalogUrl)
            repository.downloadSelected()
        }
    }
