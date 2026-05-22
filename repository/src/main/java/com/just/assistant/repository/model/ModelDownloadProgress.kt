package com.just.assistant.repository.model

data class ModelDownloadProgress(
    val variantId: String,
    val bytesWritten: Long,
    val totalBytes: Long?,
) {
    val ratio: Float? get() = totalBytes?.takeIf { it > 0 }?.let { bytesWritten.toFloat() / it }
}
