package com.just.feature.memo.memoDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just.assistant.repository.model.Note
import com.just.assistant.usecase.note.di.FindNoteByIdUseCase
import com.just.assistant.usecase.schedule.di.UnscheduleNoteUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MemoDetailState {
    data object Loading : MemoDetailState

    data class Loaded(val note: Note) : MemoDetailState

    data object NotFound : MemoDetailState
}

@HiltViewModel(assistedFactory = MemoDetailViewModel.Factory::class)
class MemoDetailViewModel
    @AssistedInject
    constructor(
        @Assisted private val noteId: Long,
        private val findById: FindNoteByIdUseCase,
        private val unschedule: UnscheduleNoteUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow<MemoDetailState>(MemoDetailState.Loading)
        val state: StateFlow<MemoDetailState> = _state.asStateFlow()

        init {
            load()
        }

        fun onUnschedule() {
            viewModelScope.launch {
                unschedule(noteId)
                load()
            }
        }

        private fun load() {
            viewModelScope.launch {
                val note = findById(noteId)
                _state.value =
                    if (note == null) MemoDetailState.NotFound else MemoDetailState.Loaded(note)
            }
        }

        @AssistedFactory
        interface Factory {
            fun create(noteId: Long): MemoDetailViewModel
        }
    }
