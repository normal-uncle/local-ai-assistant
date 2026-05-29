# LiteRT-LM SDK 마이그레이션 (MediaPipe Tasks GenAI 교체)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** `MediaPipeInferenceEngine` (MediaPipe Tasks GenAI 0.10.x, `.task` zip 포맷만 지원)을 `LiteRtLmInferenceEngine` (Google AI Edge LiteRT-LM SDK, `.litertlm` 포맷 지원)으로 교체해 spec 의도대로 Gemma 4 E2B를 디바이스에서 추론 가능하게 한다. `InferenceEngine` interface는 유지하고 impl만 교체 — 호출 측(`:usecase`, `:feature_capture`) 무영향.

**Architecture:**
- 새 의존성: `com.google.ai.edge.litertlm:litertlm-android:latest.release`. MediaPipe Tasks GenAI 제거.
- 새 클래스 `:ai/inference/LiteRtLmInferenceEngine.kt` — `Engine` + `Conversation` 기반. `MediaPipeInferenceEngine.kt`는 삭제 (롤백 가치 낮음 — git 히스토리에 남음).
- `AiModule.kt`의 `@Binds`만 새 impl 가리키도록 교체.
- 카탈로그를 `gemma-4-E2B-it.litertlm` (표준 CPU/GPU XNNPack/MLDrift) 가리키도록 업데이트. SHA·size 재측정.
- LiteRT-LM API 정확한 시그니처는 SDK 인증 (artifact의 `.class` javap 또는 ai.google.dev/edge/litert-lm/android 문서)으로 implementer가 빌드 시점에 정정. P2.B Task 5 패턴 그대로.

**Tech Stack:** Google AI Edge LiteRT-LM (`com.google.ai.edge.litertlm:litertlm-android`), Kotlin Coroutines (기존), Hilt (기존).

**참고:**
- LiteRT-LM Android 가이드: https://ai.google.dev/edge/litert-lm/android
- README of `litert-community/gemma-4-E2B-it-litert-lm`: API 클래스 `Engine` + `Engine(engineConfig).initialize()` + `engine.createConversation(conversationConfig)` + `conversation.sendMessage(...)` 또는 `sendMessageAsync(...).collect { ... }`
- 이전 plan (실패): `docs/superpowers/plans/2026-05-28-gemma-4-e2b-swap.md` — 카탈로그만 바꿔서 0.10.35 / Gemma 4 시도했으나 zip 포맷 mismatch로 실패

---

## File Structure

```
gradle/libs.versions.toml                            # mediapipe 제거, litertlm 추가

ai/build.gradle.kts                                  # mediapipe 의존성 교체
ai/src/main/java/com/just/assistant/ai/
  inference/InferenceEngine.kt                       # 변경 없음 (계약 유지)
  inference/InferenceConfig.kt                       # 변경 없음
  inference/MediaPipeInferenceEngine.kt              # 삭제
  inference/LiteRtLmInferenceEngine.kt               # 신규 (Engine + Conversation 래퍼)
  AiModule.kt                                        # @Binds 교체

dist/
  model_catalog.json                                 # .task → .litertlm URL + 새 SHA/size
  README.md                                          # 변종 표 + 히스토리 추가
```

**컨벤션 메모:**
- `InferenceEngine` interface (`suspend load(File, InferenceConfig)`, `unload()`, `isReady()`, `suspend generate(prompt): String`) 유지.
- ktlint imin, jvmToolchain(18), minSdk 31 — LiteRT-LM이 더 높은 minSdk 요구하면 그 시점에 plan 정정.
- 테스트: instrumented 회귀 (P2.C의 `ClassificationRegressionTest`) 그대로. 단위 테스트는 추론 자체를 검증하지 않음 (native lib).

---

## Task 1: libs.versions.toml — MediaPipe 제거 + LiteRT-LM 추가

**Files:**
- Modify: `gradle/libs.versions.toml`

- [ ] **Step 1: LiteRT-LM 최신 버전 확인**

```bash
curl -sL "https://dl.google.com/android/maven2/com/google/ai/edge/litertlm/litertlm-android/maven-metadata.xml" | head -30
```

