package com.just.assistant.ui.component.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = ColorSet.PRIMARY_500,
    onPrimary = ColorSet.WHITE_100,
    primaryContainer = ColorSet.PRIMARY_100,
    surface = ColorSet.WHITE_100,
    onSurface = ColorSet.GRAY_900,
    surfaceVariant = ColorSet.NEUTRAL_50,
    onSurfaceVariant = ColorSet.NEUTRAL_500,
    outline = ColorSet.NEUTRAL_300,
    error = ColorSet.ERROR_500,
    onError = ColorSet.WHITE_100,
)

private val AppTypography = Typography(
    titleLarge = TextSet.TitleLarge,
    titleMedium = TextSet.TitleMedium,
    bodyLarge = TextSet.BodyLarge,
    bodyMedium = TextSet.BodyMedium,
    labelLarge = TextSet.LabelLarge,
)

@Composable
fun AssistantTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content,
    )
}
