package com.just.assistant.local.model

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.modelStatusDataStore by preferencesDataStore(name = "model_status")

@Singleton
class ModelStatusPrefs
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val keySelectedVariantId = stringPreferencesKey("selected_variant_id")
        private val keyStatus = stringPreferencesKey("status")

        /** "NOT_READY" | "DOWNLOADING" | "READY" | "FAILED" — Repository에서 enum 매핑. */
        val rawStatus: Flow<String> =
            context.modelStatusDataStore.data.map { it[keyStatus] ?: "NOT_READY" }

        val selectedVariantId: Flow<String?> =
            context.modelStatusDataStore.data.map { it[keySelectedVariantId] }

        suspend fun setStatus(value: String) {
            context.modelStatusDataStore.edit { it[keyStatus] = value }
        }

        suspend fun setSelectedVariant(variantId: String) {
            context.modelStatusDataStore.edit { it[keySelectedVariantId] = variantId }
        }
    }
