package com.just.assistant.usecase.model.impl

import com.just.assistant.repository.di.ModelRepository
import com.just.assistant.usecase.model.di.ObserveSelectedVariantIdUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSelectedVariantIdUseCaseImpl
    @Inject
    constructor(
        private val repository: ModelRepository,
    ) : ObserveSelectedVariantIdUseCase {
        override fun invoke(): Flow<String?> = repository.selectedVariantId
    }
