package com.just.feature.chat.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just.assistant.ai.inference.ChatSession
import com.just.assistant.usecase.chat.di.StartChatSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(val role: Role, val text: String) {
    enum class Role { USER, ASSISTANT }
}

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isStreaming: Boolean = false,
    val error: String? = null,
    val modelReady: Boolean = true,
)

@HiltViewModel
class ChatViewModel
    @Inject
    constructor(
        private val startChatSession: StartChatSessionUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(ChatState())
        val state: StateFlow<ChatState> get() = _state.asStateFlow()

        private var session: ChatSession? = null
        private var streamJob: Job? = null

        fun onInputChanged(text: String) {
            _state.update { it.copy(input = text) }
        }

        fun onSend() {
            val text = _state.value.input.trim()
            if (text.isEmpty() || _state.value.isStreaming) return
            viewModelScope.launch {
                val s = session ?: startChatSession()?.also { session = it }
                if (s == null) {
                    _state.update { it.copy(modelReady = false) }
                    return@launch
                }
                _state.update {
                    it.copy(
                        messages = it.messages +
                            ChatMessage(ChatMessage.Role.USER, text) +
                            ChatMessage(ChatMessage.Role.ASSISTANT, ""),
                        input = "",
                        isStreaming = true,
                        error = null,
                        modelReady = true,
                    )
                }
                streamJob =
                    launch {
                        try {
                            s.send(text).collect { delta ->
                                _state.update { st ->
                                    val msgs = st.messages.toMutableList()
                                    val last = msgs.lastIndex
                                    msgs[last] = msgs[last].copy(text = msgs[last].text + delta)
                                    st.copy(messages = msgs)
                                }
                            }
                        } catch (t: Throwable) {
                            session?.close()
                            session = null
                            // error를 non-null로 세팅(내용은 UI가 친절한 문자열로 대체) — 사용자에게 raw 예외 미노출.
                            _state.update { it.copy(error = t.message ?: "", isStreaming = false) }
                        } finally {
                            _state.update { it.copy(isStreaming = false) }
                        }
                    }
            }
        }

        fun onCancel() {
            streamJob?.cancel()
            _state.update { it.copy(isStreaming = false) }
        }

        fun onResetConversation() {
            streamJob?.cancel()
            session?.close()
            session = null
            _state.update { ChatState() }
        }

        override fun onCleared() {
            session?.close()
        }
    }
