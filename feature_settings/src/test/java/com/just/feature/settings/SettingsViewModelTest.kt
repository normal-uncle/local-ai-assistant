package com.just.feature.settings

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.just.assistant.local.pref.UserPreferences
import com.just.assistant.usecase.briefing.di.BriefingController
import com.just.feature.settings.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class SettingsViewModelTest {
    private class FakeController : BriefingController {
        var enabled = 0
        var disabled = 0
        override fun enable() { enabled++ }
        override fun disable() { disabled++ }
    }

    // viewModelScope dispatches on Dispatchers.Main.immediate. Under Robolectric the
    // main looper is the same thread runBlocking blocks, so route Main to a real
    // background dispatcher to avoid a deadlock while keeping real time + DataStore IO.
    @Before
    fun setUp() {
        Dispatchers.setMain(Dispatchers.Default)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun prefs() = UserPreferences(ApplicationProvider.getApplicationContext())

    @Test
    fun toggle_on_saves_and_enables() =
        runBlocking {
            val p = prefs()
            val c = FakeController()
            val vm = SettingsViewModel(p, c)
            vm.onToggleBriefing(true)
            delay(100)
            assertEquals(true, p.briefingEnabled.first())
            assertEquals(1, c.enabled)
        }

    @Test
    fun toggle_off_saves_and_disables() =
        runBlocking {
            val p = prefs()
            p.setBriefingEnabled(true)
            val c = FakeController()
            val vm = SettingsViewModel(p, c)
            vm.onToggleBriefing(false)
            delay(100)
            assertEquals(false, p.briefingEnabled.first())
            assertEquals(1, c.disabled)
        }
}
