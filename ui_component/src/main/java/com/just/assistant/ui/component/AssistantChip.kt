package com.just.assistant.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.just.assistant.ui.component.theme.LocalAssistantColors

enum class ChipTone { Primary, Success }

@Composable
fun AssistantChip(
    text: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    tone: ChipTone = ChipTone.Primary,
) {
    val container: Color
    val content: Color
    when (tone) {
        ChipTone.Primary -> {
            container = MaterialTheme.colorScheme.primaryContainer
            content = MaterialTheme.colorScheme.onPrimaryContainer
        }
        ChipTone.Success -> {
            container = LocalAssistantColors.current.successContainer
            content = LocalAssistantColors.current.onSuccessContainer
        }
    }
    Row(
        modifier = modifier
            .background(container, CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = content, modifier = Modifier.size(16.dp))
        }
        Text(text, color = content, style = MaterialTheme.typography.labelSmall)
    }
}
