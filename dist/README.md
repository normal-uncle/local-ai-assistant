# dist/ — Distribution assets

이 폴더는 앱의 모델 카탈로그를 호스팅합니다. GitHub raw URL로 직접 접근:

- `model_catalog.json` — 카탈로그 manifest

실제 모델 바이너리는 Hugging Face `litert-community` org에서 호스팅 (인증 불필요,
HTTP 302 → S3 CDN redirect).

## 현재 변종 (P2.B)

`Qwen2.5-0.5B-Instruct_multi-prefill-seq_q8_ekv1280` — `522MB` `.task` 파일,
MediaPipe LLM Inference 0.10.x 호환. Hugging Face `litert-community` org에서 호스팅.

Gemma 계열(`litert-community/Gemma*`, `google/gemma-*-task`)은 모두 HF gated repo
(Gemma license agreement 필요)라 익명 다운로드 불가. 따라서 동급 사이즈의 비-게이트
모델인 Qwen2.5-0.5B int8 `.task`를 선택. MediaPipe Tasks GenAI 0.10.x API
(`LlmInference.createFromOptions`)에서 동일하게 동작.

## URL 전환 메모

P2.B 개발 중에는 `feature/v0.1-p2b-plan` branch URL을 가리키도록 `AppConfigModule`이
설정됨. 머지 후 master URL로 변경 필요.
