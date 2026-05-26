package com.just.assistant.ai.golden

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoldenCaseLoader
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val json = Json { ignoreUnknownKeys = true }

        /**
         * `res/raw/golden_classification.jsonl`을 로드해 1줄당 1 case로 파싱.
         * 빈 줄 / 주석(#)은 스킵.
         */
        fun load(): List<ClassificationGoldenCase> {
            val resId =
                context.resources.getIdentifier(
                    "golden_classification",
                    "raw",
                    context.packageName,
                )
            require(resId != 0) { "golden_classification.jsonl resource not found" }
            return context.resources.openRawResource(resId).bufferedReader().useLines { lines ->
                lines
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && !it.startsWith("#") }
                    .map { json.decodeFromString(ClassificationGoldenCase.serializer(), it) }
                    .toList()
            }
        }
    }
