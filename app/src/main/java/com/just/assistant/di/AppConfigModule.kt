package com.just.assistant.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {
    /**
     * 개발용 카탈로그 URL. 현재는 v0.2-plan brach를 가리킴 (Gemma 4 E2B 카탈로그 포함).
     * TODO(머지 후): URL의 `feature/v0.2-plan` 부분을 `master`로 변경.
     */
    @Provides
    @Named("modelCatalogUrl")
    fun provideModelCatalogUrl(): String =
        "https://raw.githubusercontent.com/normal-uncle/local-ai-assistant/feature/v0.2-plan/dist/model_catalog.json"
}
