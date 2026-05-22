package com.just.assistant.repository.model

data class ModelVariant(
    val id: String,
    val url: String,
    val sha256: String,
    val sizeMb: Long,
    val minRamGb: Int,
    val recommended: Boolean,
)
