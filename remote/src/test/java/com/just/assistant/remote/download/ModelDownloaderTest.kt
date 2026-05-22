package com.just.assistant.remote.download

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okio.Buffer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.security.MessageDigest

class ModelDownloaderTest {
    private lateinit var server: MockWebServer
    private lateinit var tmp: File
    private lateinit var downloader: ModelDownloader

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        tmp = File.createTempFile("model-test", ".bin").apply { delete() }
        downloader = OkHttpModelDownloader(OkHttpClient())
    }

    @After
    fun tearDown() {
        server.shutdown()
        tmp.delete()
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    @Test
    fun download_writes_full_file_and_emits_progress() =
        runTest {
            val payload = ByteArray(10_000) { (it and 0xff).toByte() }
            val expectedSha = sha256(payload)
            server.enqueue(
                MockResponse()
                    .setBody(Buffer().write(payload))
                    .setHeader("Content-Length", payload.size.toString()),
            )

            val events =
                downloader.download(
                    url = server.url("/m.bin").toString(),
                    destination = tmp,
                    expectedSha256 = expectedSha,
                ).toList()

            assertTrue("expected progress events", events.size > 1)
            assertEquals(DownloadEvent.Completed(payload.size.toLong()), events.last())
            assertEquals(payload.size.toLong(), tmp.length())
            assertEquals(expectedSha, sha256(tmp.readBytes()))
        }

    @Test
    fun download_resumes_partial_file_with_range_request() =
        runTest {
            val full = ByteArray(8_000) { (it and 0xff).toByte() }
            val partial = full.copyOfRange(0, 3_000)
            tmp.writeBytes(partial)

            val tail = full.copyOfRange(3_000, full.size)
            server.enqueue(
                MockResponse()
                    .setResponseCode(206)
                    .setBody(Buffer().write(tail))
                    .setHeader("Content-Length", tail.size.toString())
                    .setHeader("Content-Range", "bytes 3000-7999/8000"),
            )

            val events =
                downloader.download(
                    url = server.url("/m.bin").toString(),
                    destination = tmp,
                    expectedSha256 = sha256(full),
                ).toList()

            val request = server.takeRequest()
            assertEquals("bytes=3000-", request.getHeader("Range"))
            assertEquals(DownloadEvent.Completed(full.size.toLong()), events.last())
            assertEquals(full.size.toLong(), tmp.length())
            assertEquals(sha256(full), sha256(tmp.readBytes()))
        }

    @Test
    fun download_fails_when_sha_mismatches() =
        runTest {
            val payload = ByteArray(500) { 1 }
            server.enqueue(
                MockResponse().setBody(Buffer().write(payload)).setHeader("Content-Length", "500"),
            )

            val events =
                downloader.download(
                    url = server.url("/m.bin").toString(),
                    destination = tmp,
                    expectedSha256 = "0".repeat(64),
                ).toList()

            val last = events.last()
            assertTrue("expected Failed but got $last", last is DownloadEvent.Failed)
        }

    @Test
    fun download_completes_when_collected_from_non_io_dispatcher() =
        runTest {
            val payload = ByteArray(2_000) { 0x42 }
            val expectedSha = sha256(payload)
            server.enqueue(
                okhttp3.mockwebserver.MockResponse()
                    .setBody(okio.Buffer().write(payload))
                    .setHeader("Content-Length", payload.size.toString()),
            )

            val events =
                downloader.download(
                    url = server.url("/m.bin").toString(),
                    destination = tmp,
                    expectedSha256 = expectedSha,
                ).toList()

            assertEquals(DownloadEvent.Completed(payload.size.toLong()), events.last())
        }
}
