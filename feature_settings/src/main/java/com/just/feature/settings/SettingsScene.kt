package com.just.feature.settings

import androidx.compose.runtime.Composable
import com.just.feature.settings.settings.SettingsScreen

@Composable
fun SettingsScene(onBack: () -> Unit) {
    SettingsScreen(onBack = onBack)
}
