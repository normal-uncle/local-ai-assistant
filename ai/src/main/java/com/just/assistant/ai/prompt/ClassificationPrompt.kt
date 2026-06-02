package com.just.assistant.ai.prompt

object ClassificationPrompt {
    /**
     * 사용자의 캡처 텍스트를 받아 구조화된 JSON 분류를 요청하는 프롬프트.
     * 모델이 출력해야 하는 JSON 스키마를 시스템 메시지처럼 박는다.
     */
    fun build(userInput: String): String =
        """
        당신은 사용자의 짧은 입력을 분류하는 비서입니다.
        아래 사용자 입력을 읽고 정확히 하나의 JSON 객체로만 답하세요. 설명·추가 텍스트 금지.

        JSON 스키마:
        {
          "type": "MEMO" | "EVENT" | "REMINDER",
          "title": "<짧은 제목, 최대 40자>",
          "body": "<본문, 없으면 빈 문자열>",
          "tags": ["<태그 0~3개>"],
          "datetime_iso": "<ISO 8601 datetime, 없으면 null>"
        }

        규칙:
        - "내일 3시 치과", "다음주 월요일 회의" 같이 특정 시각이 있으면 EVENT.
        - 시각이 명시되지 않아도 미래 사건·일정·약속 표현(요일·날짜·"등산"·"모임"·"식사" 등)은 EVENT.
        - "콜백 잊지 말기", "퇴근 전 보고서 제출" 같이 마감/할 일이면 REMINDER.
        - 동사형·명령형 동작/할 일(예: "~사오기", "~받기", "~제출", "buy", "pick up", "call", "register")은 시각이 없어도 REMINDER.
        - 그 외 일반 메모/생각/정보는 MEMO.
        - datetime_iso는 입력에 명확한 날짜·시간이 있을 때만 채우고, 없으면 null.
        - tags는 입력에 자연스럽게 포함된 키워드. 강제로 만들지 말 것.

        사용자 입력:
        ${userInput.trim()}

        JSON:
        """.trimIndent()
}
