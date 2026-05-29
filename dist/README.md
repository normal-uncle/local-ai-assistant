# dist/ — Distribution assets

이 폴더는 앱의 모델 카탈로그를 호스팅합니다.
GitHub raw URL로 직접 접근:

- `model_catalog.json` — 카탈로그 manifest (Gemma 4 E2B 단일 변종, `.litertlm` 포맷)

## 현재 변종

| ID | 사이즈 | 최소 RAM | 비고 |
|---|---|---|---|
| gemma-4-E2B-it | ~2.4GB | 4GB | Spec 의도. LiteRT-LM SDK 필요. ungated |

`DeviceProfiler`가 기기 RAM ≥ 4GB 확인하고 `VariantSelector`가 단일 권장 변종 선택.

## 히스토리

- **2026-05-29**: `.litertlm` 포맷으로 전환. LiteRT-LM SDK 0.12.0 도입.
- 2026-05-28 (실패): `gemma-4-E2B-it-web.task`로 시도했으나 MediaPipe Tasks GenAI가 LiteRT-LM `.task` 변종 zip을 못 열어 실패.
- 2026-05-26 (P2.C): Qwen 2.5 0.5B + 1.5B 변종. Gemma agreement 우회용 baseline.
- 2026-05-22 (P2.B): Qwen 2.5 0.5B 단일.
- 2026-05-21 (P2.A): 1MB dev-fixture 더미.

## v0.3 계획

E2B + E4B 다중 변종 — `gemma-4-E4B-it.litertlm` (litert-community) 추가 검증.
