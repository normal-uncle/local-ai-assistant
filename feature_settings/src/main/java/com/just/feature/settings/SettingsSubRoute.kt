package com.just.feature.settings

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

internal sealed interface SettingsSubRoute : NavKey {
    @Serializable
    data object List : SettingsSubRoute

    @Serializable
    data object About : SettingsSubRoute
}
