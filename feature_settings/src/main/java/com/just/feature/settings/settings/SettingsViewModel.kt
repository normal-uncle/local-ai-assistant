package com.just.feature.settings.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just.assistant.local.pref.UserPreferences
import com.just.assistant.usecase.briefing.di.BriefingController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val prefs: UserPreferences,
        private val controller: BriefingController,
    ) : ViewModel() {
        val briefingEnabled: StateFlow<Boolean> =
            prefs.briefingEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

        fun onToggleBriefing(enabled: Boolean) {
            viewModelScope.launch {
                prefs.setBriefingEnabled(enabled)
                if (enabled) controller.enable() else controller.disable()
            }
        }
    }
