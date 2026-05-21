package com.just.feature.memo.memoDetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.just.assistant.repository.model.Note
import com.just.assistant.usecase.note.di.FindNoteByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface MemoDetailState {
    data object Loading : MemoDetailState

    data class Loaded(val note: Note) : MemoDetailState

    data object NotFound : MemoDetailState
}

@HiltViewModel
class MemoDetailViewModel
    @Inject
    constructor(
        savedState: SavedStateHandle,
        private val findById: FindNoteByIdUseCase,
    ) : ViewModel() {
        private val noteId: Long = checkNotNull(savedState["noteId"])
        private val _state = MutableStateFlow<MemoDetailState>(MemoDetailState.Loading)
        val state: StateFlow<MemoDetailState> = _state.asStateFlow()

        init {
            load()
        }

        private fun load() {
            viewModelScope.launch {
                val note = findById(noteId)
                _state.value = if (note == null) MemoDetailState.NotFound else MemoDetailState.Loaded(note)
            }
        }
    }
