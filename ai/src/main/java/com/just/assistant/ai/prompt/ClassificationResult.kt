package com.just.assistant.ai.prompt

import com.just.assistant.repository.model.NoteType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ClassificationResultDto(
    val type: String,
    val title: String,
    val body: String = "",
    val tags: List<String> = emptyList(),
    @SerialName("datetime_iso") val datetimeIso: String? = null,
)

data class ClassificationResult(
    val type: NoteType,
    val title: String,
    val body: String,
    val tags: List<String>,
    val datetimeIso: String?,
) {
    companion object {
        private val json =
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            }

        /**
         * AI 응답에서 첫 번째 JSON 객체를 추출해 파싱.
         * 모델이 ```json ... ``` 코드펜스로 감싸거나 앞뒤 텍스트가 붙어도 동작.
         * 파싱 실패 시 null 반환 (호출 측에서 폴백 처리).
         */
        fun parse(rawResponse: String): ClassificationResult? {
            val firstBrace = rawResponse.indexOf('{')
            val lastBrace = rawResponse.lastIndexOf('}')
            if (firstBrace < 0 || lastBrace <= firstBrace) return null
            val jsonSlice = rawResponse.substring(firstBrace, lastBrace + 1)
            return runCatching {
                val dto = json.decodeFromString(ClassificationResultDto.serializer(), jsonSlice)
                ClassificationResult(
                    type = runCatching { NoteType.valueOf(dto.type.uppercase()) }.getOrDefault(NoteType.MEMO),
                    title = dto.title,
                    body = dto.body,
                    tags = dto.tags,
                    datetimeIso = dto.datetimeIso,
                )
            }.getOrNull()
        }
    }
}
