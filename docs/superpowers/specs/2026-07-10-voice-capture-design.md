# 음성 캡처 (녹음 → Gemma 직접 분류) 설계

날짜: 2026-07-10
상태: 승인됨

## 목표

캡처 화면에서 음성으로 메모/일정/리마인더를 등록한다. 녹음된 음성을 Gemma 4 E2B에 직접 넣어
한 번의 온디바이스 추론으로 분류(MEMO/EVENT/REMINDER + 제목/본문/태그/일시)까지 끝낸다.
기존 시스템 STT(RecognizerIntent) 버튼은 제거하고 이 경로로 교체한다 — 완전 오프라인 보장.

## 결정 사항

- **직접 분류**: 전사→텍스트 분류 2단계가 아닌, 오디오→분류 1회 추론. 결과는 기존 PreviewSheet에서 수정 가능.
- **탭 토글 녹음**: 탭으로 시작, 재탭으로 종료. 경과 시간 표시, 최대 30초 자동 종료.
- **STT 교체**: RecognizerIntent 버튼/문자열 제거.
- **전제**: litertlm-android **0.14.0** (0.12.0은 오디오 임베딩을 조용히 무시 — 2026-07-09 검증).

## 컴포넌트

| 모듈 | 신규/변경 | 내용 |
|---|---|---|
| `:local` | 신규 `local/audio/AudioRecorder.kt` | `AudioRecorder` 인터페이스 + `AudioRecorderImpl`(TtsSpeaker와 동일 DI 패턴). `AudioRecord` 16kHz mono PCM16 → `stop()`이 WAV 바이트 반환. 최대 30초 자동 종료. 파일 저장 없음(메모리 ~960KB). WAV 헤더 래핑은 순수 함수로 분리해 단위 테스트. |
| `:ai` | 신규 `prompt/AudioClassificationPrompt.kt` | ImageClassificationPrompt 미러. "음성 메모를 분류하는 비서" task형 지시(전사 요청형 문구는 텍스트 모드 거부를 유발하므로 금지). 동일 JSON 스키마, 입력창 텍스트를 caption으로 동봉 가능. |
| `:usecase` | 신규 `capture/di/ClassifyAudioCaptureUseCase.kt` + impl | ClassifyImageCaptureUseCase 미러. `engine.load(InferenceConfig(enableAudio = true))` → `generate(prompt, images = emptyList(), audios = listOf(wav))` → `ClassificationResult.parse`. 모델 미준비/실패 시 null. |
| `:feature_capture` | 변경 | `CaptureViewModel`: `isRecording`/경과초 상태, `onToggleRecording()`, `onPrepareAudio()`. `CaptureScreen`: RecognizerIntent 버튼 제거 → 녹음 토글 버튼("음성 녹음" ↔ "중지 (N초)"), RECORD_AUDIO 런타임 권한(카메라 launcher 패턴). |
| `:app` | 변경 | 매니페스트 `RECORD_AUDIO` 권한 추가. |
| strings | 변경 | 신규: 녹음 시작/중지, 권한 거부 안내. 제거: capture_voice_input/voice_prompt/voice_unavailable (ko/en). |

## 데이터 플로우

탭 → 권한 확인/요청 → `AudioRecorder.start()` → 재탭(또는 30초) → `stop(): ByteArray(WAV)`
→ `isPreparing = true` → `ClassifyAudioCaptureUseCase(wav, caption = 입력창 텍스트?)`
→ `ClassificationResult` → `CapturePreview` → PreviewSheet 수정/확정 → 기존 저장·일정등록 플로우.

## 에러 처리

- 권한 거부: 토스트 안내, 녹음 시작 안 함.
- 분류 실패/모델 미준비(null): 이미지 경로와 동일한 MEMO 폴백 프리뷰(입력창 텍스트 또는 폴백 제목).
- `AudioRecord` 초기화 실패: 토스트, 상태 원복.

## 테스트

- `AudioClassificationPromptTest` — 스키마 키/캡션 포함 여부 (ImageClassificationPromptTest 미러).
- `ClassifyAudioCaptureUseCaseImplTest` — fake 엔진으로 enableAudio 로드, 오디오 1개 전달, null 경로들 (Image 테스트 미러).
- WAV 인코딩 순수 함수 단위 테스트 (RIFF 헤더, 길이 필드).
- `CaptureViewModelTest` 확장 — 녹음 토글 상태 전이, prepareAudio 성공/폴백 (fake recorder + fake usecase).
- 엔진 실경로: 기존 `AudioInferenceSpikeTest`(디바이스)가 커버. 마이크 E2E는 수동 확인.
