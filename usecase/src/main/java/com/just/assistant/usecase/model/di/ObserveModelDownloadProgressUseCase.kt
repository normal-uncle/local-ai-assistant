package com.just.assistant.usecase.model.di

import com.just.assistant.repository.model.ModelDownloadProgress
import kotlinx.coroutines.flow.Flow

interface ObserveModelDownloadProgressUseCase {
    operator fun invoke(): Flow<ModelDownloadProgress?>
}
