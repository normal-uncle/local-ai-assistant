package com.just.assistant.ai.inference

data class InferenceConfig(
    val maxTokens: Int = 512,
    val temperature: Float = 0.2f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
)
