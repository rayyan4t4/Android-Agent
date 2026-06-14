package com.androidagent.domain.repository

import com.androidagent.domain.model.ActionResult
import com.androidagent.domain.model.AgentAction

interface ActionRepository {
    suspend fun execute(action: AgentAction): ActionResult
    suspend fun isAccessibilityReady(): Boolean
}
