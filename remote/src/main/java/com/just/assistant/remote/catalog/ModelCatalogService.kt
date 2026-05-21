package com.just.assistant.remote.catalog

import com.just.assistant.remote.catalog.dto.CatalogDto
import retrofit2.http.GET
import retrofit2.http.Url

interface ModelCatalogService {
    /**
     * 절대 URL로 카탈로그 JSON을 fetch. 카탈로그 호스팅 위치가 변경되면 URL만 갈아끼우면 됨.
     */
    @GET
    suspend fun fetch(
        @Url url: String,
    ): CatalogDto
}
