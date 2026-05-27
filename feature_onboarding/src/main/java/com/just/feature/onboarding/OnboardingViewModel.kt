package com.just.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just.assistant.repository.model.ModelDownloadProgress
import com.just.assistant.repository.model.ModelStatus
import com.just.assistant.usecase.model.di.EnsureModelDownloadedUseCase
import com.just.assistant.usecase.model.di.ObserveModelDownloadProgressUseCase
import com.just.assistant.usecase.model.di.ObserveModelStatusUseCase
import com.just.assistant.usecase.model.di.ObserveSelectedVariantIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

data class OnboardingState(
    val status: ModelStatus = ModelStatus.NOT_READY,
    val progress: ModelDownloadProgress? = null,
    val variantId: String? = null,
)

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        observeStatus: ObserveModelStatusUseCase,
        observeProgress: ObserveModelDownloadProgressUseCase,
        observeVariant: ObserveSelectedVariantIdUseCase,
        private val ensure: EnsureModelDownloadedUseCase,
        @Named("modelCatalogUrl") private val catalogUrl: String,
    ) : ViewModel() {
        val state: StateFlow<OnboardingState> =
            combine(observeStatus(), observeProgress(), observeVariant()) { status, progress, variantId ->
                OnboardingState(status, progress, variantId)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = OnboardingState(),
            )

        fun onStart() {
            viewModelScope.launch { ensure(catalogUrl) }
        }
    }
