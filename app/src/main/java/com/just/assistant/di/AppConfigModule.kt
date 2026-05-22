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
     * 개발용 카탈로그 URL. v0.2 출시 전 BuildConfig flavor별로 분리한다.
     * 현재 호스팅 위치: GitHub raw — normal-uncle/local-ai-assistant repo 안 dist/ 폴더.
     * Task 9에서 dist/ 폴더를 master에 push해서 fetchable해진다.
     */
    @Provides
    @Named("modelCatalogUrl")
    fun provideModelCatalogUrl(): String =
        "https://raw.githubusercontent.com/normal-uncle/local-ai-assistant/master/dist/model_catalog.json"
}