Expected: XML에 `<latest>X.Y.Z</latest>` 표시.

> 좌표가 `com.google.ai.edge.litertlm:litertlm-android`가 아닐 가능성도 있음 (artifact 이름 변경 등). 위 명령이 404나 empty면 다음 후보 시도:
> - `com.google.ai.edge.litert:litertlm-android`
> - `com.google.ai.edge.litert-lm:litertlm-android`
> - `com.google.ai.edge:litertlm-android`
>
> 검증: Google Maven 검색 (`https://maven.google.com/web/index.html`) 또는 `mvnrepository.com` 검색.
>
> 정확한 좌표·버전을 메모. 이하 `<LITERTLM_VERSION>` 자리에 넣는다.

- [ ] **Step 2: `[versions]`에서 mediaPipeTasksGenAi 제거 + litertlmAndroid 추가**

기존 `mediaPipeTasksGenAi = "0.10.35"` 줄 삭제. 그 자리(또는 알파벳 위치)에 추가:
```toml
litertlmAndroid = "<LITERTLM_VERSION>"
```

- [ ] **Step 3: `[libraries]`에서 mediapipe-tasks-genai 제거 + litertlm-android 추가**

기존 `mediapipe-tasks-genai = { ... }` 줄 삭제. 그 자리에 추가 (좌표는 Step 1에서 확정한 값):
```toml
litertlm-android = { group = "com.google.ai.edge.litertlm", name = "litertlm-android", version.ref = "litertlmAndroid" }
```

- [ ] **Step 4: 빌드 검증**

```bash
cd /Users/kichan/Desktop/studies/local_ai
./gradlew help
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: 커밋 (다음 task와 묶기 위해 보류 가능, 단독도 OK)**

```bash
git add gradle/libs.versions.toml
git commit -m "chore(build): MediaPipe Tasks GenAI 제거 + LiteRT-LM <LITERTLM_VERSION> 추가"
```

---

## Task 2: `:ai/build.gradle.kts` — 의존성 교체

**Files:**
- Modify: `ai/build.gradle.kts`

- [ ] **Step 1: `dependencies { }` 블록에서 mediapipe → litertlm 교체**

기존 라인:
```kotlin
    implementation(libs.mediapipe.tasks.genai)
```

다음으로 교체:
```kotlin
    implementation(libs.litertlm.android)
```

다른 라인은 그대로.

- [ ] **Step 2: 빌드 검증** (이 시점에 `MediaPipeInferenceEngine.kt`가 아직 mediapipe 패키지를 import하고 있어 컴파일 에러 예상)

```bash
./gradlew :ai:assembleDebug 2>&1 | tail -15
```

Expected: COMPILE FAIL with `Unresolved reference: mediapipe`. 다음 Task에서 클래스 교체.

- [ ] **Step 3: (commit 보류 — Task 3까지 묶음)**

---

## Task 3: `LiteRtLmInferenceEngine.kt` 작성 + `MediaPipeInferenceEngine.kt` 삭제

**Files:**
- Delete: `ai/src/main/java/com/just/assistant/ai/inference/MediaPipeInferenceEngine.kt`
- Create: `ai/src/main/java/com/just/assistant/ai/inference/LiteRtLmInferenceEngine.kt`
- Modify: `ai/src/main/java/com/just/assistant/ai/AiModule.kt`

- [ ] **Step 1: 기존 MediaPipeInferenceEngine 삭제**

```bash
rm /Users/kichan/Desktop/studies/local_ai/ai/src/main/java/com/just/assistant/ai/inference/MediaPipeInferenceEngine.kt
```

- [ ] **Step 2: `LiteRtLmInferenceEngine.kt` 작성**

LiteRT-LM SDK의 정확한 API (Engine constructor 시그니처, EngineConfig 필드, ConversationConfig 필드)는 implementer가 빌드 시점에 SDK 문서/`javap`으로 검증해 정정. 다음은 README 기준 추정 코드:

```kotlin
package com.just.assistant.ai.inference

