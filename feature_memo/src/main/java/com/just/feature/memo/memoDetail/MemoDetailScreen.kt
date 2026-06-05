package com.just.feature.memo.memoDetail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.just.feature.memo.DateTimePickerSheet
import com.just.feature.memo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MemoDetailScreen(
    noteId: Long,
    onBack: () -> Unit,
    viewModel: MemoDetailViewModel =
        hiltViewModel<MemoDetailViewModel, MemoDetailViewModel.Factory>(
            key = "memo-detail-$noteId",
        ) { factory -> factory.create(noteId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showRescheduleSheet by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.memo_detail_title)) }) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
        ) {
            when (val s = state) {
                MemoDetailState.Loading -> Text(stringResource(R.string.memo_detail_loading))
                MemoDetailState.NotFound -> Text(stringResource(R.string.memo_detail_not_found))
                is MemoDetailState.Loaded -> {
                    val note = s.note
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = note.title,
                            style = MaterialTheme.typography.titleLarge,
                            textDecoration = if (note.isCompleted) TextDecoration.LineThrough else null,
                        )
                        if (note.isCompleted) {
                            Spacer(Modifier.padding(start = 8.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.memo_detail_completed),
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    note.imageUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = stringResource(R.string.memo_detail_image_desc),
                            modifier = Modifier.fillMaxWidth().height(220.dp),
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                    Text(
                        text = note.body,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (note.isCompleted) TextDecoration.LineThrough else null,
                    )

                    val hasCalendar = note.calendarEventId != null
                    val hasAlarm = note.alarmRequestId != null
                    if (!note.isCompleted && (hasCalendar || hasAlarm)) {
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (hasCalendar) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(stringResource(R.string.memo_detail_chip_calendar)) },
                                    colors = AssistChipDefaults.assistChipColors(),
                                )
                            }
                            if (hasAlarm) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text(stringResource(R.string.memo_detail_chip_alarm)) },
                                    colors = AssistChipDefaults.assistChipColors(),
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { showRescheduleSheet = true }) {
                                Text(stringResource(R.string.memo_detail_reschedule))
                            }
                            Button(
                                onClick = {
                                    viewModel.onUnschedule()
                                    Toast.makeText(
                                        context,
                                        R.string.memo_detail_unschedule_done,
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                },
                            ) {
                                Text(stringResource(R.string.memo_detail_unschedule))
                            }
                        }
                    }

                    if (showRescheduleSheet && note.datetime != null) {
                        DateTimePickerSheet(
                            initial = note.datetime!!,
                            onConfirm = { newDateTime ->
                                viewModel.onReschedule(newDateTime)
                                showRescheduleSheet = false
                                Toast.makeText(
                                    context,
                                    R.string.memo_detail_reschedule_done,
                                    Toast.LENGTH_SHORT,
                                ).show()
                            },
                            onDismiss = { showRescheduleSheet = false },
                        )
                    }
                }
            }
        }
    }
}
