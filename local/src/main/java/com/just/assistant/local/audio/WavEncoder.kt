package com.just.assistant.local.audio

import java.nio.ByteBuffer
import java.nio.ByteOrder

object WavEncoder {
    private const val HEADER_SIZE = 44

    /** PCM 16-bit mono 샘플을 WAV 컨테이너로 래핑. */
    fun pcm16ToWav(
        pcm: ByteArray,
        sampleRateHz: Int,
    ): ByteArray {
        val channels = 1
        val bytesPerSample = 2
        val byteRate = sampleRateHz * channels * bytesPerSample
        return ByteBuffer.allocate(HEADER_SIZE + pcm.size)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put("RIFF".toByteArray(Charsets.US_ASCII))
            .putInt(36 + pcm.size)
            .put("WAVE".toByteArray(Charsets.US_ASCII))
            .put("fmt ".toByteArray(Charsets.US_ASCII))
            .putInt(16)
            .putShort(1) // PCM
            .putShort(channels.toShort())
            .putInt(sampleRateHz)
            .putInt(byteRate)
            .putShort((channels * bytesPerSample).toShort()) // block align
            .putShort(16) // bits per sample
            .put("data".toByteArray(Charsets.US_ASCII))
            .putInt(pcm.size)
            .put(pcm)
            .array()
    }
}
