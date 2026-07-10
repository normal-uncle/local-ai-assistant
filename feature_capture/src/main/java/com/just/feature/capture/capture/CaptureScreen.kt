package com.just.feature.capture.capture

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.just.assistant.repository.model.NoteType
import com.just.feature.capture.PreviewSheet
import com.just.feature.capture.R
import com.just.feature.capture.permission.SchedulePermissionState
import com.just.feature.capture.permission.rememberSchedulePermissionState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CaptureScreen(
    onOpenMemo: () -> Unit,
    onOpenChat: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val imageFallbackTitle = stringResource(R.string.capture_image_fallback_title)
    val cameraDeniedMessage = stringResource(R.string.capture_camera_denied_banner)
    var cameraOutputUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) viewModel.onImagePicked(uri)
        }
    val cameraLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) cameraOutputUri?.let(viewModel::onImagePicked)
        }
    val cameraPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                val file = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                cameraOutputUri = uri
                cameraLauncher.launch(uri)
            } else {
                android.widget.Toast.makeText(context, cameraDeniedMessage, android.widget.Toast.LENGTH_SHORT).show()
            }
        }

    val voiceFallbackTitle = stringResource(R.string.capture_voice_fallback_title)
    val recordPermissionDenied = stringResource(R.string.capture_record_permission_denied)
    val recordPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                viewModel.onToggleRecording(voiceFallbackTitle)
            } else {
                android.widget.Toast.makeText(context, recordPermissionDenied, android.widget.Toast.LENGTH_SHORT).show()
            }
        }

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
                    Button(onClick = onOpenChat) {
                        Text(stringResource(R.string.capture_top_open_chat))
                    }
                    Button(onClick = onOpenMemo) {
                        Text(stringResource(R.string.capture_top_open_list))
                    }
                    Button(onClick = onOpenSettings) {
                        Text(stringResource(R.string.capture_top_open_settings))
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
                            .testTag("capture_input"),
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    }) { Text(stringResource(R.string.capture_pick_gallery)) }
                    Button(onClick = {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }) { Text(stringResource(R.string.capture_take_photo)) }
                    Button(
                        onClick = {
                            if (state.isRecording) {
                                viewModel.onToggleRecording(voiceFallbackTitle)
                            } else {
                                recordPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.testTag("capture_record"),
                    ) {
                        Text(
                            if (state.isRecording) {
                                stringResource(R.string.capture_record_stop, state.recordingSeconds)
                            } else {
                                stringResource(R.string.capture_record_start)
                            },
                        )
                    }
                }
                state.pickedImage?.let { uri ->
                    Spacer(Modifier.height(8.dp))
                    AsyncImage(
                        model = uri,
                        contentDescription = stringResource(R.string.capture_image_thumbnail_desc),
                        modifier = Modifier.fillMaxWidth().height(160.dp).testTag("capture_thumbnail"),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (state.pickedImage != null) viewModel.onPrepareImage(imageFallbackTitle) else viewModel.onPrepare()
                    },
                    enabled = (state.input.isNotBlank() || state.pickedImage != null) && !state.isSaving && !state.isPreparing,
                    modifier = Modifier.testTag("capture_prepare"),
                ) { Text(stringResource(R.string.capture_prepare)) }
                state.error?.let { msg ->
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = msg.ifBlank { stringResource(R.string.capture_save_failed) },
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (state.isPreparing) {
                    Spacer(Modifier.height(12.dp))
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = 4.dp))
                        Text(
                            text = stringResource(R.string.capture_preparing),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
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
