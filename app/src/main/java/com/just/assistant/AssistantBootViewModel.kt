package com.just.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just.assistant.repository.model.ModelStatus
import com.just.assistant.usecase.model.di.ObserveModelStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AssistantBootViewModel
    @Inject
    constructor(
        observeStatus: ObserveModelStatusUseCase,
    ) : ViewModel() {
        val initialRoute: StateFlow<AssistantRoute?> =
            observeStatus()
                .map<ModelStatus, AssistantRoute?> { status ->
                    if (status == ModelStatus.READY) AssistantRoute.Capture else AssistantRoute.Onboarding
                }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = null,
                )
    }
