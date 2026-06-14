-dontwarn kotlinx.serialization.**
-keep class com.androidagent.domain.model.** { *; }
-keep class com.androidagent.data.llm.LlamaCppEngine { *; }
-keepclassmembers class com.androidagent.data.llm.LlamaCppEngine {
    native <methods>;
}
