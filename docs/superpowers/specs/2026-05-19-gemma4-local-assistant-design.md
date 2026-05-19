# Gemma 4 로컬 AI 안드로이드 비서 — 설계 문서

- **작성일:** 2026-05-19
- **작성자:** kichan
- **상태:** Draft — 사용자 리뷰 대기

---

## 1. 개요

Gemma 4를 안드로이드 기기에서 온디바이스로 구동하는 **일상 비서 앱**. 사용자가 음성·텍스트·이미지로 입력한 내용을 AI가 자동 분류(메모/일정/리마인더)하여 저장하고, 안드로이드 캘린더·알람 등 OS 기능과 연동한다.

### 1.1 차별점

- **온디바이스**: 입력 데이터가 기기 밖으로 나가지 않음 (프라이버시·오프라인).
- **멀티모달 캡처**: 음성·텍스트·이미지 어떤 입력이든 한 곳에서 받아 AI가 분류.
- **OS 깊은 연동**: 캘린더·알람·공유 인텐트까지.

### 1.2 목표 / 비목표

**목표**
- Play Store 정식 출시 (한국·미국 시장).
- 분류 정확도 ≥ 90%, datetime 추출 ≥ 85%.
- Pixel 8급 기기에서 분류 추론 P50 1.5s / P95 3.5s.

**비목표 (현재 스코프 외)**
- 클라우드 동기화·다중 기기 (v2+).
- 사용자 간 공유·협업.
- 외부 캘린더 서비스(구글 캘린더) 직접 API 연동 — 안드로이드 캘린더 프로바이더 경유로 충분.

---

## 2. 기술 스택

| 영역 | 선택 | 이유 |
|---|---|---|
| 모델 | Gemma 4 **E2B** (4-bit 양자화)로 시작 | 스마트폰 타깃 변종, 메모리 ~2GB, 발열 마진. v0.3에서 E4B 변종 추가 |
| 추론 엔진 | **MediaPipe LLM Inference** (Google AI Edge) | Gemma 직접 지원, GPU/NNAPI 가속, 이미지 입력 지원, Apache 2.0 |
| 음성 입력 | 안드로이드 **SpeechRecognizer** (온디바이스, API 33+) | Gemma audio 직접 호출보다 안정적·저전력. 부분 결과 지원 |
| TTS | 안드로이드 TextToSpeech | v0.1 옵셔널 |
| 로컬 DB | **Room (SQLite)** | 표준. v0.4에서 FTS5로 검색 확장 |
| 벡터 검색 (v0.5+) | Room + **sqlite-vec** + MediaPipe Text Embedder | 외부 의존 최소 |
| DI | Hilt | |
| UI | Jetpack Compose + Material 3 | |
| 비동기 | Kotlin Coroutines + Flow | |
| 최소 SDK | API 31 (Android 12) | NNAPI 안정·온디바이스 STT |
| 타깃 SDK | API 35 (Android 15) | 최신 권한 정책 |

**모델 크기 트레이드오프 정책**: v0.1은 E2B 단일 변종. v0.3에서 E4B 변종 추가 + `DeviceProfiler` 매칭 활성. 카탈로그 구조는 v0.1부터 갖춤.

---

## 3. 시스템 아키텍처

### 3.1 모듈 구조

```
:app                              # MainActivity, Navigation host, Application
:core:designsystem                # Compose theme, components
:core:database                    # Room (Note, Tag, AISession 엔티티)
:core:datastore                   # 설정·온보딩·모델 다운로드 상태
:core:ai                          # AI 추론 추상화 레이어
    ├─ InferenceEngine (interface)
    ├─ MediaPipeInferenceEngine (구현)
    ├─ PromptTemplates
    ├─ ModelManager (다운로드·체크섬·버전·카탈로그)
    ├─ DeviceProfiler (RAM/SoC 매칭 — v0.3 활성)
    └─ schemas/ (structured output JSON 스키마)
:feature:capture                  # 입력 화면 (음성/텍스트, v0.3 이미지)
:feature:memo                     # 목록·상세·검색
:feature:settings                 # 모델·권한·테마

# 미래 (v0.2+)
:feature:calendar
:feature:summary
:core:embeddings
:core:rag
```

### 3.2 AI 모듈 인터페이스

```kotlin
interface InferenceEngine {
    suspend fun classify(input: CaptureInput): ClassificationResult
    suspend fun summarize(text: String): String
    fun stream(prompt: Prompt): Flow<TextChunk>
}
```

추론 엔진 구현 교체와 테스트 시 Fake 주입을 가능하게 한다.

### 3.3 구조화 출력 정책

