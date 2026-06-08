package com.just.feature.chat

import androidx.compose.runtime.Composable
import com.just.feature.chat.chat.ChatScreen

@Composable
fun ChatScene(onBack: () -> Unit) {
    ChatScreen(onBack = onBack)
}
