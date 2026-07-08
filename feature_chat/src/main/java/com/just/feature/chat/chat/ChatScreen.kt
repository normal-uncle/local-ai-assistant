package com.just.feature.chat.chat

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.just.assistant.ui.component.AssistantScaffold
import com.just.assistant.ui.component.AssistantTopBar
import com.just.assistant.ui.component.ChatBubble
import com.just.assistant.ui.component.theme.Spacing
import com.just.feature.chat.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val voiceUnavailable = stringResource(R.string.chat_voice_unavailable)
    val voicePrompt = stringResource(R.string.chat_voice_prompt)
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
    AssistantScaffold(
        topBar = {
            AssistantTopBar(
                title = stringResource(R.string.chat_title),
                actions = {
                    TextButton(onClick = viewModel::onToggleTts) {
                        Text(
                            stringResource(
                                if (state.ttsEnabled) R.string.chat_tts_on else R.string.chat_tts_off,
                            ),
                        )
                    }
                    TextButton(onClick = viewModel::onResetConversation) {
                        Text(stringResource(R.string.chat_new))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(Spacing.md),
        ) {
            if (!state.modelReady) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.sm)
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(Spacing.md),
                        )
                        .padding(Spacing.md),
                ) {
                    Text(
                        text = stringResource(R.string.chat_model_not_ready),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
            if (state.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.sm)
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(Spacing.md),
                        )
                        .padding(Spacing.md),
                ) {
                    Text(
                        text = stringResource(R.string.chat_error),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                items(state.messages) { msg ->
                    ChatBubble(
                        text = msg.text,
                        isUser = msg.role == ChatMessage.Role.USER,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                OutlinedTextField(
                    value = state.input,
                    onValueChange = viewModel::onInputChanged,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.chat_input_hint)) },
                )
                if (!state.isStreaming) {
                    Button(onClick = {
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
                            Toast.makeText(context, voiceUnavailable, Toast.LENGTH_SHORT).show()
                        }
                    }) { Text(stringResource(R.string.chat_voice_input)) }
                }
                if (state.isStreaming) {
                    Button(onClick = viewModel::onCancel) {
                        Text(stringResource(R.string.chat_cancel))
                    }
                } else {
                    Button(
                        onClick = viewModel::onSend,
                        enabled = state.input.isNotBlank(),
                    ) {
                        Text(stringResource(R.string.chat_send))
                    }
                }
            }
        }
    }
}
