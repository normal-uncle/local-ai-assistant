package com.just.feature.onboarding.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.just.assistant.repository.model.ModelDownloadProgress
import com.just.assistant.repository.model.ModelStatus
import com.just.feature.onboarding.OnboardingState
import com.just.feature.onboarding.R

@Composable
internal fun OnboardingScreen(
    state: OnboardingState,
    onStart: () -> Unit,
    onDone: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.onboarding_subtitle), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(32.dp))

            when (state.status) {
                ModelStatus.NOT_READY -> {
                    Button(onClick = onStart) {
                        Text(stringResource(R.string.onboarding_start))
                    }
                }
                ModelStatus.DOWNLOADING -> {
                    ProgressBlock(state.progress)
                }
                ModelStatus.READY -> {
                    Text(stringResource(R.string.onboarding_done))
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onDone) { Text(stringResource(R.string.onboarding_continue)) }
                }
                ModelStatus.FAILED -> {
                    Text(stringResource(R.string.onboarding_error, "다운로드 실패"))
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onStart) { Text(stringResource(R.string.onboarding_retry)) }
                }
            }
        }
    }
}

@Composable
private fun ProgressBlock(progress: ModelDownloadProgress?) {
    val ratio = progress?.ratio ?: 0f
    val percent = (ratio * 100).toInt()
    val totalBytes = progress?.totalBytes
    val sizeText =
        if (totalBytes != null) {
            "${progress.bytesWritten / 1_048_576} / ${totalBytes / 1_048_576} MiB"
        } else {
            "${(progress?.bytesWritten ?: 0L) / 1_048_576} MiB"
        }
    LinearProgressIndicator(
        progress = { ratio },
        modifier = Modifier.fillMaxWidth().height(8.dp),
    )
    Spacer(Modifier.height(12.dp))
    Text(stringResource(R.string.onboarding_progress_format, percent, sizeText))
}
