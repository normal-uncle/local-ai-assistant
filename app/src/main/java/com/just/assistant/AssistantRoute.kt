package com.just.assistant

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AssistantRoute : NavKey {
    @Serializable
    data object Capture : AssistantRoute

    @Serializable
    data object Memo : AssistantRoute
}
