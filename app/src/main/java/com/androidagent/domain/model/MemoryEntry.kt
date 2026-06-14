package com.androidagent.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MemoryEntry(
    val id: Long = 0,
    val type: MemoryType,
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis(),
    val accessCount: Int = 0,
    val expiresAt: Long = 0
)

@Serializable
enum class MemoryType {
    SHORT_TERM,
    LONG_TERM
}
