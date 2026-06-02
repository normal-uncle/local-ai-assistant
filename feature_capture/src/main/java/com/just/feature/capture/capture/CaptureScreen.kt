package com.just.feature.capture.capture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.just.assistant.repository.model.NoteType
import com.just.feature.capture.PreviewSheet
import com.just.feature.capture.R
import com.just.feature.capture.permission.SchedulePermissionState
import com.just.feature.capture.permission.rememberSchedulePermissionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CaptureScreen(
    onOpenMemo: () -> Unit,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val permissionState = rememberSchedulePermissionState()
    var pendingToggleType by remember { mutableStateOf<NoteType?>(null) }
    var scheduleToggleOn by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf<PermissionDialog?>(null) }

    LaunchedEffect(permissionState.lastResult, pendingToggleType) {
        val t = pendingToggleType ?: return@LaunchedEffect
        when (permissionState.lastResult) {
            SchedulePermissionState.Result.Granted -> {
                if (t == NoteType.REMINDER && !permissionState.canScheduleExactAlarms()) {
                    dialog = PermissionDialog.ExactAlarm
                    scheduleToggleOn = false
                } else {
                    scheduleToggleOn = true
                }
                pendingToggleType = null
                permissionState.lastResult = SchedulePermissionState.Result.Idle
            }
            SchedulePermissionState.Result.DeniedTransient -> {
                scheduleToggleOn = false
                pendingToggleType = null
                permissionState.lastResult = SchedulePermissionState.Result.Idle
            }
            SchedulePermissionState.Result.DeniedPermanent -> {
                dialog =
                    when (t) {
                        NoteType.EVENT -> PermissionDialog.CalendarPermanent
                        NoteType.REMINDER -> PermissionDialog.NotificationPermanent
                        NoteType.MEMO -> null
                    }
                scheduleToggleOn = false
                pendingToggleType = null
                permissionState.lastResult = SchedulePermissionState.Result.Idle
            }
            SchedulePermissionState.Result.Idle -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.capture_top_title)) },
                actions = {
                    Button(onClick = onOpenMemo) {
                        Text(stringResource(R.string.capture_top_open_list))
                    }
                },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Column(
                Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
            ) {
                OutlinedTextField(
                    value = state.input,
                    onValueChange = viewModel::onInputChanged,
                    label = { Text(stringResource(R.string.capture_input_label)) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("capture_input"),
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = viewModel::onPrepare,
                    enabled = state.input.isNotBlank() && !state.isSaving,
                    modifier = Modifier.testTag("capture_prepare"),
                ) { Text(stringResource(R.string.capture_prepare)) }
            }
        }

        if (state.preview != null) {
            ModalBottomSheet(onDismissRequest = viewModel::onCancel) {
                PreviewSheet(
                    preview = state.preview!!,
                    scheduleEnabledExternal = scheduleToggleOn,
                    onScheduleToggleRequest = { wantOn, type ->
                        if (!wantOn) {
                            scheduleToggleOn = false
                            return@PreviewSheet
                        }
                        pendingToggleType = type
                        when (type) {
                            NoteType.EVENT -> {
                                if (permissionState.hasCalendarPermission()) {
                                    scheduleToggleOn = true
                                    pendingToggleType = null
                                } else {
                                    permissionState.requestCalendar()
                                }
                            }
                            NoteType.REMINDER -> {
                                if (!permissionState.hasNotificationPermission()) {
                                    permissionState.requestNotification()
                                } else if (!permissionState.canScheduleExactAlarms()) {
                                    dialog = PermissionDialog.ExactAlarm
                                    scheduleToggleOn = false
                                    pendingToggleType = null
                                } else {
                                    scheduleToggleOn = true
                                    pendingToggleType = null
                                }
                            }
                            NoteType.MEMO -> {
                                scheduleToggleOn = false
                                pendingToggleType = null
                            }
                        }
                    },
                    onCancel = {
                        scheduleToggleOn = false
                        pendingToggleType = null
                        dialog = null
                        viewModel.onCancel()
                    },
                    onConfirm = { confirmed ->
                        scheduleToggleOn = false
                        pendingToggleType = null
                        dialog = null
                        viewModel.onConfirm(confirmed)
                    },
                )
            }
        }

        dialog?.let { d ->
            AlertDialog(
                onDismissRequest = { dialog = null },
                title = { Text(stringResource(d.titleRes)) },
                text = { Text(stringResource(d.bodyRes)) },
                confirmButton = {
                    TextButton(onClick = {
                        when (d) {
                            PermissionDialog.ExactAlarm -> permissionState.openExactAlarmSettings()
                            PermissionDialog.CalendarPermanent,
                            PermissionDialog.NotificationPermanent,
                            -> permissionState.openAppSettings()
                        }
                        dialog = null
                    }) { Text(stringResource(R.string.capture_permission_settings_button)) }
                },
                dismissButton = {
                    TextButton(onClick = { dialog = null }) {
                        Text(stringResource(R.string.capture_permission_cancel))
                    }
                },
            )
        }
    }
}

private sealed interface PermissionDialog {
    val titleRes: Int
    val bodyRes: Int

    data object CalendarPermanent : PermissionDialog {
        override val titleRes = R.string.capture_permission_calendar_title
        override val bodyRes = R.string.capture_permission_calendar_body
    }

    data object NotificationPermanent : PermissionDialog {
        override val titleRes = R.string.capture_permission_notification_title
        override val bodyRes = R.string.capture_permission_notification_body
    }

    data object ExactAlarm : PermissionDialog {
        override val titleRes = R.string.capture_permission_exact_alarm_title
        override val bodyRes = R.string.capture_permission_exact_alarm_body
    }
}
