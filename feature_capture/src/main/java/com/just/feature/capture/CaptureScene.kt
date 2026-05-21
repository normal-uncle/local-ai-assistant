package com.just.feature.capture

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureScene(
    onOpenMemoList: () -> Unit,
    viewModel: CaptureViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새 메모") },
                actions = {
                    Button(onClick = onOpenMemoList) { Text("목록") }
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
                    label = { Text("내용") },
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .testTag("capture_input"),
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = viewModel::onPrepare,
                    enabled = state.input.isNotBlank() && !state.isSaving,
                    modifier = Modifier.testTag("capture_prepare"),
                ) { Text("저장 미리보기") }
            }
        }

        if (state.preview != null) {
            ModalBottomSheet(onDismissRequest = viewModel::onCancel) {
                PreviewSheet(
                    preview = state.preview!!,
                    onCancel = viewModel::onCancel,
                    onConfirm = viewModel::onConfirm,
                )
            }
        }
    }
}
