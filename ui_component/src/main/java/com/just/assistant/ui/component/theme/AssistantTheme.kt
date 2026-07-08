package com.just.assistant.ui.component.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

private val LightColors = lightColorScheme(
    primary = ColorSet.L_PRIMARY,
    onPrimary = ColorSet.L_ON_PRIMARY,
    primaryContainer = ColorSet.L_PRIMARY_CONTAINER,
    onPrimaryContainer = ColorSet.L_ON_PRIMARY_CONTAINER,
    background = ColorSet.L_BACKGROUND,
    onBackground = ColorSet.L_ON_SURFACE,
    surface = ColorSet.L_SURFACE,
    onSurface = ColorSet.L_ON_SURFACE,
    surfaceVariant = ColorSet.L_SURFACE_VARIANT,
    onSurfaceVariant = ColorSet.L_ON_SURFACE_VARIANT,
    outline = ColorSet.L_OUTLINE,
    error = ColorSet.L_ERROR,
    onError = ColorSet.L_ON_ERROR,
    errorContainer = ColorSet.L_ERROR_CONTAINER,
    onErrorContainer = ColorSet.L_ON_ERROR_CONTAINER,
)

private val DarkColors = darkColorScheme(
    primary = ColorSet.D_PRIMARY,
    onPrimary = ColorSet.D_ON_PRIMARY,
    primaryContainer = ColorSet.D_PRIMARY_CONTAINER,
    onPrimaryContainer = ColorSet.D_ON_PRIMARY_CONTAINER,
    background = ColorSet.D_BACKGROUND,
    onBackground = ColorSet.D_ON_SURFACE,
    surface = ColorSet.D_SURFACE,
    onSurface = ColorSet.D_ON_SURFACE,
    surfaceVariant = ColorSet.D_SURFACE_VARIANT,
    onSurfaceVariant = ColorSet.D_ON_SURFACE_VARIANT,
    outline = ColorSet.D_OUTLINE,
    error = ColorSet.D_ERROR,
    onError = ColorSet.D_ON_ERROR,
    errorContainer = ColorSet.D_ERROR_CONTAINER,
    onErrorContainer = ColorSet.D_ON_ERROR_CONTAINER,
)

private val AppTypography = Typography(
    headlineSmall = TextSet.HeadlineSmall,
    titleLarge = TextSet.TitleLarge,
    titleMedium = TextSet.TitleMedium,
    titleSmall = TextSet.TitleSmall,
    bodyLarge = TextSet.BodyLarge,
    bodyMedium = TextSet.BodyMedium,
    labelLarge = TextSet.LabelLarge,
    labelSmall = TextSet.LabelSmall,
)

@Composable
fun AssistantTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val assistantColors = if (darkTheme) DarkAssistantColors else LightAssistantColors
    CompositionLocalProvider(LocalAssistantColors provides assistantColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            shapes = AssistantShapes,
            content = content,
        )
    }
}
