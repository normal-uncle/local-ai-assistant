package com.just.assistant

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.just.assistant.deeplink.DeepLinkConstants
import com.just.assistant.deeplink.DeepLinkRouter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var deepLinkRouter: DeepLinkRouter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent { AssistantApp() }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action != DeepLinkConstants.ACTION_VIEW_NOTE) return
        val noteId = intent.getLongExtra(DeepLinkConstants.EXTRA_NOTE_ID, -1L)
        if (noteId >= 0L) {
            deepLinkRouter.emitNoteId(noteId)
        }
    }
}
