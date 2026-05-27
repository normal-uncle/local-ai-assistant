# dist/ — Distribution assets

이 폴더는 앱의 모델 카탈로그를 호스팅합니다.
GitHub raw URL로 직접 접근:

- `model_catalog.json` — 카탈로그 manifest (변종 2개)

## 변종 (P2.C)

| ID | 사이즈 | 최소 RAM | 권장 |
|---|---|---|---|
| Qwen2.5-0.5B-Instruct (q8) | 522MB | 4GB | 저사양 fallback |
| Qwen2.5-1.5B-Instruct (q8) | ~1.5GB | 8GB | 고사양 권장 (분류 정확도 더 높음) |

`DeviceProfiler`가 기기 RAM을 측정해 `VariantSelector`가 자동으로 최적 변종 선택.

v0.2부터는 실제 Gemma 4 E2B / E4B로 교체 예정. 호스팅도 더 큰 파일에 적합한
CDN(Cloudflare R2 또는 Hugging Face의 Gemma org auth)으로 이전.
