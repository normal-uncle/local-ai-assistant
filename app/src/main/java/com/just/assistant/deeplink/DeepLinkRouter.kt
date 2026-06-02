package com.just.assistant.deeplink

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepLinkRouter
    @Inject
    constructor() {
        private val _events =
            MutableSharedFlow<Long>(
                replay = 1,
                extraBufferCapacity = 0,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )
        val events: SharedFlow<Long> = _events.asSharedFlow()

        fun emitNoteId(noteId: Long) {
            _events.tryEmit(noteId)
        }
    }
