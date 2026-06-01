package com.just.assistant

import android.app.Application
import com.just.assistant.local.notification.NotificationChannelInitializer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AssistantApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationChannelInitializer.init(this)
    }
}
