package com.just.feature.onboarding.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.just.assistant.repository.model.ModelDownloadProgress
import com.just.assistant.repository.model.ModelStatus
import com.just.assistant.ui.component.AssistantScaffold
import com.just.assistant.ui.component.PrimaryButton
import com.just.assistant.ui.component.theme.Radius
import com.just.assistant.ui.component.theme.Spacing
import com.just.feature.onboarding.OnboardingState
import com.just.feature.onboarding.R

@Composable
internal fun OnboardingScreen(
    state: OnboardingState,
    onStart: () -> Unit,
    onDone: () -> Unit,
) {
    AssistantScaffold { padding ->
        Column(
            modifier =
                Modifier
                    .padding(padding)
                    .padding(Spacing.lg)
                    .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Filled.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(44.dp)) }
            Spacer(Modifier.height(Spacing.xl))
            Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(Spacing.md))
            Text(stringResource(R.string.onboarding_subtitle), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(Spacing.md))
            state.variantId?.let { vid ->
                Text(
                    text = stringResource(R.string.onboarding_variant_format, vid),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(Spacing.sm))
            }
            Spacer(Modifier.height(Spacing.xxl))

            when (state.status) {
                ModelStatus.NOT_READY -> {
                    PrimaryButton(stringResource(R.string.onboarding_start), onStart)
                }
                ModelStatus.DOWNLOADING -> {
                    ProgressBlock(state.progress)
                }
                ModelStatus.READY -> {
                    Text(stringResource(R.string.onboarding_done))
                    Spacer(Modifier.height(Spacing.md))
                    PrimaryButton(stringResource(R.string.onboarding_continue), onDone)
                }
                ModelStatus.FAILED -> {
                    Icon(Icons.Filled.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                    Text(
                        stringResource(R.string.onboarding_error, stringResource(R.string.onboarding_error_generic)),
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(Spacing.md))
                    PrimaryButton(stringResource(R.string.onboarding_retry), onStart)
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
        strokeCap = StrokeCap.Round,
        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(Radius.sm)),
    )
    Spacer(Modifier.height(Spacing.md))
    Text(stringResource(R.string.onboarding_progress_format, percent, sizeText))
}
