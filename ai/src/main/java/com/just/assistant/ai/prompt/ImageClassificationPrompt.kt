package com.just.assistant.ai.prompt

object ImageClassificationPrompt {
    fun build(caption: String?): String {
        val captionSection =
            caption?.takeIf { it.isNotBlank() }?.let { "\n사용자 메모: ${it.trim()}" } ?: ""
        return """
            당신은 사용자가 첨부한 이미지를 분류하는 비서입니다.
            이미지(영수증·화이트보드·메모·일정표 등)를 읽고 정확히 하나의 JSON 객체로만 답하세요.
            설명·추가 텍스트 금지.

            JSON 스키마:
            {
              "type": "MEMO" | "EVENT" | "REMINDER",
              "title": "<짧은 제목, 최대 40자>",
              "body": "<이미지에서 읽은 핵심 내용>",
              "tags": ["<태그 0~3개>"],
              "datetime_iso": "<ISO 8601 datetime, 없으면 null>"
            }

            규칙:
            - 이미지에 날짜·시각이 있으면 EVENT, datetime_iso 채움.
            - 할 일/마감 성격이면 REMINDER.
            - 그 외는 MEMO. body에 이미지 핵심 텍스트 요약.$captionSection

            JSON:
        """.trimIndent()
    }
}
