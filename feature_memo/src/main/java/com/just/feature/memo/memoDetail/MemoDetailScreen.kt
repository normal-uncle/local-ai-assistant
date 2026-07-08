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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.just.assistant.ui.component.AssistantChip
import com.just.assistant.ui.component.AssistantScaffold
import com.just.assistant.ui.component.AssistantTopBar
import com.just.assistant.ui.component.ChipTone
import com.just.assistant.ui.component.PrimaryButton
import com.just.assistant.ui.component.SecondaryButton
import com.just.assistant.ui.component.SectionCard
import com.just.assistant.ui.component.theme.Radius
import com.just.assistant.ui.component.theme.Spacing
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

    AssistantScaffold(
        topBar = {
            AssistantTopBar(title = stringResource(R.string.memo_detail_title), onBack = onBack)
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Spacing.lg),
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
                            Spacer(Modifier.padding(start = Spacing.sm))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.memo_detail_completed),
                            )
                        }
                    }
                    Spacer(Modifier.height(Spacing.sm))
                    note.imageUri?.let { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = stringResource(R.string.memo_detail_image_desc),
                            modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(Radius.lg)),
                        )
                        Spacer(Modifier.height(Spacing.sm))
                    }
                    Text(
                        text = note.body,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (note.isCompleted) TextDecoration.LineThrough else null,
                    )

                    val hasCalendar = note.calendarEventId != null
                    val hasAlarm = note.alarmRequestId != null
                    if (!note.isCompleted && (hasCalendar || hasAlarm)) {
                        Spacer(Modifier.height(Spacing.lg))
                        SectionCard {
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                if (hasCalendar) AssistantChip(stringResource(R.string.memo_detail_chip_calendar), leadingIcon = Icons.Filled.Event, tone = ChipTone.Success)
                                if (hasAlarm) AssistantChip(stringResource(R.string.memo_detail_chip_alarm), leadingIcon = Icons.Filled.Notifications, tone = ChipTone.Primary)
                            }
                            Spacer(Modifier.height(Spacing.md))
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                SecondaryButton(
                                    text = stringResource(R.string.memo_detail_reschedule),
                                    onClick = { showRescheduleSheet = true },
                                )
                                PrimaryButton(
                                    text = stringResource(R.string.memo_detail_unschedule),
                                    onClick = {
                                        viewModel.onUnschedule()
                                        Toast.makeText(
                                            context,
                                            R.string.memo_detail_unschedule_done,
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    },
                                )
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
