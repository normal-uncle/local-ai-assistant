package com.just.assistant.local.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

interface AudioRecorder {
    /**
     * 마이크 녹음 시작. RECORD_AUDIO 권한은 호출 측이 보장.
     * 이미 녹음 중이거나 AudioRecord 초기화 실패 시 false.
     */
    fun start(): Boolean

    /**
     * 녹음 종료 후 WAV(16kHz mono PCM16) 바이트 반환. 녹음 중이 아니면 null.
     * [MAX_DURATION_SECONDS] 초과분은 버려진다.
     */
    fun stop(): ByteArray?

    val isRecording: Boolean

    companion object {
        const val MAX_DURATION_SECONDS = 30
    }
}

@Singleton
class AudioRecorderImpl
    @Inject
    constructor() : AudioRecorder {
        private var record: AudioRecord? = null
        private var readerThread: Thread? = null
        private val buffer = ByteArrayOutputStream()

        override val isRecording: Boolean get() = record != null

        @SuppressLint("MissingPermission")
        override fun start(): Boolean {
            if (record != null) return false
            val minBuf =
                AudioRecord.getMinBufferSize(
                    SAMPLE_RATE_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                )
            if (minBuf <= 0) return false
            val r =
                AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_HZ,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBuf * 2,
                )
            if (r.state != AudioRecord.STATE_INITIALIZED) {
                r.release()
                return false
            }
            buffer.reset()
            r.startRecording()
            record = r
            readerThread =
                Thread {
                    val chunk = ByteArray(minBuf)
                    while (record === r) {
                        val n = r.read(chunk, 0, chunk.size)
                        if (n <= 0) break
                        synchronized(buffer) {
                            if (buffer.size() < MAX_BYTES) {
                                buffer.write(chunk, 0, n.coerceAtMost(MAX_BYTES - buffer.size()))
                            }
                        }
                    }
                }.apply {
                    isDaemon = true
                    start()
                }
            return true
        }

        override fun stop(): ByteArray? {
            val r = record ?: return null
            record = null
            readerThread?.join(1_000)
            readerThread = null
            runCatching { r.stop() }
            r.release()
            val pcm = synchronized(buffer) { buffer.toByteArray().also { buffer.reset() } }
            return WavEncoder.pcm16ToWav(pcm, SAMPLE_RATE_HZ)
        }

        companion object {
            private const val SAMPLE_RATE_HZ = 16_000
            private const val MAX_BYTES = SAMPLE_RATE_HZ * 2 * AudioRecorder.MAX_DURATION_SECONDS
        }
    }
