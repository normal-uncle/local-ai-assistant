package com.just.feature.memo

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

internal sealed interface MemoSubRoute : NavKey {
    @Serializable
    data object List : MemoSubRoute

    @Serializable
    data class Detail(val noteId: Long) : MemoSubRoute
}
