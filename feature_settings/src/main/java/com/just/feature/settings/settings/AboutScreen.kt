package com.just.feature.settings.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.just.assistant.ui.component.AssistantScaffold
import com.just.assistant.ui.component.AssistantTopBar
import com.just.assistant.ui.component.theme.Spacing
import com.just.feature.settings.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AboutScreen(
    appVersion: String,
    onBack: () -> Unit,
) {
    AssistantScaffold(
        topBar = {
            AssistantTopBar(title = stringResource(R.string.about_title), onBack = onBack)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(stringResource(R.string.about_version, appVersion), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.about_licenses_header), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(R.string.about_licenses_intro), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.about_licenses_list), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.about_apache_notice), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.about_model_header), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(R.string.about_model_body), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.about_model_terms_url), style = MaterialTheme.typography.bodySmall)
            Text(stringResource(R.string.about_privacy_header), style = MaterialTheme.typography.titleSmall)
            Text(stringResource(R.string.about_privacy_body), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
