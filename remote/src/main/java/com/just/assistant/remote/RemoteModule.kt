package com.just.assistant.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.just.assistant.remote.catalog.ModelCatalogService
import com.just.assistant.remote.download.ModelDownloader
import com.just.assistant.remote.download.OkHttpModelDownloader
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    /**
     * Retrofit requires a baseUrl, but every endpoint on this module uses `@Url` with an
     * absolute URL (카탈로그 호스팅 위치가 환경마다 다름). The base URL is therefore unused
     * at request time. We pick a deliberately invalid hostname so any accidental
     * relative `@GET("foo")` call fails fast at the network layer instead of silently
     * routing to example.com.
     */
    private const val UNUSED_BASE_URL = "https://placeholder.invalid/"

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        json: Json,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(UNUSED_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideModelCatalogService(retrofit: Retrofit): ModelCatalogService = retrofit.create(ModelCatalogService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteBindings {
    @Binds
    @Singleton
    abstract fun bindModelDownloader(impl: OkHttpModelDownloader): ModelDownloader
}
