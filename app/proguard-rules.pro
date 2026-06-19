# ============================================================================
# 루밍(Luming) R8 규칙
# isMinifyEnabled=true 로 R8 이 이 파일을 읽어 난독화/축소/최적화를 수행한다.
# ============================================================================

# 디버깅용 스택트레이스를 위해 줄 번호는 보존하되 원본 파일명은 가린다.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ----------------------------------------------------------------------------
# kotlinx.serialization
# (@Serializable 클래스의 생성된 serializer / Companion 보존)
# ----------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}
# 본 앱 모델의 합성 $$serializer 클래스 보존
-keep,includedescriptorclasses class io.github.rmflawkdyd.luming.**$$serializer { *; }
-keepclassmembers class io.github.rmflawkdyd.luming.** {
    *** Companion;
}

# ----------------------------------------------------------------------------
# Retrofit / OkHttp (대부분 라이브러리 동봉 규칙으로 처리되나 방어적으로 명시)
# ----------------------------------------------------------------------------
-keepattributes Signature, Exceptions
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

# ----------------------------------------------------------------------------
# TFLite / LiteRT (JNI 네이티브 바인딩 보존)
# ----------------------------------------------------------------------------
-keep class org.tensorflow.lite.** { *; }
-keep class com.google.ai.edge.litert.** { *; }
-dontwarn org.tensorflow.lite.**
-dontwarn com.google.ai.edge.litert.**

# ----------------------------------------------------------------------------
# 로그 제거 — release 바이트코드에서 디버그/verbose 로그 호출 자체를 삭제한다.
# (반올림 좌표·날씨 응답 등이 logcat 으로 새는 것을 원천 차단)
# ----------------------------------------------------------------------------
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
