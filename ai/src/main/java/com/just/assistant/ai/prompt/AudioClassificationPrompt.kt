package com.just.assistant.ai.prompt

object AudioClassificationPrompt {
    /**
     * 주의: "들리는 말을 받아 적어줘"류 전사 요청 표현은 모델의 텍스트 모드 거부
     * ("저는 오디오를 들을 수 없습니다")를 확률적으로 유발한다 (2026-07-09 spike 검증).
     * 음성 내용 처리를 task로 지시하는 표현만 사용한다.
     */
    fun build(caption: String?): String {
        val captionSection =
            caption?.takeIf { it.isNotBlank() }?.let { "\n사용자 메모: ${it.trim()}" } ?: ""
        return """
            당신은 사용자의 음성 메모를 분류하는 비서입니다.
            첨부된 오디오의 음성 내용을 바탕으로 정확히 하나의 JSON 객체로만 답하세요.
            설명·추가 텍스트 금지.

            JSON 스키마:
            {
              "type": "MEMO" | "EVENT" | "REMINDER",
              "title": "<짧은 제목, 최대 40자>",
              "body": "<음성 내용의 핵심>",
              "tags": ["<태그 0~3개>"],
              "datetime_iso": "<ISO 8601 datetime, 없으면 null>"
            }

            규칙:
            - 음성에 날짜·시각이 있으면 EVENT, datetime_iso 채움.
            - 동사형/명령형 할 일·마감 성격이면 REMINDER.
            - 그 외는 MEMO. body에 음성 핵심 내용 요약.$captionSection

            JSON:
        """.trimIndent()
    }
}
