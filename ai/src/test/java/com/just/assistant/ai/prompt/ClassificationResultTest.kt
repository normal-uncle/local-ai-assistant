package com.just.assistant.ai.prompt

import com.just.assistant.repository.model.NoteType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ClassificationResultTest {
    @Test
    fun parses_clean_json_object() {
        val raw = """{"type":"MEMO","title":"장보기","body":"우유","tags":["home"]}"""
        val r = ClassificationResult.parse(raw)
        assertNotNull(r)
        assertEquals(NoteType.MEMO, r!!.type)
        assertEquals("장보기", r.title)
        assertEquals("우유", r.body)
        assertEquals(listOf("home"), r.tags)
        assertNull(r.datetimeIso)
    }

    @Test
    fun parses_json_wrapped_in_code_fence() {
        val raw =
            "여기 결과입니다:\n" +
                "```json\n" +
                """{"type":"event","title":"치과","datetime_iso":"2026-05-23T15:00:00","body":""}""" + "\n" +
                "```"
        val r = ClassificationResult.parse(raw)
        assertNotNull(r)
        assertEquals(NoteType.EVENT, r!!.type)
        assertEquals("치과", r.title)
        assertEquals("2026-05-23T15:00:00", r.datetimeIso)
    }

    @Test
    fun returns_null_when_no_json_object_present() {
        assertNull(ClassificationResult.parse("그냥 일반 텍스트입니다"))
        assertNull(ClassificationResult.parse(""))
    }

    @Test
    fun unknown_type_falls_back_to_MEMO() {
        val raw = """{"type":"WEIRD","title":"t","body":"b"}"""
        val r = ClassificationResult.parse(raw)
        assertNotNull(r)
        assertEquals(NoteType.MEMO, r!!.type)
    }

    @Test
    fun missing_optional_fields_use_defaults() {
        val raw = """{"type":"reminder","title":"콜백"}"""
        val r = ClassificationResult.parse(raw)
        assertNotNull(r)
        assertEquals(NoteType.REMINDER, r!!.type)
        assertEquals("", r.body)
        assertEquals(emptyList<String>(), r.tags)
        assertNull(r.datetimeIso)
    }
}
