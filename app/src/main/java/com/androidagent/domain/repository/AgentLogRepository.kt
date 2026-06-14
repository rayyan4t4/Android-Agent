package com.androidagent.domain.repository

import com.androidagent.domain.model.AgentLog
import com.androidagent.domain.model.LogCategory
import kotlinx.coroutines.flow.Flow

interface AgentLogRepository {
    suspend fun log(entry: AgentLog)
    fun observeLogs(): Flow<List<AgentLog>>
    suspend fun getLogsByCategory(category: LogCategory): List<AgentLog>
    suspend fun clearLogs()
    suspend fun getRecentLogs(limit: Int = 50): List<AgentLog>
}
