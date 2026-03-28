-repackageclasses ''
-allowaccessmodification
-overloadaggressively

-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

-keepattributes SourceFile,LineNumberTable

-keepclassmembers class * extends androidx.compose.runtime.Composer { *; }
-keep class androidx.compose.runtime.Recomposer { *; }

-keepclassmembers class * {
    @org.koin.core.annotation.KoinInternalApi *;
}

-keep class com.arkivanov.decompose.** { *; }

-keepattributes *Annotation*, EnclosingMethod, InnerClasses
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}

-keep class org.drinkless.tdlib.** { *; }