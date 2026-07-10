package com.just.assistant.ai.prompt

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioClassificationPromptTest {
    @Test
    fun includes_json_schema_keys() {
        val p = AudioClassificationPrompt.build(null)
        assertTrue(p.contains("datetime_iso"))
        assertTrue(p.contains("REMINDER"))
    }

    @Test
    fun appends_caption_when_present() {
        val p = AudioClassificationPrompt.build("다음주 회의")
        assertTrue(p.contains("다음주 회의"))
    }

    @Test
    fun omits_caption_section_when_blank() {
        val p = AudioClassificationPrompt.build("   ")
        assertFalse(p.contains("사용자 메모:"))
    }

    @Test
    fun avoids_transcription_request_phrasing() {
        // "받아 적어줘"류 전사 요청 표현은 모델의 텍스트 모드 거부("오디오를 들을 수 없습니다")를
        // 확률적으로 유발한다 (2026-07-09 spike 검증). task형 지시만 사용해야 한다.
        val p = AudioClassificationPrompt.build(null)
        assertFalse(p.contains("받아 적"))
        assertFalse(p.contains("들리는 말"))
    }
}
