package com.just.assistant.remote.catalog

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.just.assistant.remote.catalog.dto.CatalogDto
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class ModelCatalogServiceTest {
    private lateinit var server: MockWebServer
    private lateinit var service: ModelCatalogService

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        val json = Json { ignoreUnknownKeys = true }
        service =
            Retrofit.Builder()
                .baseUrl(server.url("/"))
                .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
                .build()
                .create(ModelCatalogService::class.java)
    }

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun fetch_parses_catalog_json() =
        runTest {
            server.enqueue(
                MockResponse().setBody(
                    """
                    {
                      "version": "2026-05-21",
                      "variants": [
                        {
                          "id": "gemma4-e2b-q4",
                          "url": "https://example.com/e2b-q4.task",
                          "sha256": "abc123",
                          "sizeMb": 1500,
                          "minRamGb": 6,
                          "recommended": true
                        }
                      ]
                    }
                    """.trimIndent(),
                ).setHeader("Content-Type", "application/json"),
            )

            val catalog: CatalogDto = service.fetch(server.url("/catalog.json").toString())

            assertEquals("2026-05-21", catalog.version)
            assertEquals(1, catalog.variants.size)
            val v = catalog.variants[0]
            assertEquals("gemma4-e2b-q4", v.id)
            assertEquals(1500L, v.sizeMb)
            assertEquals(6, v.minRamGb)
            assertTrue(v.recommended)
        }
}
