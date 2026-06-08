package com.just.feature.chat

import com.just.assistant.ai.inference.ChatSession
import com.just.assistant.usecase.chat.di.StartChatSessionUseCase
import com.just.feature.chat.chat.ChatMessage
import com.just.feature.chat.chat.ChatViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChatViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private class FakeSession(private val deltas: List<String>) : ChatSession {
        var closed = false
        var sendCount = 0
        var lastSent: String? = null
        override fun send(message: String): Flow<String> =
            flow { sendCount++; lastSent = message; deltas.forEach { emit(it) } }
        override fun close() { closed = true }
    }

    private class FakeRetriever(private val context: String = "") :
        com.just.assistant.usecase.chat.di.RetrieveNoteContextUseCase {
        var lastQuery: String? = null
        override suspend fun invoke(query: String): String { lastQuery = query; return context }
    }

    private class FakeStart(private val session: ChatSession?) : StartChatSessionUseCase {
        var count = 0
        override suspend fun invoke(): ChatSession? { count++; return session }
    }

    private class FakeTts : com.just.assistant.local.audio.TtsSpeaker {
        var spokenText: String? = null
        var speakCount = 0
        var stopCount = 0
        override fun speak(text: String) { speakCount++; spokenText = text }
        override fun stop() { stopCount++ }
    }

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun send_appends_streaming_deltas_to_assistant_message() =
        runTest {
            val session = FakeSession(listOf("안", "녕", "하세요"))
            val vm = ChatViewModel(FakeStart(session), FakeTts(), FakeRetriever())
            vm.onInputChanged("안녕")
            vm.onSend()
            advanceUntilIdle()
            val msgs = vm.state.first().messages
            assertEquals(2, msgs.size)
            assertEquals(ChatMessage.Role.USER, msgs[0].role)
            assertEquals("안녕", msgs[0].text)
            assertEquals(ChatMessage.Role.ASSISTANT, msgs[1].role)
            assertEquals("안녕하세요", msgs[1].text)
            assertFalse(vm.state.first().isStreaming)
        }

    @Test
    fun second_send_reuses_same_session() =
        runTest {
            val session = FakeSession(listOf("a"))
            val start = FakeStart(session)
            val vm = ChatViewModel(start, FakeTts(), FakeRetriever())
            vm.onInputChanged("hi"); vm.onSend(); advanceUntilIdle()
            vm.onInputChanged("again"); vm.onSend(); advanceUntilIdle()
            assertEquals(1, start.count)
            assertEquals(2, session.sendCount)
        }

    @Test
    fun model_not_ready_sets_flag() =
        runTest {
            val vm = ChatViewModel(FakeStart(null), FakeTts(), FakeRetriever())
            vm.onInputChanged("hi")
            vm.onSend()
            advanceUntilIdle()
            assertFalse(vm.state.first().modelReady)
        }

    @Test
    fun send_error_sets_error_and_stops_streaming() =
        runTest {
            val failing =
                object : ChatSession {
                    override fun send(message: String): Flow<String> = flow { throw RuntimeException("4096") }
                    override fun close() {}
                }
            val vm = ChatViewModel(FakeStart(failing), FakeTts(), FakeRetriever())
            vm.onInputChanged("hi")
            vm.onSend()
            advanceUntilIdle()
            val st = vm.state.first()
            assertFalse(st.isStreaming)
            assertEquals(false, st.error.isNullOrEmpty())
        }

    @Test
    fun speaks_final_text_when_tts_enabled_and_stream_completes() =
        runTest {
            val session = FakeSession(listOf("안", "녕"))
            val tts = FakeTts()
            val vm = ChatViewModel(FakeStart(session), tts, FakeRetriever())
            vm.onToggleTts()
            vm.onInputChanged("hi")
            vm.onSend()
            advanceUntilIdle()
            assertEquals(1, tts.speakCount)
            assertEquals("안녕", tts.spokenText)
        }

    @Test
    fun does_not_speak_when_tts_disabled() =
        runTest {
            val tts = FakeTts()
            val vm = ChatViewModel(FakeStart(FakeSession(listOf("a"))), tts, FakeRetriever())
            vm.onInputChanged("hi")
            vm.onSend()
            advanceUntilIdle()
            assertEquals(0, tts.speakCount)
        }

    @Test
    fun onSend_stops_previous_speech() =
        runTest {
            val tts = FakeTts()
            val vm = ChatViewModel(FakeStart(FakeSession(listOf("a"))), tts, FakeRetriever())
            vm.onInputChanged("hi")
            vm.onSend()
            advanceUntilIdle()
            assertEquals(true, tts.stopCount >= 1)
        }

    @Test
    fun toggle_off_stops_speech() =
        runTest {
            val tts = FakeTts()
            val vm = ChatViewModel(FakeStart(FakeSession(listOf("a"))), tts, FakeRetriever())
            vm.onToggleTts()
            vm.onToggleTts()
            assertEquals(true, tts.stopCount >= 1)
            assertEquals(false, vm.state.first().ttsEnabled)
        }

    @Test
    fun injects_context_into_sent_message_but_shows_original_in_bubble() =
        runTest {
            val session = FakeSession(listOf("ok"))
            val vm = ChatViewModel(FakeStart(session), FakeTts(), FakeRetriever("- [일정] 치과 (2026-06-09 15:00)"))
            vm.onInputChanged("내일 일정 뭐 있어?")
            vm.onSend()
            advanceUntilIdle()
            assertTrue(session.lastSent!!.contains("치과"))
            assertTrue(session.lastSent!!.contains("내일 일정 뭐 있어?"))
            val userMsg = vm.state.first().messages.first { it.role == ChatMessage.Role.USER }
            assertEquals("내일 일정 뭐 있어?", userMsg.text)
        }

    @Test
    fun sends_original_when_no_context() =
        runTest {
            val session = FakeSession(listOf("ok"))
            val vm = ChatViewModel(FakeStart(session), FakeTts(), FakeRetriever(""))
            vm.onInputChanged("안녕")
            vm.onSend()
            advanceUntilIdle()
            assertEquals("안녕", session.lastSent)
        }
}
