package com.androidagent.data.logging

import androidx.room.*
import com.androidagent.domain.model.AgentLog
import com.androidagent.domain.model.LogCategory
import com.androidagent.domain.model.LogLevel
import com.androidagent.domain.repository.AgentLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Entity(tableName = "agent_logs")
data class LogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "level") val level: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "message") val message: String,
    @ColumnInfo(name = "details") val details: String = ""
)

@Dao
interface LogDao {
    @Insert
    suspend fun insert(entry: LogEntity)

    @Query("SELECT * FROM agent_logs ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<LogEntity>>

    @Query("SELECT * FROM agent_logs WHERE category = :category ORDER BY timestamp DESC")
    suspend fun getByCategory(category: String): List<LogEntity>

    @Query("SELECT * FROM agent_logs ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<LogEntity>

    @Query("DELETE FROM agent_logs")
    suspend fun clearAll()
}

@Database(entities = [LogEntity::class], version = 1, exportSchema = false)
abstract class LogDatabase : RoomDatabase() {
    abstract fun logDao(): LogDao
}

@Singleton
class AgentLogRepositoryImpl @Inject constructor(
    private val dao: LogDao
) : AgentLogRepository {

    override suspend fun log(entry: AgentLog) {
        dao.insert(entry.toEntity())
    }

    override fun observeLogs(): Flow<List<AgentLog>> {
        return dao.observeAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getLogsByCategory(category: LogCategory): List<AgentLog> {
        return dao.getByCategory(category.name).map { it.toDomain() }
    }

    override suspend fun clearLogs() {
        dao.clearAll()
    }

    override suspend fun getRecentLogs(limit: Int): List<AgentLog> {
        return dao.getRecent(limit).map { it.toDomain() }
    }

    private fun AgentLog.toEntity() = LogEntity(
        timestamp = timestamp,
        level = level.name,
        category = category.name,
        message = message,
        details = details
    )

    private fun LogEntity.toDomain() = AgentLog(
        id = id,
        timestamp = timestamp,
        level = LogLevel.valueOf(level),
        category = LogCategory.valueOf(category),
        message = message,
        details = details
    )
}

@Singleton
class AgentLogger @Inject constructor(
    private val repository: AgentLogRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun logAgent(message: String, details: String = "") = log(LogLevel.INFO, LogCategory.AGENT, message, details)
    fun logPlanning(message: String, details: String = "") = log(LogLevel.INFO, LogCategory.PLANNING, message, details)
    fun logTool(message: String, details: String = "") = log(LogLevel.INFO, LogCategory.TOOL, message, details)
    fun logLlm(message: String, details: String = "") = log(LogLevel.DEBUG, LogCategory.LLM, message, details)
    fun logPerception(message: String, details: String = "") = log(LogLevel.DEBUG, LogCategory.PERCEPTION, message, details)
    fun logSafety(message: String, details: String = "") = log(LogLevel.WARNING, LogCategory.SAFETY, message, details)
    fun logError(message: String, details: String = "") = log(LogLevel.ERROR, LogCategory.SYSTEM, message, details)

    private fun log(level: LogLevel, category: LogCategory, message: String, details: String) {
        scope.launch {
            repository.log(
                AgentLog(
                    level = level,
                    category = category,
                    message = message,
                    details = details
                )
            )
        }
        android.util.Log.d("AgentLog", "[$category] $message")
    }
}