AI 출력은 항상 JSON 스키마를 강제한다. 프롬프트에 스키마를 박고 `temperature=0.2`로 호출.

```json
{
  "type": "memo|event|reminder",
  "title": "string",
  "tags": ["string"],
  "datetime": "ISO8601 | null",
  "body": "string"
}
```

JSON 파싱 실패 시 1회 retry (temperature 추가 하향) → 그래도 실패하면 **MEMO 타입으로 폴백 저장**. 입력은 절대 손실되지 않는다.

### 3.4 모델 카탈로그 (디바이스별 변종)

서버(CDN)에 `model_catalog.json` 호스팅:

```json
{
  "version": "YYYY-MM-DD",
  "variants": [
    {
      "id": "gemma4-e2b-q4",
      "url": "...",
      "sha256": "...",
      "sizeMb": 1500,
      "minRamGb": 6,
      "recommended": true
    }
  ]
}
```

- `DeviceProfiler.profile()`이 RAM·SoC·Vulkan 지원을 검사하고 카탈로그와 매칭하여 최적 변종 선택.
- 사용자는 설정에서 수동 변경 가능.
- v0.1은 변종 1개로 시작, 인프라만 갖춤.

### 3.5 모델 다운로드

- E2B 4-bit ≈ 1.5GB. APK에 동봉 불가.
- 첫 실행 시 온보딩에서 다운로드 진행. WorkManager + Range 요청으로 재개 가능, SHA256 검증.
- `context.filesDir`에 저장, 백업 제외 플래그.
- 시작 전 4GB 여유 공간 검사.
- Wi-Fi 권장 / 셀룰러 확인 다이얼로그.

### 3.6 데이터 흐름 (v0.1)

```
[Capture 화면]
   ├─ 음성 ─► AndroidSpeechRecognizer (온디바이스)
   │              └─► 텍스트 transcript (실시간 부분 결과)
   ├─ 텍스트
   └─► CaptureViewModel.onSubmit(text)
            ▼
       AIEngine.classify(text)   [:core:ai]
            ▼
       ClassificationResult { type, title, tags, datetime?, body }
            ▼
       [Preview Sheet]   ← 사용자가 분류·제목·태그 수정 가능
            ▼ Confirm
       NoteRepository.save(note) → Room
            ▼
       [Memo 목록]   ← StateFlow로 갱신
```

핵심 결정:

- **음성은 STT만 사용, audio를 Gemma에 직접 입력하지 않음** (v0.1). 모델 부하 분산·발열·배터리 이유. Gemma audio는 v0.3+ 검토.
- **Preview Sheet 강제**: AI 분류 결과를 사용자가 1초 안에 수정 후 저장. 학습 데이터로도 활용 (v1.0+).
- **모델 prewarm**: Application.onCreate에서 백그라운드로 모델 적재 시작.
- **캔슬 가능**: Coroutine 캔슬로 추론 중단.

### 3.7 단방향 데이터 흐름

UI → ViewModel → UseCase → Repository → (Room | AI Engine | DataStore). Compose는 StateFlow 구독.

---

## 4. 에러 처리

| 시나리오 | 처리 |
|---|---|
| 모델 미다운로드 / 손상 | 온보딩 강제 이동, SHA256 재검증 후 재다운로드 |
| 다운로드 중 네트워크 끊김 | WorkManager + Range로 재개. 백그라운드 알림 |
| 추론 OOM | 1회 retry → 실패 시 토스트 + MEMO 폴백 저장 |
| 추론 타임아웃 (>10s) | 캔슬 + MEMO 폴백 저장 + 안내 |
| JSON 파싱 실패 | retry (낮은 temp) → 실패 시 MEMO 폴백 |
| 마이크 권한 거부 | "텍스트 입력만" 배너 + 설정 deeplink |
| STT 무음/실패 | 부분 결과 있으면 그것만 사용, 없으면 재시도 안내 |
| 백그라운드 전환 중 추론 | ForegroundService 승격 |
| 발열 SEVERE | `PowerManager.thermalStatus` 모니터링, 추론 일시 거부 |
| 저장 공간 부족 | 다운로드 전 4GB 검사, 부족 시 차단 |

---

## 5. 성능

### 5.1 예산 (v0.1, Pixel 8급)

| 지표 | 목표 |
|---|---|
| Cold start → Capture 화면 | < 1.5s |
| 모델 로드 (메모리 적재) | 백그라운드 prewarm, 사용자 미체감 |
| 분류 추론 (200 토큰) | P50 1.5s / P95 3.5s |
| 메모리 풋프린트 (추론 중) | < 2.5GB peak |
| 배터리 (10회 분류) | < 1% |

### 5.2 기법

