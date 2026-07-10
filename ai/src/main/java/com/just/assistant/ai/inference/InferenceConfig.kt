package com.just.assistant.ai.inference

data class InferenceConfig(
    val maxTokens: Int = 4096,
    val temperature: Float = 0.2f,
    val topK: Int = 40,
    val topP: Float = 0.95f,
    val preferGpu: Boolean = true,
    /** true면 EngineConfig에 visionBackend + maxNumImages를 실어 이미지 입력을 허용한다. */
    val enableVision: Boolean = false,
    /** true면 EngineConfig에 audioBackend를 실어 오디오 입력을 허용한다. */
    val enableAudio: Boolean = false,
)
