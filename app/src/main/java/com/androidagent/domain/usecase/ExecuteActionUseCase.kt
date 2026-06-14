package com.androidagent.domain.usecase

import com.androidagent.domain.model.ActionResult
import com.androidagent.domain.model.AgentAction
import com.androidagent.domain.repository.ActionRepository
import javax.inject.Inject

class ExecuteActionUseCase @Inject constructor(
    private val actionRepository: ActionRepository
) {
    suspend operator fun invoke(action: AgentAction): ActionResult = actionRepository.execute(action)
    suspend fun isReady(): Boolean = actionRepository.isAccessibilityReady()
}
