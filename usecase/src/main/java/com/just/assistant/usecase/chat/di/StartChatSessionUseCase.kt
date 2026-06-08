package com.just.assistant.usecase.chat.di

import com.just.assistant.ai.inference.ChatSession

interface StartChatSessionUseCase {
    /** 모델 준비 시 ChatSession, 미준비/실패 시 null. */
    suspend operator fun invoke(): ChatSession?
}
