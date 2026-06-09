# 릴리스 서명 가이드

릴리스 APK/AAB에 서명하려면 루트에 `keystore.properties`(gitignored)를 만든다.
파일이 없으면 release 빌드는 **unsigned**로 성공한다(CI/시크릿 없는 환경 보호).

## 1. keystore 생성 (최초 1회)
```bash
keytool -genkeypair -v -keystore release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 -alias assistant
```
> `release.jks`는 안전한 곳에 보관하고 **절대 커밋하지 않는다**(`*.jks` ignore됨). 분실 시 Play 업데이트 불가.

## 2. keystore.properties 작성 (루트)
```
storeFile=/absolute/path/to/release.jks
storePassword=****
keyAlias=assistant
keyPassword=****
```

## 3. 서명된 release 빌드
```bash
./gradlew :app:assembleRelease
```
`storeFile` 프로퍼티가 감지되면 `signingConfigs.release`가 적용되어 서명된 APK가 생성된다.

## R8 / 디버깅
- keep 규칙: `app/proguard-rules.pro`(kotlinx-serialization $serializer, LiteRT-LM JNI).
- 난독화 역추적: `app/build/outputs/mapping/release/mapping.txt`.
- R8 후 직렬화/JNI 경로 정합성은 실기기 smoke로 최종 확인.
