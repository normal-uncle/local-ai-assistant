package com.just.assistant.ai.prompt

import org.junit.Assert.assertTrue
import org.junit.Test

class ClassificationPromptTest {
    @Test
    fun build_includes_user_input() {
        val prompt = ClassificationPrompt.build("내일 3시 치과")
        assertTrue("user input must appear in prompt", prompt.contains("내일 3시 치과"))
    }

    @Test
    fun build_includes_json_schema_keys() {
        val prompt = ClassificationPrompt.build("test")
        listOf("MEMO", "EVENT", "REMINDER", "title", "body", "tags", "datetime_iso").forEach {
            assertTrue("schema key $it missing", prompt.contains(it))
        }
    }

    @Test
    fun build_trims_user_input_to_avoid_trailing_whitespace_in_prompt() {
        val prompt = ClassificationPrompt.build("  hello  \n\n")
        assertTrue(prompt.contains("hello"))
        assertTrue("trimmed extra trailing newlines should be gone after 'hello'", !prompt.contains("hello  \n\n"))
    }
}
