package com.just.assistant.usecase.model.impl

import com.just.assistant.repository.di.ModelRepository
import com.just.assistant.repository.model.ModelDownloadProgress
import com.just.assistant.usecase.model.di.ObserveModelDownloadProgressUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveModelDownloadProgressUseCaseImpl
    @Inject
    constructor(
        private val repository: ModelRepository,
    ) : ObserveModelDownloadProgressUseCase {
        override fun invoke(): Flow<ModelDownloadProgress?> = repository.downloadProgress
    }
