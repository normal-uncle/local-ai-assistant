# kotlinx.serialization — @Serializable 생성 serializer 보존 (com.just 한정)
# 라이브러리가 자체 consumer 규칙을 동봉하므로 프로젝트 클래스 보존만 명시
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.just.**$$serializer { *; }
-keepclassmembers class com.just.** {
    *** Companion;
}
-keepclasseswithmembers class com.just.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# LiteRT-LM (JNI) 보존
-keep class com.google.ai.edge.litertlm.** { *; }
