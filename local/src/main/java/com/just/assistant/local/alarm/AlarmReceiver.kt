package com.just.assistant.local.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
        val requestId = intent.getIntExtra(EXTRA_REQUEST_ID, -1)
        // v0.2: 알람 인프라만 검증. Notification은 v0.3.
        Log.i(TAG, "alarm fired: requestId=$requestId title=$title body=$body")
    }

    companion object {
        const val ACTION_FIRE = "com.just.assistant.local.alarm.FIRE"
        const val EXTRA_TITLE = "extra.title"
        const val EXTRA_BODY = "extra.body"
        const val EXTRA_REQUEST_ID = "extra.requestId"
        const val EXTRA_NOTE_ID = "extra.noteId"
        private const val TAG = "AlarmReceiver"
    }
}