import android.content.Context
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.ConversationConfig
import com.google.ai.edge.litertlm.Backend
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LiteRtLmInferenceEngine
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) : InferenceEngine {
        @Volatile
        private var engine: Engine? = null

        @Volatile
        private var currentConfig: InferenceConfig? = null

        @Volatile
        private var currentModelPath: String? = null

        override suspend fun load(modelFile: File, config: InferenceConfig) {
            withContext(Dispatchers.IO) {
                if (engine != null && currentConfig == config && currentModelPath == modelFile.absolutePath) {
                    return@withContext
                }
                engine?.close()
                engine = null

                val engineConfig =
                    EngineConfig.builder()
                        .setModelPath(modelFile.absolutePath)
                        .setBackend(Backend.CPU)
                        .build()
                val newEngine = Engine(engineConfig)
                newEngine.initialize()
                engine = newEngine
                currentConfig = config
                currentModelPath = modelFile.absolutePath
            }
        }

        override fun unload() {
            engine?.close()
            engine = null
            currentConfig = null
            currentModelPath = null
        }

        override fun isReady(): Boolean = engine != null

        override suspend fun generate(prompt: String): String =
            withContext(Dispatchers.IO) {
                val e = engine ?: error("InferenceEngine not loaded; call load() first")
                val convConfig =
                    ConversationConfig.builder()
                        .setMaxTokens(currentConfig?.maxTokens ?: 512)
                        .setTemperature(currentConfig?.temperature ?: 0.2f)
                        .setTopK(currentConfig?.topK ?: 40)
                        .setTopP(currentConfig?.topP ?: 0.95f)
                        .build()
                val conversation = e.createConversation(convConfig)
                try {
                    val response = conversation.sendMessage(prompt)
                    response.toString()
                } finally {
                    conversation.close()
                }
            }
    }
