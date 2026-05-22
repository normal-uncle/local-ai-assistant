package com.just.assistant.remote.download

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

sealed interface DownloadEvent {
    data class Progress(val bytesWritten: Long, val totalBytes: Long?) : DownloadEvent

    data class Completed(val totalBytes: Long) : DownloadEvent

    data class Failed(val message: String) : DownloadEvent
}

interface ModelDownloader {
    fun download(
        url: String,
        destination: File,
        expectedSha256: String,
    ): Flow<DownloadEvent>
}

@Singleton
class OkHttpModelDownloader
    @Inject
    constructor(
        private val client: OkHttpClient,
    ) : ModelDownloader {
        override fun download(
            url: String,
            destination: File,
            expectedSha256: String,
        ): Flow<DownloadEvent> =
            flow {
                val startBytes = if (destination.exists()) destination.length() else 0L
                val builder = Request.Builder().url(url)
                if (startBytes > 0) builder.header("Range", "bytes=$startBytes-")
                val request = builder.build()

                try {
                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            emit(DownloadEvent.Failed("HTTP ${response.code}"))
                            return@flow
                        }
                        val body =
                            response.body ?: run {
                                emit(DownloadEvent.Failed("empty body"))
                                return@flow
                            }
                        val contentLength = body.contentLength().takeIf { it > 0 }
                        val totalBytes = contentLength?.let { it + startBytes }

                        RandomAccessFile(destination, "rw").use { raf ->
                            raf.channel.use { sink ->
                                sink.position(startBytes)
                                val source = body.byteStream()
                                val buf = ByteArray(64 * 1024)
                                var written = startBytes
                                while (true) {
                                    val read = source.read(buf)
                                    if (read < 0) break
                                    sink.write(ByteBuffer.wrap(buf, 0, read))
                                    written += read
                                    emit(DownloadEvent.Progress(written, totalBytes))
                                }
                            }
                        }
                    }
                } catch (e: IOException) {
                    emit(DownloadEvent.Failed(e.message ?: "I/O error"))
                    return@flow
                }

                val actualSha = sha256(destination)
                if (!actualSha.equals(expectedSha256, ignoreCase = true)) {
                    destination.delete()
                    emit(DownloadEvent.Failed("sha256 mismatch (expected=$expectedSha256, actual=$actualSha)"))
                    return@flow
                }
                emit(DownloadEvent.Completed(destination.length()))
            }

        private fun sha256(file: File): String {
            val md = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buf = ByteArray(64 * 1024)
                while (true) {
                    val n = input.read(buf)
                    if (n < 0) break
                    md.update(buf, 0, n)
                }
            }
            return md.digest().joinToString("") { "%02x".format(it) }
        }
    }
