# Gemma 4 E2B 모델 교체 + 회귀 재측정

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** P2.B/P2.C에서 Gemma access agreement 우회로 사용했던 Qwen2.5를 spec 의도대로 **Gemma 4 E2B**로 교체. ungated `litert-community/gemma-4-E2B-it-litert-lm/gemma-4-E2B-it-web.task`를 카탈로그에 등록하고 동일한 골든 데이터셋으로 회귀 재측정해서 분류 정확도·지연 비교.

**Architecture:** 카탈로그 JSON만 교체. 코드 변경 없음. HF token / Authorization 헤더 / 라이선스 agreement 모두 불필요 (repo가 `gated: False`). 디바이스 reinstall → 1.87GB 재다운로드 → 회귀 실행 → `fetch-regression-report.sh`로 결과 추출 → Qwen 기준 0.42 대비 비교.

**Tech Stack:** 변경 없음 (P2.B의 MediaPipe LLM Inference 0.10.24 그대로). MediaPipe Tasks GenAI Android는 `.task` 표준 포맷을 로드하므로 `gemma-4-E2B-it-web.task`가 호환될 것 — 디바이스 smoke로 확인.

**참고:**
- P2.C: `docs/superpowers/plans/2026-05-26-v0.1-p2c-golden-dataset-device-profiler.md`
- Qwen 회귀 결과 (baseline): accuracyType=0.42, accuracyDatetime=0.82, p50=7152ms, p95=8886ms

---

## File Structure

```
dist/
  model_catalog.json          # 변종 2개를 Gemma 4 E2B + (선택) 폴백으로 교체
  README.md                   # 변종 라벨/사이즈 업데이트
regression-reports/
  report-<timestamp>.json     # 새 회귀 결과 (gitignore 권장 — 다음 task에서)
```

**컨벤션 메모:** 기존 P2.C 인프라 그대로 사용. `DeviceProfiler` + `VariantSelector`가 `minRamGb` 기준 자동 선택 — Gemma 4 E2B는 ~1.87GB라 `minRamGb=4`로 잡으면 대부분 기기에서 동작.

---

## Task 1: SHA-256 + 사이즈 측정

**Files:**
- 없음 (측정 결과만 다음 Task에 입력)

- [ ] **Step 1: 익명 access 재확인**

```bash
curl -sI "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it-web.task" | head -3
```

Expected: `HTTP/2 302` 또는 `200`. 인증 없이 access OK.

- [ ] **Step 2: 모델 파일 다운로드**

```bash
mkdir -p /tmp/model-check
rm -f /tmp/model-check/gemma-4-e2b.task
curl -sL "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it-web.task" \
  -o /tmp/model-check/gemma-4-e2b.task \
  --max-time 1800
ls -lh /tmp/model-check/gemma-4-e2b.task
```

Expected: ~1.87GB 파일.

- [ ] **Step 3: SHA-256 + size 계산**

```bash
shasum -a 256 /tmp/model-check/gemma-4-e2b.task
stat -f %z /tmp/model-check/gemma-4-e2b.task
```

`<GEMMA_SHA>` = shasum 출력의 hex 문자열
`<GEMMA_SIZE_BYTES>` = stat 출력 (bytes)
`<GEMMA_SIZE_MB>` = ceil(SIZE_BYTES / 1048576) — 약 1911 정도 예상

값을 메모. 다음 Task에서 카탈로그에 박음.

---

## Task 2: `dist/model_catalog.json` 교체

**Files:**
- Modify: `dist/model_catalog.json`

- [ ] **Step 1: 카탈로그를 Gemma 4 E2B로 교체**

P2.C의 2-변종 카탈로그(Qwen 0.5B + 1.5B)를 다음으로 대체:

```json
{
  "version": "2026-05-28-gemma4-e2b",
  "variants": [
    {
      "id": "gemma-4-E2B-it-web",
      "url": "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it-web.task",
      "sha256": "<GEMMA_SHA>",
      "sizeMb": <GEMMA_SIZE_MB>,
      "minRamGb": 4,
      "recommended": true
    }
  ]
}
```

`<GEMMA_SHA>`, `<GEMMA_SIZE_MB>`는 Task 1의 값으로 교체.

> Qwen 변종을 카탈로그에서 제거하는 이유: spec 의도가 Gemma 4 E2B 단일 변종이었고, 다중 변종은 v0.3 (E2B + E4B)에서 의미가 살아남. 인프라(DeviceProfiler/VariantSelector)는 변종 1개에서도 정상 동작 (P2.A에서 검증). Qwen은 historical baseline으로 PR description에 측정값만 남기고 카탈로그에선 빼는 게 깔끔.

