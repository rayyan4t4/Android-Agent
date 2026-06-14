package com.androidagent.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AgentGoal(
    val id: String,
    val description: String,
    val createdAt: Long,
    val status: GoalStatus = GoalStatus.PENDING,
    val subGoals: List<SubGoal> = emptyList()
)

@Serializable
data class SubGoal(
    val id: String,
    val description: String,
    val status: GoalStatus = GoalStatus.PENDING,
    val attempts: Int = 0
)

@Serializable
enum class GoalStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    SKIPPED
}
