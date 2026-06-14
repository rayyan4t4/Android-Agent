package com.androidagent.domain.model

data class LlmResponse(
    val text: String,
    val tokensGenerated: Int = 0,
    val tokensPrompt: Int = 0,
    val generationTimeMs: Long = 0,
    val tokensPerSecond: Float = 0f,
    val stopReason: StopReason = StopReason.END_OF_TEXT
)

enum class StopReason {
    END_OF_TEXT,
    MAX_TOKENS,
    STOP_SEQUENCE,
    ERROR
}
