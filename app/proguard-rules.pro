# kotlinx.serialization — @Serializable 생성 serializer 보존
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * { @kotlinx.serialization.Serializable <methods>; }
-keep,includedescriptorclasses class com.just.**$$serializer { *; }
-keepclassmembers class com.just.** {
    *** Companion;
}
-keepclasseswithmembers class com.just.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# LiteRT-LM (JNI) 보존
-keep class com.google.ai.edge.litertlm.** { *; }
-keepclassmembers class com.google.ai.edge.litertlm.** { *; }
