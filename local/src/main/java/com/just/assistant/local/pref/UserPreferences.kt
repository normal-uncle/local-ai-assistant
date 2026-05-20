package com.just.assistant.local.pref

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences
    @Inject
    constructor(
        private val context: Context,
    ) {
        private val keyOnboardingDone = booleanPreferencesKey("onboarding_done")

        val onboardingDone: Flow<Boolean> =
            context.dataStore.data.map { it[keyOnboardingDone] ?: false }

        suspend fun setOnboardingDone(done: Boolean) {
            context.dataStore.edit { it[keyOnboardingDone] = done }
        }
    }
