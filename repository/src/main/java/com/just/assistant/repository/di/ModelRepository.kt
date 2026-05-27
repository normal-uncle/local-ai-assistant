package com.just.assistant.repository.di

import com.just.assistant.repository.model.ModelDownloadProgress
import com.just.assistant.repository.model.ModelStatus
import com.just.assistant.repository.model.ModelVariant
import kotlinx.coroutines.flow.Flow

interface ModelRepository {
    val status: Flow<ModelStatus>
    val downloadProgress: Flow<ModelDownloadProgress?>
    val selectedVariantId: Flow<String?>

    /** 카탈로그 fetch → 추천 variant 선택 → 메모리 캐시에 저장 → 반환. */
    suspend fun fetchAndSelectRecommendedVariant(catalogUrl: String): ModelVariant

    /** 선택된 variant. 없으면 null. */
    suspend fun selectedVariant(): ModelVariant?

    /**
     * 선택된 variant 다운로드. 진행률은 [downloadProgress] flow로 옵저빙.
     * 완료 시 [status]가 READY로 전환.
     */
    suspend fun downloadSelected()
}
