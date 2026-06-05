package com.just.assistant.ai.prompt

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImageClassificationPromptTest {
    @Test
    fun includes_json_schema_keys() {
        val p = ImageClassificationPrompt.build(null)
        assertTrue(p.contains("datetime_iso"))
        assertTrue(p.contains("REMINDER"))
    }

    @Test
    fun appends_caption_when_present() {
        val p = ImageClassificationPrompt.build("다음주 회의")
        assertTrue(p.contains("다음주 회의"))
    }

    @Test
    fun omits_caption_section_when_blank() {
        val p = ImageClassificationPrompt.build("   ")
        assertFalse(p.contains("사용자 메모:"))
    }
}
