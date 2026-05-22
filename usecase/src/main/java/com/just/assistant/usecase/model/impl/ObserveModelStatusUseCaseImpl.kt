package com.just.assistant.usecase.model.impl

import com.just.assistant.repository.di.ModelRepository
import com.just.assistant.repository.model.ModelStatus
import com.just.assistant.usecase.model.di.ObserveModelStatusUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveModelStatusUseCaseImpl
    @Inject
    constructor(
        private val repository: ModelRepository,
    ) : ObserveModelStatusUseCase {
        override fun invoke(): Flow<ModelStatus> = repository.status
    }
