package com.just.assistant.local.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

interface TtsSpeaker {
    /** [text]를 낭독. 이전 발화는 끊고 새로 시작. 미초기화/빈 문자열이면 no-op. */
    fun speak(text: String)

    /** 현재 낭독 중단. */
    fun stop()
}

@Singleton
class TtsSpeakerImpl
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : TtsSpeaker {
        @Volatile private var ready = false
        private lateinit var tts: TextToSpeech

        init {
            tts =
                TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        tts.language = Locale.getDefault()
                        ready = true
                    }
                }
        }

        override fun speak(text: String) {
            if (!ready || text.isBlank()) return
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "chat")
        }

        override fun stop() {
            if (ready) tts.stop()
        }
    }
