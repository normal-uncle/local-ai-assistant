# dist/ — Distribution assets

이 폴더는 앱의 모델 카탈로그와 dev 단계 모델 fixture를 호스팅합니다.
GitHub raw URL로 직접 접근:

- `model_catalog.json` — 카탈로그 manifest
- `dev-fixture.bin` — P2.A 인프라 검증용 1MB dummy binary

v0.2 부터는 실제 Gemma 4 E2B 4-bit 모델로 교체 예정. 호스팅도 더 큰 파일에 적합한
CDN(Cloudflare R2 또는 Hugging Face)으로 이전.

## URL 전환 메모

P2.A 개발 중에는 `feature/v0.1-p2-plan` brach URL을 가리키도록 `AppConfigModule`이
설정됨. 머지 후 master URL로 변경 필요.
