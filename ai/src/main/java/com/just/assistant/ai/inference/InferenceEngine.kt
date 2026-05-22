package com.just.assistant.ai.inference

import java.io.File

interface InferenceEngine {
    /**
     * [modelFile]을 메모리에 로드한다. 이미 로드돼 있으면 no-op.
     * 모델 파일이 유효하지 않으면 throw.
     */
    suspend fun load(
        modelFile: File,
        config: InferenceConfig = InferenceConfig(),
    )

    /** 메모리에서 모델 unload. 호출 후 [generate]는 IllegalStateException. */
    fun unload()

    /** 현재 로드 상태. */
    fun isReady(): Boolean

    /**
     * 동기 추론. [prompt]에 대한 텍스트 응답을 반환.
     * 호출 전 [load] 필요. 미로드 상태에서 호출 시 IllegalStateException.
     */
    suspend fun generate(prompt: String): String
}
