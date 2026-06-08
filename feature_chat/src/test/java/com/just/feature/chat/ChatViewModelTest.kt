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
import org.junit.Before
import org.junit.Test

class ChatViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private class FakeSession(private val deltas: List<String>) : ChatSession {
        var closed = false
        var sendCount = 0
        override fun send(message: String): Flow<String> =
            flow { sendCount++; deltas.forEach { emit(it) } }
        override fun close() { closed = true }
    }

    private class FakeStart(private val session: ChatSession?) : StartChatSessionUseCase {
        var count = 0
        override suspend fun invoke(): ChatSession? { count++; return session }
    }

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun send_appends_streaming_deltas_to_assistant_message() =
        runTest {
            val session = FakeSession(listOf("안", "녕", "하세요"))
            val vm = ChatViewModel(FakeStart(session))
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
            val vm = ChatViewModel(start)
            vm.onInputChanged("hi"); vm.onSend(); advanceUntilIdle()
            vm.onInputChanged("again"); vm.onSend(); advanceUntilIdle()
            assertEquals(1, start.count)
            assertEquals(2, session.sendCount)
        }

    @Test
    fun model_not_ready_sets_flag() =
        runTest {
            val vm = ChatViewModel(FakeStart(null))
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
            val vm = ChatViewModel(FakeStart(failing))
            vm.onInputChanged("hi")
            vm.onSend()
            advanceUntilIdle()
            val st = vm.state.first()
            assertFalse(st.isStreaming)
            assertEquals(false, st.error.isNullOrEmpty())
        }
}
