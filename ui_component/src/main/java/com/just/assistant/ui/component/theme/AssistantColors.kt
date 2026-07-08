package com.just.assistant.ui.component.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AssistantColors(
    val success: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
)

val LightAssistantColors = AssistantColors(
    success = ColorSet.L_SUCCESS,
    successContainer = ColorSet.L_SUCCESS_CONTAINER,
    onSuccessContainer = ColorSet.L_ON_SUCCESS_CONTAINER,
)

val DarkAssistantColors = AssistantColors(
    success = ColorSet.D_SUCCESS,
    successContainer = ColorSet.D_SUCCESS_CONTAINER,
    onSuccessContainer = ColorSet.D_ON_SUCCESS_CONTAINER,
)

val LocalAssistantColors = staticCompositionLocalOf { LightAssistantColors }