- 추론은 `Dispatchers.Default` 별도 SupervisorScope에서.
- 동일 입력 5분 캐시.
- 컨텍스트 1500 토큰 초과 시 입력 자르기.
- GPU 가속 설정 토글 (일부 기기 회피용).
- 백그라운드 5분 후 모델 unload, foreground 복귀 시 재로드.

### 5.3 호환성

- 최소 RAM 6GB (Play Store 디바이스 카탈로그 필터 + 런타임 체크).
- API 31+ (Android 12+).

---

## 6. 테스트 전략

### 6.1 피라미드

- **Unit (JUnit5 + Turbine + MockK)**: ViewModel, UseCase, Mapper, JSON 파서, DeviceProfiler. AI는 Fake 주입.
- **Integration (Robolectric)**: Room DAO (in-memory), Repository, ModelManager (Fake HTTP).
- **Instrumented (Compose UI Test)**: Capture → Preview Sheet → 저장 → 목록 end-to-end. 권한 거부 시 배너.
- **Manual QA**: 실기기 발열·배터리, 30분 연속 사용, 다국어, 접근성.

### 6.2 골든 데이터셋 (AI 회귀)

```
test/resources/golden/
  ├─ classification.jsonl     # 200~500 케이스
  ├─ event_extraction.jsonl
  └─ summarization.jsonl      # v0.4+
```

- 한·영 일상 표현. 변종(E2B/E4B)마다 정확도·P95 지연 기록.
- 기준: 분류 ≥ 90%, datetime ≥ 85%.
- 로컬 / Firebase Test Lab 야간 잡 실행 (CI 부담 회피).

### 6.3 디바이스 매트릭스 (v1.0 전)

| 등급 | 예시 |
|---|---|
| 저사양 (6GB) | Galaxy A35 |
| 중사양 (8GB) | Galaxy A55, Pixel 7a |
| 고사양 (12GB+) | Pixel 8 Pro, S24 |
| 태블릿 | Tab S9 |

Firebase Test Lab + 실기기 2~3대.

---

## 7. 로드맵

각 버전은 자체 출시 가능한 빌드 (내부/베타 채널 포함).

### v0.1 — Capture + 분류 MVP (~4주)
음성/텍스트 입력, STT, Gemma 분류, Preview Sheet, Room 저장, 메모 목록. 카탈로그 인프라 + E2B-q4 단일 변종. 온보딩. **검증**: 실기기 추론 성능·발열·OOM 예산 충족.

### v0.2 — 캘린더·알람 (~3주)
EVENT/REMINDER → 안드로이드 캘린더·AlarmManager 등록. 권한 (Calendar, POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM). **검증**: datetime 추출 정확도 골든셋 ≥ 85%.

### v0.3 — 이미지 + 디바이스 변종 (~4주)
사진 입력 (카메라/갤러리). E4B-q4 변종 추가, `DeviceProfiler` 활성. 영수증·화이트보드 사진 처리. **검증**: E4B 고사양 안정, 저사양 E2B 폴백.

### v0.4 — 요약 + 공유 인텐트 (~3주)
ACTION_SEND 핸들러. 긴 글 요약·키포인트·다음 액션. 메모 검색 (Room FTS5). **검증**: 1500토큰 입력 지연·메모리.

### v0.5 — RAG 베타 (~3주, 옵션)
sqlite-vec + Text Embedder. 자연어 메모 검색. 백그라운드 임베딩 인덱싱.

### v1.0 — 출시 (~3주)
다국어 (한·영), 접근성 (TalkBack, 동적 글자 크기), 권한·프라이버시 정책, Play Store 메타데이터, Crashlytics, 단계 출시 (내부 → 비공개 → 프로덕션).

### 총 일정 (1인 풀타임)

- v0.1~v0.4 (코어 기능): ~14주
- + v1.0 (출시 작업): +3주 → **코어+출시: ~17주**
- + v0.5 (RAG, 옵션): +3주 → **RAG 포함 출시: ~20주 (5개월)**

투자/홍보 일정 압박이 있으면 v0.5는 v1.1로 미루고 17주 경로 권장.

---

## 8. 오픈 이슈 / 추후 결정

- TTS 활성화 시점 (v0.4 또는 v1.0).
- 라이선스 표기 (Gemma 4 Apache 2.0 + MediaPipe Apache 2.0) — About 화면 구성.
- 사용자 피드백 수집 채널 (인앱 → 이메일? 또는 Play Store 평가 deeplink만).
- 분석/텔레메트리 정책 — 온디바이스 컨셉상 최소화. 크래시(Crashlytics)만, 사용자 동의 후.
- 한국어 STT 정확도 검증 (특히 일정 표현 "내일모레", "다음주 화요일").
