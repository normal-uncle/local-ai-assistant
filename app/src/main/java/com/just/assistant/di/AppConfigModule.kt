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
     * 개발용 카탈로그 URL. 현재는 P2.C 작업 brach를 가리킴.
     * TODO(P2.C 머지 후): URL의 `feature/v0.1-p2c-plan` 부분을 `master`로 변경.
     */
    @Provides
    @Named("modelCatalogUrl")
    fun provideModelCatalogUrl(): String =
        "https://raw.githubusercontent.com/normal-uncle/local-ai-assistant/feature/v0.1-p2c-plan/dist/model_catalog.json"
}
