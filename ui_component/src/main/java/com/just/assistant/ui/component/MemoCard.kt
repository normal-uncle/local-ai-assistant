package com.just.assistant.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.just.assistant.ui.component.theme.Radius
import com.just.assistant.ui.component.theme.Spacing

@Composable
fun MemoCard(
    title: String,
    body: String,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
    scheduleChip: (@Composable () -> Unit)? = null,
    thumbnail: (@Composable (Modifier) -> Unit)? = null,
    onClick: () -> Unit,
) {
    val strike = if (isCompleted) TextDecoration.LineThrough else null
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(Spacing.lg)) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textDecoration = strike,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (body.isNotBlank()) {
                    Text(
                        body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textDecoration = strike,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = Spacing.xs),
                    )
                }
                if (scheduleChip != null) {
                    Column(Modifier.padding(top = Spacing.sm)) { scheduleChip() }
                }
            }
            if (thumbnail != null) {
                thumbnail(
                    Modifier
                        .padding(start = Spacing.md)
                        .size(56.dp)
                        .clip(RoundedCornerShape(Radius.md)),
                )
            }
        }
    }
}
