package com.androidagent.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AgentLog(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val category: LogCategory,
    val message: String,
    val details: String = ""
)

@Serializable
enum class LogLevel {
    DEBUG,
    INFO,
    WARNING,
    ERROR
}

@Serializable
enum class LogCategory {
    AGENT,
    PLANNING,
    TOOL,
    LLM,
    PERCEPTION,
    MEMORY,
    SAFETY,
    SYSTEM
}
