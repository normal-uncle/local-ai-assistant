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
     * 텍스트 전용 추론. [generate] (prompt, emptyList) 와 동일.
     * 호출 전 [load] 필요. 미로드 상태에서 호출 시 IllegalStateException.
     */
    suspend fun generate(prompt: String): String = generate(prompt, emptyList())

    /**
     * 멀티모달 추론. [images] 가 비었으면 텍스트 전용과 동일.
     * 각 이미지는 디코드 가능한 JPEG/PNG 등의 바이트.
     */
    suspend fun generate(prompt: String, images: List<ByteArray>): String

    /**
     * 멀티턴 채팅 세션 시작. 호출 전 [load] 필요.
     * 기본 구현은 미지원 throw — 채팅 지원 엔진(LiteRtLm)만 override.
     */
    fun startChat(): ChatSession = throw UnsupportedOperationException("chat not supported")

    /**
     * 마지막 [load] 호출에서 실제 사용된 backend 식별자. 로드 전이면 null.
     * 회귀 리포트 등 진단 용도.
     */
    val lastBackend: String? get() = null
}
