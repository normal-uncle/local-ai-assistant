package com.just.feature.capture.capture

import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.just.assistant.ui.component.AssistantScaffold
import com.just.assistant.ui.component.AssistantTopBar
import com.just.assistant.ui.component.PrimaryButton
import com.just.assistant.ui.component.theme.Radius
import com.just.assistant.ui.component.theme.Spacing
import com.just.assistant.repository.model.NoteType
import com.just.feature.capture.PreviewSheet
import com.just.feature.capture.R
import com.just.feature.capture.permission.SchedulePermissionState
import com.just.feature.capture.permission.rememberSchedulePermissionState
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CaptureScreen(
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

    val voiceUnavailable = stringResource(R.string.capture_voice_unavailable)
    val voicePrompt = stringResource(R.string.capture_voice_prompt)
    val speechLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val transcript =
                result.data
                    ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?.firstOrNull()
                    ?.trim()
            if (!transcript.isNullOrEmpty()) {
                val current = state.input.trim()
                viewModel.onInputChanged(if (current.isEmpty()) transcript else "$current $transcript")
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

    AssistantScaffold(
        topBar = {
            AssistantTopBar(stringResource(R.string.capture_top_title))
        },
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Column(
                Modifier
                    .padding(Spacing.lg)
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
                Spacer(Modifier.height(Spacing.md))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    IconButton(onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    }) {
                        Icon(
                            Icons.Filled.PhotoLibrary,
                            contentDescription = stringResource(R.string.capture_pick_gallery),
                        )
                    }
                    IconButton(onClick = {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                    }) {
                        Icon(
                            Icons.Filled.PhotoCamera,
                            contentDescription = stringResource(R.string.capture_take_photo),
                        )
                    }
                    IconButton(onClick = {
                        val intent =
                            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
                                )
                                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, java.util.Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, voicePrompt)
                            }
                        try {
                            speechLauncher.launch(intent)
                        } catch (e: android.content.ActivityNotFoundException) {
                            android.widget.Toast.makeText(context, voiceUnavailable, android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = stringResource(R.string.capture_voice_input),
                        )
                    }
                }
                state.pickedImage?.let { uri ->
                    Spacer(Modifier.height(Spacing.sm))
                    AsyncImage(
                        model = uri,
                        contentDescription = stringResource(R.string.capture_image_thumbnail_desc),
                        modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(Radius.lg)).testTag("capture_thumbnail"),
                    )
                }
                Spacer(Modifier.height(Spacing.md))
                PrimaryButton(
                    text = stringResource(R.string.capture_prepare),
                    onClick = {
                        if (state.pickedImage != null) viewModel.onPrepareImage(imageFallbackTitle) else viewModel.onPrepare()
                    },
                    modifier = Modifier.fillMaxWidth().testTag("capture_prepare"),
                    enabled = (state.input.isNotBlank() || state.pickedImage != null) && !state.isSaving && !state.isPreparing,
                )
                state.error?.let { msg ->
                    Spacer(Modifier.height(Spacing.sm))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.md))
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(Spacing.md),
                    ) {
                        Text(
                            text = msg.ifBlank { stringResource(R.string.capture_save_failed) },
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
                if (state.isPreparing) {
                    Spacer(Modifier.height(Spacing.md))
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    ) {
                        CircularProgressIndicator(modifier = Modifier.padding(end = Spacing.xs))
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