- [ ] **Step 2: catalog JSON syntax 검증**

```bash
python3 -c "import json; print(json.load(open('dist/model_catalog.json'))['variants'][0]['id'])"
```

Expected: `gemma-4-E2B-it-web`

- [ ] **Step 3: 커밋 (push는 다음 Task와 묶기)**

```bash
git add dist/model_catalog.json
# push는 README 업데이트 후 함께
```

---

## Task 3: `dist/README.md` 업데이트

**Files:**
- Modify: `dist/README.md`

- [ ] **Step 1: README 내용 교체**

```markdown
# dist/ — Distribution assets

이 폴더는 앱의 모델 카탈로그를 호스팅합니다.
GitHub raw URL로 직접 접근:

- `model_catalog.json` — 카탈로그 manifest (Gemma 4 E2B 단일 변종)

## 현재 변종

| ID | 사이즈 | 최소 RAM | 비고 |
|---|---|---|---|
| gemma-4-E2B-it-web | ~1.9GB | 4GB | Spec 의도. ungated, MediaPipe `.task` 호환 |

`DeviceProfiler`가 기기 RAM ≥ 4GB 확인하고 `VariantSelector`가 단일 권장 변종 선택.

## 히스토리

- **2026-05-28**: Gemma 4 E2B로 교체. ungated `litert-community/gemma-4-E2B-it-litert-lm`에서 호스팅.
- 2026-05-26 (P2.C): Qwen 2.5 0.5B + 1.5B 변종. Gemma agreement 우회용 baseline.
- 2026-05-22 (P2.B): Qwen 2.5 0.5B 단일.
- 2026-05-21 (P2.A): 1MB dev-fixture 더미.

## v0.3 계획

E2B + E4B 다중 변종 — `Gemma-3n-E4B` 또는 `gemma-4-E4B-it-litert-lm` 의 `.task` 파일 검증 후 추가.
```

- [ ] **Step 2: 두 파일 함께 push**

```bash
git add dist/README.md
git commit -m "feat(dist): Gemma 4 E2B (ungated)로 카탈로그 교체 + Qwen 제거"
git push
```

- [ ] **Step 3: raw URL 동작 확인**

```bash
sleep 5
curl -sI "https://raw.githubusercontent.com/normal-uncle/local-ai-assistant/feature/v0.2-plan/dist/model_catalog.json" | head -3
curl -sL "https://raw.githubusercontent.com/normal-uncle/local-ai-assistant/feature/v0.2-plan/dist/model_catalog.json" | python3 -m json.tool
```

Expected: HTTP 200, JSON에 Gemma 4 E2B variant 1개.

---

## Task 4: `AppConfigModule` catalog URL을 P2.D brach로

**Files:**
- Modify: `app/src/main/java/com/just/assistant/di/AppConfigModule.kt`

- [ ] **Step 1: URL 변경**

`AppConfigModule.provideModelCatalogUrl()`을 현재 brach로 변경:

```kotlin
package com.just.assistant.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {
    /**
     * 개발용 카탈로그 URL. 현재는 v0.2-plan brach를 가리킴 (Gemma 4 E2B 카탈로그 포함).
     * TODO(머지 후): URL의 `feature/v0.2-plan` 부분을 `master`로 변경.
     */
    @Provides
    @Named("modelCatalogUrl")
    fun provideModelCatalogUrl(): String =
        "https://raw.githubusercontent.com/normal-uncle/local-ai-assistant/feature/v0.2-plan/dist/model_catalog.json"
}
```

P2.C에서 brach가 `feature/v0.1-p2c-plan`이었던 부분만 교체. 그 외 로직 변경 없음.

- [ ] **Step 2: 빌드 + ktlint**

