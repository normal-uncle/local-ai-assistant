package com.just.assistant.usecase.model.di

import com.just.assistant.repository.model.ModelStatus
import kotlinx.coroutines.flow.Flow

interface ObserveModelStatusUseCase {
    operator fun invoke(): Flow<ModelStatus>
}
