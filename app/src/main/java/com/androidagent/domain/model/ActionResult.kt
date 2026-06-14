package com.androidagent.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ActionResult(
    val success: Boolean,
    val action: AgentAction,
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0
)
