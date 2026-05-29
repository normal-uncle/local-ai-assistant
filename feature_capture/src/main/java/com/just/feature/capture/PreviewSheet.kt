package com.just.feature.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.just.assistant.repository.model.NoteType
import com.just.feature.capture.capture.CapturePreview
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun PreviewSheet(
    preview: CapturePreview,
    onConfirm: (CapturePreviewConfirmed) -> Unit,
    onCancel: () -> Unit,
) {
    var title by remember(preview) { mutableStateOf(preview.title) }
    var body by remember(preview) { mutableStateOf(preview.body) }
    var type by remember(preview) { mutableStateOf(preview.type) }
    var datetime by remember(preview) {
        mutableStateOf(preview.datetime ?: defaultDatetimeFor())
    }
    var scheduleEnabled by remember(preview) {
        mutableStateOf(type == NoteType.EVENT || type == NoteType.REMINDER)
    }
    var showDatePicker by remember { mutableStateOf(false) }

    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
    val zoneId = remember { ZoneId.systemDefault() }

    Column(
        modifier =
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.capture_preview_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NoteType.entries.forEach { t ->
                FilterChip(
                    selected = type == t,
                    onClick = {
                        type = t
                        if (t == NoteType.MEMO) scheduleEnabled = false
                    },
                    label = { Text(stringResource(t.labelRes())) },
                )
            }
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

        if (type == NoteType.EVENT || type == NoteType.REMINDER) {
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val toggleLabel =
                    if (type == NoteType.EVENT) {
                        stringResource(R.string.capture_schedule_to_calendar)
                    } else {
                        stringResource(R.string.capture_schedule_alarm)
                    }
                Text(toggleLabel, style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = scheduleEnabled,
                    onCheckedChange = { scheduleEnabled = it },
                    modifier = Modifier.testTag("capture_schedule_toggle"),
                )
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.capture_datetime_edit),
                    style = MaterialTheme.typography.bodyMedium,
                )
                TextButton(onClick = { showDatePicker = true }) {
                    val localDt = LocalDateTime.ofInstant(datetime, zoneId)
                    Text(formatter.format(localDt))
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.capture_cancel))
            }
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    onConfirm(
                        CapturePreviewConfirmed(
                            title = title,
                            body = body,
                            type = type,
                            tags = preview.tags,
                            datetime = if (scheduleEnabled) datetime else null,
                            scheduleEnabled = scheduleEnabled,
                        ),
                    )
                },
                modifier = Modifier.testTag("capture_save"),
            ) {
                Text(stringResource(R.string.capture_save))
            }
        }
    }

    if (showDatePicker) {
        DateTimePickerSheet(
            initial = datetime,
            onConfirm = {
                datetime = it
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false },
        )
    }
}

data class CapturePreviewConfirmed(
    val title: String,
    val body: String,
    val type: NoteType,
    val tags: List<String>,
    val datetime: Instant?,
    val scheduleEnabled: Boolean,
)

private fun defaultDatetimeFor(): Instant = Instant.now().plusSeconds(3_600)

private fun NoteType.labelRes(): Int =
    when (this) {
        NoteType.MEMO -> R.string.note_type_memo
        NoteType.EVENT -> R.string.note_type_event
        NoteType.REMINDER -> R.string.note_type_reminder
    }
