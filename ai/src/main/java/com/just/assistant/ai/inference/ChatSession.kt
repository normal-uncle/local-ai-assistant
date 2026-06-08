package com.just.assistant.ai.inference

import kotlinx.coroutines.flow.Flow

interface ChatSession {
    /** 사용자 메시지 전송 → 응답 토큰 델타 스트림(증분). 완료 시 자연 종료. */
    fun send(message: String): Flow<String>

    /** 세션 종료(Conversation 닫기). */
    fun close()
}
