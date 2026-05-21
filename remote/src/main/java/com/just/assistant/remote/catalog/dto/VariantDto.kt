package com.just.assistant.remote.catalog.dto

import kotlinx.serialization.Serializable

@Serializable
data class VariantDto(
    val id: String,
    val url: String,
    val sha256: String,
    val sizeMb: Long,
    val minRamGb: Int,
    val recommended: Boolean = false,
)
