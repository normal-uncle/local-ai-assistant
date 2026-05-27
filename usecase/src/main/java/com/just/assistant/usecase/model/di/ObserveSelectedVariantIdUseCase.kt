package com.just.assistant.usecase.model.di

import kotlinx.coroutines.flow.Flow

interface ObserveSelectedVariantIdUseCase {
    operator fun invoke(): Flow<String?>
}
