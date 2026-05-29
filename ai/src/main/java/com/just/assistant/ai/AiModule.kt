package com.just.assistant.ai

import com.just.assistant.ai.inference.InferenceEngine
import com.just.assistant.ai.inference.LiteRtLmInferenceEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {
    @Binds
    @Singleton
    abstract fun bindInferenceEngine(impl: LiteRtLmInferenceEngine): InferenceEngine
}