```bash
./gradlew :app:assembleDebug :app:ktlintCheck
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: 커밋**

```bash
git add app/
git commit -m "chore(app): catalog URL을 v0.2-plan brach (Gemma 4 E2B 카탈로그)로 갱신"
```

---

## Task 5: 디바이스 reinstall + 다운로드 + 회귀 재측정

- [ ] **Step 1: 자동 회귀 (unit + instrumented)**

P2.C 인프라가 그대로 작동하는지 확인:

```bash
./gradlew testDebugUnitTest --rerun-tasks
./gradlew connectedDebugAndroidTest --rerun-tasks
./gradlew ktlintCheck
```

Expected: P2.C의 53 unit + 12 instrumented PASS (회귀 테스트는 모델 없어서 assumeTrue로 skip).

- [ ] **Step 2: 디바이스 reinstall + 클린 상태**

```bash
./gradlew :app:installDebug
adb shell pm clear com.just.assistant
adb shell am start -n com.just.assistant/.MainActivity
```

- [ ] **Step 3: 사용자 액션 — Gemma 4 다운로드**

사용자에게 안내:
- Onboarding 화면 표시 확인
- 변종 라벨: `gemma-4-E2B-it-web` 표시
- "다운로드 시작" 탭
- 약 1.9GB Wi-Fi 다운로드 (5~15분)
- 완료 후 자동 Capture 진입 (status READY)

```bash
# 진행 모니터링 (사용자가 시작한 후)
for i in 1 2 3 4 5 6 7 8 9 10; do
  sleep 30
  size=$(adb shell run-as com.just.assistant stat -c %s files/models/gemma-4-E2B-it-web.task 2>/dev/null || echo 0)
  echo "$(date +%H:%M:%S) bytes=$size"
done
```

다운로드 완료 (about 2_003_697_664 bytes) 확인.

- [ ] **Step 4: 회귀 테스트 실행**

```bash
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.just.assistant.regression.ClassificationRegressionTest
```

50 케이스 × Gemma 4 E2B 추론 — 약 5~15분 (E2B는 Effective 2B, Qwen 1.5B와 비슷한 latency 예상).

- [ ] **Step 5: 결과 추출**

```bash
./scripts/fetch-regression-report.sh
```

출력에서 확인:
- `variantId`: `gemma-4-E2B-it-web`
- `accuracyType`: 새 값 (Qwen 0.42 대비)
- `accuracyDatetime`: 새 값 (Qwen 0.82 대비)
- `p50LatencyMs` / `p95LatencyMs`: 새 값
- 오답 목록 (misclassified)

- [ ] **Step 6: 결과 비교 메모 (commit 메시지 또는 PR description용)**

다음 표 형식으로 정리:

| 지표 | Qwen 2.5-1.5B (P2.C) | Gemma 4 E2B (this) |
|---|---|---|
| accuracyType | 0.42 | <new> |
| accuracyDatetime | 0.82 | <new> |
| p50 latency | 7152ms | <new> |
| p95 latency | 8886ms | <new> |
| 한국어 EVENT 정답률 | 2/10 | <new> |
| 한국어 REMINDER 정답률 | 1/10 | <new> |
| JSON 파싱 실패 | 3 | <new> |

- [ ] **Step 7: 정리 커밋 (필요 시)**

```bash
git add -A
git commit -m "chore: Gemma 4 E2B 회귀 검증 후 정리"
```

---

## Done Definition (Gemma 4 E2B 교체 완료 기준)

- [ ] `dist/model_catalog.json`이 Gemma 4 E2B 단일 변종 + 올바른 SHA·sizeMb
- [ ] `dist/README.md` 변종 표 업데이트 + 히스토리 노트
- [ ] `AppConfigModule` URL이 `feature/v0.2-plan` brach 가리킴
- [ ] catalog raw URL HTTP 200
- [ ] 자동 검증 (unit + instrumented + ktlint + assembleRelease) 모두 PASS
- [ ] 디바이스 reinstall → Gemma 4 E2B 1.9GB 다운로드 → SHA 검증 → status READY → Capture 진입 흐름 성공
- [ ] `ClassificationRegressionTest` 50 케이스 실행 완료 + `report.json` 생성
- [ ] `fetch-regression-report.sh`로 정확도·지연·오답 추출
- [ ] Qwen 1.5B 대비 비교 표 작성

v0.2 진행 결정:
- Gemma datetime 정확도 ≥ 0.85 (spec 목표) → v0.2 plan 그대로 진행
- 0.85 미만이지만 0.82 이상 → v0.2 진행하되 prompt engineering follow-up
- 0.82 미만 → 모델 재검토 (더 큰 변종 또는 prompt 보강 우선)

---

## 의도적 미룬 항목

| 항목 | 미루는 이유 | 처리 시점 |
|---|---|---|
| 다중 변종 (E2B + E4B) | v0.3 spec 명시 항목 | v0.3 |
| HF token / Authorization 인프라 | Gemma 4 E2B가 ungated라 불필요 | 다른 gated 모델 필요 시 |
| 모델 unload 백그라운드 5분 정책 | 별도 polish | P3 |
| 새 prompt engineering (few-shot for 한국어 EVENT) | 회귀 결과 확인 후 결정 | 결과 따라 |
| Gemma 카드/라이선스 표시 (About 화면) | 라이선스 화면 자체가 v1.0 polish | v1.0 |
