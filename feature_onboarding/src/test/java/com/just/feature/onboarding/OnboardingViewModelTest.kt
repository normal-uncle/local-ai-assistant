package com.just.feature.onboarding

import com.just.assistant.repository.model.ModelDownloadProgress
import com.just.assistant.repository.model.ModelStatus
import com.just.assistant.usecase.model.di.EnsureModelDownloadedUseCase
import com.just.assistant.usecase.model.di.ObserveModelDownloadProgressUseCase
import com.just.assistant.usecase.model.di.ObserveModelStatusUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OnboardingViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    private class FakeObserveStatus : ObserveModelStatusUseCase {
        val flow = MutableStateFlow(ModelStatus.NOT_READY)

        override fun invoke(): Flow<ModelStatus> = flow
    }

    private class FakeObserveProgress : ObserveModelDownloadProgressUseCase {
        val flow = MutableStateFlow<ModelDownloadProgress?>(null)

        override fun invoke(): Flow<ModelDownloadProgress?> = flow
    }

    private class FakeEnsure : EnsureModelDownloadedUseCase {
        var calledUrl: String? = null

        override suspend operator fun invoke(catalogUrl: String) {
            calledUrl = catalogUrl
        }
    }

    @Before fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun state_starts_with_NOT_READY() =
        runTest {
            val vm = OnboardingViewModel(FakeObserveStatus(), FakeObserveProgress(), FakeEnsure(), "https://c")
            val job = launch { vm.state.collect {} }
            advanceUntilIdle()
            assertEquals(ModelStatus.NOT_READY, vm.state.value.status)
            job.cancel()
        }

    @Test
    fun onStart_invokes_ensure_use_case() =
        runTest {
            val ensure = FakeEnsure()
            val vm = OnboardingViewModel(FakeObserveStatus(), FakeObserveProgress(), ensure, "https://example.com/catalog.json")
            vm.onStart()
            advanceUntilIdle()
            assertEquals("https://example.com/catalog.json", ensure.calledUrl)
        }

    @Test
    fun progress_is_reflected_in_state() =
        runTest {
            val progress = FakeObserveProgress()
            val vm = OnboardingViewModel(FakeObserveStatus(), progress, FakeEnsure(), "https://c")
            val job = launch { vm.state.collect {} }
            progress.flow.value = ModelDownloadProgress("v", 500, 1000)
            advanceUntilIdle()
            val p = vm.state.value.progress
            assertTrue(p != null && p.bytesWritten == 500L)
            job.cancel()
        }
}
