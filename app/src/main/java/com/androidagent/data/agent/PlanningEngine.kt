package com.androidagent.data.agent

import com.androidagent.data.llm.PromptBuilder
import com.androidagent.data.llm.ToolCallParser
import com.androidagent.domain.model.*
import com.androidagent.domain.repository.LlmRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlanningEngine @Inject constructor(
    private val llmRepository: LlmRepository,
    private val promptBuilder: PromptBuilder,
    private val toolCallParser: ToolCallParser
) {
    suspend fun createPlan(goal: AgentGoal, screenState: ScreenState): AgentPlan {
        val prompt = promptBuilder.buildPlanningPrompt(goal.description, screenState)
        val response = llmRepository.generate(prompt, maxTokens = 256)
        val parsedSteps = toolCallParser.parsePlanSteps(response.text)

        val steps = if (parsedSteps.isNotEmpty()) {
            parsedSteps.mapIndexed { index, (desc, tool) ->
                PlanStep(
                    index = index,
                    description = desc,
                    toolName = tool,
                    status = StepStatus.PENDING
                )
            }
        } else {
            listOf(
                PlanStep(0, "Observe current screen", "ReadScreen"),
                PlanStep(1, "Execute goal: ${goal.description}", ""),
                PlanStep(2, "Verify completion", "ReadScreen")
            )
        }

        return AgentPlan(
            goalId = goal.id,
            steps = steps,
            reasoning = response.text
        )
    }

    fun updatePlanAfterAction(plan: AgentPlan, stepIndex: Int, success: Boolean): AgentPlan {
        val updatedSteps = plan.steps.toMutableList()
        updatedSteps[stepIndex] = updatedSteps[stepIndex].copy(
            status = if (success) StepStatus.COMPLETED else StepStatus.FAILED
        )
        val nextIndex = if (success) stepIndex + 1 else stepIndex
        val planStatus = when {
            updatedSteps.all { it.status == StepStatus.COMPLETED } -> PlanStatus.COMPLETED
            !success && updatedSteps[stepIndex].status == StepStatus.FAILED -> PlanStatus.REPLANNING
            else -> PlanStatus.ACTIVE
        }
        return plan.copy(
            steps = updatedSteps,
            currentStepIndex = nextIndex,
            status = planStatus
        )
    }
}
