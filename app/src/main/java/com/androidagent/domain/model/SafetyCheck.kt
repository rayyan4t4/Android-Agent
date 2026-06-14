package com.androidagent.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SafetyCheck(
    val action: AgentAction,
    val riskLevel: RiskLevel,
    val reason: String,
    val requiresApproval: Boolean,
    val approved: Boolean = false
)

@Serializable
enum class RiskLevel {
    SAFE,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
