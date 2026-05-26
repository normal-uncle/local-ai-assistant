package com.just.assistant.ai.golden

import com.just.assistant.repository.model.NoteType
import kotlinx.serialization.Serializable

@Serializable
data class ClassificationGoldenCase(
    val id: String,
    val input: String,
    val expectedType: NoteType,
    val expectedHasDatetime: Boolean,
)
