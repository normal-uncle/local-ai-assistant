package com.just.feature.memo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just.assistant.repository.model.Note
import com.just.assistant.usecase.note.di.ObserveNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface MemoListState {
    data object Loading : MemoListState

    data class Loaded(val notes: List<Note>) : MemoListState
}

@HiltViewModel
class MemoListViewModel
    @Inject
    constructor(
        observeNotes: ObserveNotesUseCase,
    ) : ViewModel() {
        val state: StateFlow<MemoListState> =
            observeNotes()
                .map<List<Note>, MemoListState> { MemoListState.Loaded(it) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = MemoListState.Loading,
                )
    }
