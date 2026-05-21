package com.just.feature.capture

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.just.feature.capture.capture.CapturePreview

@Composable
fun PreviewSheet(
    preview: CapturePreview,
    onConfirm: (CapturePreview) -> Unit,
    onCancel: () -> Unit,
) {
    var title by remember(preview) { mutableStateOf(preview.title) }
    var body by remember(preview) { mutableStateOf(preview.body) }
    val type = preview.type

    Column(
        Modifier
            .padding(16.dp)
            .fillMaxWidth(),
    ) {
        Text(stringResource(R.string.capture_preview_title), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(12.dp))
        Row {
            // TODO(P2): NoteType enum 이름 직접 노출 — AI 분류 도입 시 localized label로 교체
            AssistChip(onClick = {}, label = { Text(type.name) })
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.capture_field_title)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text(stringResource(R.string.capture_field_body)) },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Row {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.capture_cancel)) }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = { onConfirm(preview.copy(title = title, body = body)) },
                modifier = Modifier.testTag("capture_save"),
            ) {
                Text(stringResource(R.string.capture_save))
            }
        }
    }
}