```

> **API 검증 필수**: 첫 빌드에서 `Engine.builder() vs Engine 직접 생성자`, `EngineConfig.Builder` 필드 이름(`setModelPath` vs `setModelFile` vs 등), `Backend.CPU` enum 존재 여부, `ConversationConfig.Builder` 필드 (`setMaxTokens` vs `setMaxNumTokens`, `setTemperature` 위치 — Engine vs Conversation), `sendMessage()` 반환 타입 (`String` vs `ConversationResponse`) 등이 SDK 버전에 따라 다름.
>
> 검증 방법:
> 1. `./gradlew :ai:dependencies | grep litertlm` 로 실제 다운로드된 artifact 확인
> 2. `~/.gradle/caches/modules-2/files-2.1/com.google.ai.edge.litertlm/litertlm-android/<version>/*.aar` 안의 classes.jar를 풀어 `javap`로 시그니처 확인
> 3. 또는 IntelliJ/Android Studio에서 자동완성 사용
>
> 발견된 차이를 위 코드에 반영하고 commit 메시지에 명시.

- [ ] **Step 3: `AiModule.kt` binding 교체**

`ai/src/main/java/com/just/assistant/ai/AiModule.kt`:

```kotlin
package com.just.assistant.ai

import com.just.assistant.ai.inference.InferenceEngine
import com.just.assistant.ai.inference.LiteRtLmInferenceEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {
    @Binds
    @Singleton
    abstract fun bindInferenceEngine(impl: LiteRtLmInferenceEngine): InferenceEngine
}
```

- [ ] **Step 4: 빌드 + API mismatch 해결**

```bash
./gradlew :ai:assembleDebug
```

Expected (이상적): BUILD SUCCESSFUL.

만약 컴파일 에러:
1. unresolved reference → SDK 실제 API 확인 후 코드 수정 (Step 2 KDoc 참조)
2. 같은 step 안에서 반복 — 각 수정마다 빌드 다시 시도
3. 결국 성공할 때까지

성공 후 ktlint:
```bash
./gradlew :ai:ktlintFormat :ai:ktlintCheck
```

- [ ] **Step 5: 커밋 (Task 1+2+3 통합)**

```bash
git add gradle/libs.versions.toml ai/
git commit -m "feat(ai): MediaPipe → LiteRT-LM SDK 마이그레이션 (InferenceEngine impl 교체)"
```

commit 메시지 body에 발견된 API 차이(있다면) 명시.

---

## Task 4: 카탈로그를 `.litertlm` 파일로 교체

**Files:**
- 측정만 (다음 Task에서 적용)

- [ ] **Step 1: `gemma-4-E2B-it.litertlm` 익명 access 확인**

```bash
curl -sI "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm" | head -3
```

Expected: HTTP/2 302 또는 200.

- [ ] **Step 2: 모델 다운로드**

```bash
mkdir -p /tmp/model-check
rm -f /tmp/model-check/gemma-4-e2b.litertlm
curl -sL "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm" \
  -o /tmp/model-check/gemma-4-e2b.litertlm \
  --max-time 1800
ls -lh /tmp/model-check/gemma-4-e2b.litertlm
```

Expected: README 기준 2.59GB 정도 (`.task` 1.87GB보다 큼 — 임베딩 weight 포함).

- [ ] **Step 3: SHA + 사이즈 측정**

```bash
shasum -a 256 /tmp/model-check/gemma-4-e2b.litertlm
stat -f %z /tmp/model-check/gemma-4-e2b.litertlm
```

`<NEW_SHA>` = hex hash
`<NEW_SIZE_BYTES>` = file size
`<NEW_SIZE_MB>` = ceil(SIZE_BYTES / 1048576)

값 메모.

---

## Task 5: `dist/model_catalog.json` + `dist/README.md` 업데이트

**Files:**
- Modify: `dist/model_catalog.json`
- Modify: `dist/README.md`

- [ ] **Step 1: `dist/model_catalog.json` 교체**

```json
{
  "version": "2026-05-29-litert-lm",
  "variants": [
    {
      "id": "gemma-4-E2B-it",
      "url": "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm",
      "sha256": "<NEW_SHA>",
      "sizeMb": <NEW_SIZE_MB>,
      "minRamGb": 4,
      "recommended": true
    }
  ]
}
```

`<NEW_SHA>`, `<NEW_SIZE_MB>`을 Task 4에서 측정한 값으로 교체.

> 참고: variant id를 `gemma-4-E2B-it-web`에서 `gemma-4-E2B-it`로 변경 — `.litertlm` 표준 변종.

- [ ] **Step 2: JSON syntax 검증**

```bash
python3 -c "import json; print(json.load(open('dist/model_catalog.json'))['variants'][0])"
```

Expected: variant dict 출력.

- [ ] **Step 3: `dist/README.md` 업데이트**

```markdown
# dist/ — Distribution assets

이 폴더는 앱의 모델 카탈로그를 호스팅합니다.
GitHub raw URL로 직접 접근:

- `model_catalog.json` — 카탈로그 manifest (Gemma 4 E2B 단일 변종, `.litertlm` 포맷)

## 현재 변종

| ID | 사이즈 | 최소 RAM | 비고 |
|---|---|---|---|
| gemma-4-E2B-it | ~2.6GB | 4GB | Spec 의도. LiteRT-LM SDK 필요. ungated |

`DeviceProfiler`가 기기 RAM ≥ 4GB 확인하고 `VariantSelector`가 단일 권장 변종 선택.

## 히스토리

- **2026-05-29**: `.litertlm` 포맷으로 전환. LiteRT-LM SDK 도입.
- 2026-05-28 (실패): `gemma-4-E2B-it-web.task`로 시도했으나 MediaPipe Tasks GenAI가 LiteRT-LM `.task` 변종 zip을 못 열어 실패.
- 2026-05-26 (P2.C): Qwen 2.5 0.5B + 1.5B 변종. Gemma agreement 우회용 baseline.
- 2026-05-22 (P2.B): Qwen 2.5 0.5B 단일.
- 2026-05-21 (P2.A): 1MB dev-fixture 더미.

## v0.3 계획

E2B + E4B 다중 변종 — `gemma-4-E4B-it.litertlm` (litert-community) 추가 검증.
```

- [ ] **Step 4: 두 파일 커밋 + push**

```bash
git add dist/model_catalog.json dist/README.md
git commit -m "feat(dist): .litertlm 포맷 (gemma-4-E2B-it)로 카탈로그 전환"
git push
```

- [ ] **Step 5: raw URL 동작 확인**

```bash
sleep 5
curl -sI "https://raw.githubusercontent.com/normal-uncle/local-ai-assistant/feature/v0.2-plan/dist/model_catalog.json" | head -3
curl -sL "https://raw.githubusercontent.com/normal-uncle/local-ai-assistant/feature/v0.2-plan/dist/model_catalog.json" | python3 -m json.tool
```

Expected: HTTP 200, JSON variant id `gemma-4-E2B-it`, sha256 = Step 1의 `<NEW_SHA>`.

---

## Task 6: 자동 회귀 검증 (디바이스 빼고)

- [ ] **Step 1: 단위 + ktlint + assembleRelease**

```bash
cd /Users/kichan/Desktop/studies/local_ai
./gradlew testDebugUnitTest --rerun-tasks
./gradlew ktlintCheck
./gradlew :app:assembleRelease
```

Expected: 모든 PASS. P2.C의 53 unit tests + LiteRT-LM 이전과 동일. assembleRelease 사이즈는 MediaPipe 네이티브 lib 제거되고 LiteRT-LM 네이티브 lib로 교체 — 사이즈 변동 가능 (보고).

- [ ] **Step 2: ConnectedAndroidTest (회귀 빼고 기존 흐름만)**

```bash
./gradlew connectedDebugAndroidTest 2>&1 | tail -10
```

Expected: 기존 12 PASS + `ClassificationRegressionTest`는 모델 없으면 `assumeTrue` SKIP. 0 failures.

---

## Task 7: 디바이스 install + 다운로드 + 회귀 측정

- [ ] **Step 1: 앱 재설치**

```bash
./gradlew :app:installDebug
adb shell pm clear com.just.assistant 2>&1 || true
adb shell am start -n com.just.assistant/.MainActivity
```

기존 Qwen/Gemma `.task` 파일이 device에 남아있다면 명시적으로 삭제 (디바이스 storage 절약):
```bash
adb shell run-as com.just.assistant rm -rf files/models 2>&1 || true
```

- [ ] **Step 2: 사용자 액션 — Gemma 4 E2B `.litertlm` 다운로드**

사용자에게 안내:
- Onboarding 화면에 변종 라벨 `gemma-4-E2B-it` 표시
- "다운로드 시작" 탭 → 2.6GB Wi-Fi 다운로드 (~15-20분)
- SHA 검증 후 자동 Capture 진입

모니터링:
```bash
for i in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20; do
  sleep 30
  size=$(adb shell run-as com.just.assistant stat -c %s files/models/gemma-4-E2B-it.litertlm 2>/dev/null || echo 0)
  echo "$(date +%H:%M:%S) bytes=$size"
done
```

다운로드 완료 (`<NEW_SIZE_BYTES>` 일치) 확인.

- [ ] **Step 3: 회귀 테스트 실행**

```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.just.assistant.regression.ClassificationRegressionTest
```

50 케이스 × Gemma 4 E2B 추론. README 기준 S26 Ultra GPU에서 prefill 3808 tok/s, decode 52 tok/s — Z Fold4 (Snapdragon 8+ Gen 1) CPU에서는 더 느릴 가능성. 추론 시간 5~20분 예상.

만약 다시 zip archive 또는 다른 native error → SDK API/포맷 mismatch. 첨부 stacktrace 분석 후 fix.

- [ ] **Step 4: 결과 추출**

```bash
./scripts/fetch-regression-report.sh
```

출력 확인:
- `variantId`: `gemma-4-E2B-it`
- `accuracyType`: 새 값 (Qwen 0.42 대비)
- `accuracyDatetime`: 새 값 (Qwen 0.82 대비)
- `p50LatencyMs`, `p95LatencyMs`
- 오답 목록

- [ ] **Step 5: 비교 표 작성**

| 지표 | Qwen 2.5-1.5B (P2.C) | Gemma 4 E2B (.litertlm) |
|---|---|---|
| accuracyType | 0.42 | <new> |
| accuracyDatetime | 0.82 | <new> |
| p50 latency | 7152ms | <new> |
| p95 latency | 8886ms | <new> |
| 한국어 EVENT 정답률 | 2/10 | <new> |
| 한국어 REMINDER 정답률 | 1/10 | <new> |
| JSON 파싱 실패 | 3 | <new> |

표는 PR description 또는 다음 plan의 baseline으로 활용.

- [ ] **Step 6: 정리 커밋 (필요 시)**

```bash
git add -A
git commit -m "chore: LiteRT-LM 마이그레이션 회귀 검증 완료"
```

---

## Task 8: 마무리 — `MediaPipeInferenceEngineTest`(?) 등 잔여 정리

- [ ] **Step 1: dead reference 확인**

```bash
grep -r "MediaPipe\|mediapipe" --include="*.kt" --include="*.kts" /Users/kichan/Desktop/studies/local_ai 2>&1 | grep -v "build/" | grep -v ".gradle/" | head -20
```

Expected: 코드에서 MediaPipe 참조 없음. 문서·plan 파일의 historical 언급은 OK.

- [ ] **Step 2: 만약 dead reference 있으면 정리**

각 케이스별로 해당 파일 수정 + commit. 없으면 skip.

- [ ] **Step 3: 최종 빌드**

```bash
./gradlew clean
./gradlew :app:assembleDebug :app:assembleRelease
./gradlew testDebugUnitTest
./gradlew ktlintCheck
```

Expected: 모두 PASS.

- [ ] **Step 4: 정리 커밋 (있으면)**

```bash
git add -A
git commit -m "chore: LiteRT-LM 마이그레이션 잔여 dead reference 정리"
```

---

## Done Definition

- [ ] `gradle/libs.versions.toml` MediaPipe 제거 + LiteRT-LM `<LITERTLM_VERSION>` 등록
- [ ] `MediaPipeInferenceEngine.kt` 삭제, `LiteRtLmInferenceEngine.kt` 작성
- [ ] `AiModule.kt` binding이 새 impl 가리킴
- [ ] `dist/model_catalog.json`이 `gemma-4-E2B-it.litertlm` URL + 올바른 SHA·size
- [ ] 자동 검증 모두 PASS (testDebugUnitTest 53, instrumented except regression, ktlint, assembleRelease)
- [ ] 디바이스에서 다운로드 + 회규 측정 성공 — Engine.initialize() / sendMessage() 동작 확인
- [ ] `fetch-regression-report.sh`로 정확도/지연 추출
- [ ] Qwen 1.5B 대비 비교 표 작성
- [ ] 코드에 MediaPipe 잔여 참조 없음

v0.2 진행 결정:
- Gemma datetime 정확도 ≥ 0.85 → v0.2 plan 그대로 진행
- 0.85 미만이지만 0.82 이상 → v0.2 진행 + prompt engineering follow-up
- 정확도 큰 폭 증가 (예: 0.7+) → spec 의도 충족, v0.2 진행

---

## 의도적 미룬 항목

| 항목 | 미루는 이유 | 처리 시점 |
|---|---|---|
| Qualcomm NPU 가속 변종 (`_qualcomm_sm8750.litertlm`) | Z Fold4는 sm8475 (Gen 1+). 대상 변종 없음. NPU 변종 도입은 다중 변종 v0.3 | v0.3 |
| GPU backend (LiteRT MLDrift) | CPU 먼저 검증. GPU는 성능 향상 (README: 3,808 vs 557 prefill tok/s). v0.3 검증 | v0.3 |
| Streaming 응답 (`sendMessageAsync().collect`) | 동기 `sendMessage()`로 시작. UI 스트리밍은 P3 polish | P3 |
| Android AI Core / Gemini Nano | README 권장 path지만 디바이스 호환성 매우 좁음 (Pixel 8+, S24+). v1.0 출시 시 production 검토 | v1.0 |
| 모델 다운로드 동시 검증 (2.6GB 큼) | 단일 변종 카탈로그 + 기존 ModelDownloader 그대로 사용 | — |
| `.litertlm` 포맷의 in-process unit test | 네이티브 라이브러리 필요 + JVM 테스트 불가. instrumented만 | — |
