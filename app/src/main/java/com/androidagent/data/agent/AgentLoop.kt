package com.androidagent.data.agent

import com.androidagent.data.llm.PromptBuilder
import com.androidagent.data.llm.ToolCallParser
import com.androidagent.domain.model.*
import com.androidagent.domain.repository.ActionRepository
import com.androidagent.domain.repository.LlmRepository
import com.androidagent.domain.repository.MemoryRepository
import com.androidagent.domain.repository.ScreenRepository
import com.androidagent.domain.usecase.CheckSafetyUseCase
import com.androidagent.data.logging.AgentLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentLoop @Inject constructor(
    private val screenRepository: ScreenRepository,
    private val llmRepository: LlmRepository,
    private val actionRepository: ActionRepository,
    private val memoryRepository: MemoryRepository,
    private val promptBuilder: PromptBuilder,
    private val toolCallParser: ToolCallParser,
    private val planningEngine: PlanningEngine,
    private val checkSafety: CheckSafetyUseCase,
    private val logger: AgentLogger
) {
    private val _state = MutableStateFlow(AgentState.IDLE)
    val state: StateFlow<AgentState> = _state.asStateFlow()

    private val _currentGoal = MutableStateFlow<AgentGoal?>(null)
    val currentGoal: StateFlow<AgentGoal?> = _currentGoal.asStateFlow()

    private val _currentPlan = MutableStateFlow<AgentPlan?>(null)
    val currentPlan: StateFlow<AgentPlan?> = _currentPlan.asStateFlow()

    private val _actionHistory = MutableStateFlow<List<String>>(emptyList())
    val actionHistory: StateFlow<List<String>> = _actionHistory.asStateFlow()

    private val _pendingApproval = MutableStateFlow<SafetyCheck?>(null)
    val pendingApproval: StateFlow<SafetyCheck?> = _pendingApproval.asStateFlow()

    private var loopJob: Job? = null
    private var maxIterations = 50
    private var iterationCount = 0

    fun start(goalDescription: String, scope: CoroutineScope) {
        if (_state.value != AgentState.IDLE) return

        val goal = AgentGoal(
            id = System.currentTimeMillis().toString(),
            description = goalDescription,
            createdAt = System.currentTimeMillis(),
            status = GoalStatus.IN_PROGRESS
        )
        _currentGoal.value = goal
        _actionHistory.value = emptyList()
        iterationCount = 0

        loopJob = scope.launch(Dispatchers.Default) {
            logger.logAgent("Starting agent for goal: $goalDescription")
            _state.value = AgentState.PLANNING

            val plan = planningEngine.createPlan(goal, screenRepository.getScreenState())
            _currentPlan.value = plan
            logger.logPlanning("Plan created with ${plan.steps.size} steps")

            runLoop(goal)
        }
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
        _state.value = AgentState.IDLE
        _currentGoal.value = null
        _currentPlan.value = null
        _pendingApproval.value = null
        logger.logAgent("Agent stopped by user")
    }

    fun approveAction(approved: Boolean) {
        val check = _pendingApproval.value ?: return
        _pendingApproval.value = check.copy(approved = approved)
    }

    private suspend fun runLoop(goal: AgentGoal) {
        while (iterationCount < maxIterations && _state.value != AgentState.IDLE) {
            try {
                iterationCount++

                _state.value = AgentState.OBSERVING
                val screenState = screenRepository.getScreenState()

                _state.value = AgentState.THINKING
                val planContext = _currentPlan.value?.let { plan ->
                    plan.currentStep?.let { "Step ${plan.currentStepIndex + 1}/${plan.steps.size}: ${it.description}" } ?: ""
                } ?: ""

                val prompt = promptBuilder.buildAgentPrompt(
                    goal = goal.description,
                    screenState = screenState,
                    recentActions = _actionHistory.value.takeLast(5),
                    planContext = planContext
                )

                val llmResponse = llmRepository.generate(prompt, maxTokens = 512)
                logger.logLlm("Generated ${llmResponse.tokensGenerated} tokens at ${String.format("%.1f", llmResponse.tokensPerSecond)} t/s")

                val (thinking, toolCall) = toolCallParser.parse(llmResponse.text)
                if (thinking.isNotBlank()) {
                    logger.logAgent("Thinking: $thinking")
                }

                if (toolCall == null) {
                    logger.logAgent("No tool call parsed, retrying")
                    continue
                }

                val action = toolCall.toAction()
                if (action == null) {
                    logger.logAgent("Unknown tool: ${toolCall.toolName}")
                    continue
                }

                val safetyCheck = checkSafety(action, screenState.toCompactString())
                if (safetyCheck.requiresApproval) {
                    _state.value = AgentState.WAITING_APPROVAL
                    _pendingApproval.value = safetyCheck
                    logger.logSafety("Approval required: ${safetyCheck.reason}")

                    while (_pendingApproval.value?.approved == false && _pendingApproval.value?.requiresApproval == true) {
                        delay(500)
                        if (_state.value == AgentState.IDLE) return
                    }

                    val approvalResult = _pendingApproval.value
                    _pendingApproval.value = null
                    if (approvalResult?.approved != true) {
                        logger.logSafety("Action rejected by user")
                        continue
                    }
                }

                _state.value = AgentState.ACTING
                val result = actionRepository.execute(action)
                val historyEntry = "${action.description} -> ${if (result.success) "OK" else "FAILED: ${result.message}"}"
                _actionHistory.value = _actionHistory.value + historyEntry
                logger.logTool("${action.description}: ${if (result.success) "success" else "failed"}")

                delay(500)

                _state.value = AgentState.VERIFYING
                val newScreen = screenRepository.getScreenState()
                val reflectionPrompt = promptBuilder.buildReflectionPrompt(
                    goal = goal.description,
                    lastAction = action.description,
                    result = if (result.success) "Success" else "Failed: ${result.message}",
                    screenState = newScreen
                )
                val reflectionResponse = llmRepository.generate(reflectionPrompt, maxTokens = 128)
                val (nextAction, reason) = toolCallParser.parseReflectionResult(reflectionResponse.text)

                when (nextAction) {
                    "abort" -> {
                        logger.logAgent("Aborting: $reason")
                        _state.value = AgentState.ERROR
                        return
                    }
                    "replan" -> {
                        logger.logPlanning("Replanning: $reason")
                        val newPlan = planningEngine.createPlan(goal, newScreen)
                        _currentPlan.value = newPlan
                    }
                    "retry" -> {
                        logger.logAgent("Retrying: $reason")
                    }
                    else -> {
                        _currentPlan.value?.let { plan ->
                            if (plan.currentStepIndex < plan.steps.size) {
                                val updatedSteps = plan.steps.toMutableList()
                                updatedSteps[plan.currentStepIndex] = updatedSteps[plan.currentStepIndex].copy(status = StepStatus.COMPLETED)
                                _currentPlan.value = plan.copy(
                                    steps = updatedSteps,
                                    currentStepIndex = plan.currentStepIndex + 1
                                )
                            }
                        }
                    }
                }

                if (iterationCount % 5 == 0) {
                    val verifyPrompt = promptBuilder.buildVerificationPrompt(
                        goal = goal.description,
                        screenState = newScreen,
                        actionsTaken = _actionHistory.value
                    )
                    val verifyResponse = llmRepository.generate(verifyPrompt, maxTokens = 64)
                    val (completed, verifyReason) = toolCallParser.parseVerificationResult(verifyResponse.text)
                    if (completed) {
                        logger.logAgent("Goal completed: $verifyReason")
                        _state.value = AgentState.COMPLETED
                        _currentGoal.value = goal.copy(status = GoalStatus.COMPLETED)
                        saveToLongTermMemory(goal)
                        return
                    }
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.logError("Loop error: ${e.message}")
                _state.value = AgentState.ERROR
                delay(1000)
                _state.value = AgentState.OBSERVING
            }
        }

        if (iterationCount >= maxIterations) {
            logger.logAgent("Max iterations reached")
            _state.value = AgentState.ERROR
        }
    }

    private suspend fun saveToLongTermMemory(goal: AgentGoal) {
        memoryRepository.save(
            MemoryEntry(
                type = MemoryType.LONG_TERM,
                key = "completed_task_${goal.id}",
                value = "${goal.description}: ${_actionHistory.value.joinToString(" -> ")}"
            )
        )
    }
}
