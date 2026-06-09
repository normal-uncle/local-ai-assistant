package com.just.feature.settings

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.just.feature.settings.settings.AboutScreen
import com.just.feature.settings.settings.SettingsScreen

@Composable
fun SettingsScene(onBack: () -> Unit, appVersion: String) {
    val backStack = rememberNavBackStack(SettingsSubRoute.List)
    NavDisplay(
        backStack = backStack,
        entryProvider =
            entryProvider {
                entry<SettingsSubRoute.List> {
                    SettingsScreen(
                        onBack = onBack,
                        onOpenAbout = { backStack.add(SettingsSubRoute.About) },
                    )
                }
                entry<SettingsSubRoute.About> {
                    AboutScreen(
                        appVersion = appVersion,
                        onBack = { backStack.removeLastOrNull() },
                    )
                }
            },
    )
}
