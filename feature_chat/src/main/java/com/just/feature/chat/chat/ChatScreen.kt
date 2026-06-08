package com.just.feature.chat.chat

import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.chat_title)) },
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
                .padding(12.dp),
        ) {
            if (!state.modelReady) {
                Text(
                    text = stringResource(R.string.chat_model_not_ready),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                )
            }
            if (state.error != null) {
                Text(
                    text = stringResource(R.string.chat_error),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.messages) { msg ->
                    val isUser = msg.role == ChatMessage.Role.USER
                    Text(
                        text = msg.text,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = if (isUser) TextAlign.End else TextAlign.Start,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
