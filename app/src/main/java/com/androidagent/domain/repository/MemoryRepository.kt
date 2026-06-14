package com.androidagent.domain.repository

import com.androidagent.domain.model.MemoryEntry
import com.androidagent.domain.model.MemoryType
import kotlinx.coroutines.flow.Flow

interface MemoryRepository {
    suspend fun save(entry: MemoryEntry)
    suspend fun get(key: String, type: MemoryType): MemoryEntry?
    suspend fun getAll(type: MemoryType): List<MemoryEntry>
    fun observeAll(type: MemoryType): Flow<List<MemoryEntry>>
    suspend fun delete(id: Long)
    suspend fun deleteExpired()
    suspend fun clear(type: MemoryType)
    suspend fun count(type: MemoryType): Int
}
