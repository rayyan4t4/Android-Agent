package com.androidagent.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AgentPlan(
    val goalId: String,
    val steps: List<PlanStep>,
    val currentStepIndex: Int = 0,
    val status: PlanStatus = PlanStatus.ACTIVE,
    val reasoning: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val currentStep: PlanStep? get() = steps.getOrNull(currentStepIndex)
    val isComplete: Boolean get() = currentStepIndex >= steps.size || status == PlanStatus.COMPLETED
}

@Serializable
data class PlanStep(
    val index: Int,
    val description: String,
    val toolName: String = "",
    val status: StepStatus = StepStatus.PENDING,
    val result: String = ""
)

@Serializable
enum class PlanStatus {
    ACTIVE,
    COMPLETED,
    FAILED,
    REPLANNING
}

@Serializable
enum class StepStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    SKIPPED
}
