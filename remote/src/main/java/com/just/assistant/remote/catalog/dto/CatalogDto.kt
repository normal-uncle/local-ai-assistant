package com.just.assistant.remote.catalog.dto

import kotlinx.serialization.Serializable

@Serializable
data class CatalogDto(
    val version: String,
    val variants: List<VariantDto>,
)
