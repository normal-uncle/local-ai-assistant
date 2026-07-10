package com.just.assistant.local.audio

import org.junit.Assert.assertEquals
import org.junit.Test
import java.nio.ByteBuffer
import java.nio.ByteOrder

class WavEncoderTest {
    @Test
    fun wraps_pcm16_with_valid_riff_header() {
        val pcm = ByteArray(1000)
        val wav = WavEncoder.pcm16ToWav(pcm, sampleRateHz = 16000)

        assertEquals(44 + 1000, wav.size)
        assertEquals("RIFF", String(wav, 0, 4))
        assertEquals("WAVE", String(wav, 8, 4))
        assertEquals("fmt ", String(wav, 12, 4))
        assertEquals("data", String(wav, 36, 4))

        val buf = ByteBuffer.wrap(wav).order(ByteOrder.LITTLE_ENDIAN)
        assertEquals(36 + 1000, buf.getInt(4)) // RIFF chunk size
        assertEquals(1, buf.getShort(20).toInt()) // PCM format
        assertEquals(1, buf.getShort(22).toInt()) // mono
        assertEquals(16000, buf.getInt(24)) // sample rate
        assertEquals(16000 * 2, buf.getInt(28)) // byte rate
        assertEquals(16, buf.getShort(34).toInt()) // bits per sample
        assertEquals(1000, buf.getInt(40)) // data length
    }

    @Test
    fun handles_empty_pcm() {
        val wav = WavEncoder.pcm16ToWav(ByteArray(0), sampleRateHz = 16000)
        assertEquals(44, wav.size)
    }
}
