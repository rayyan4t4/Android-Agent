package com.androidagent.domain.usecase

import com.androidagent.domain.model.MemoryEntry
import com.androidagent.domain.model.MemoryType
import com.androidagent.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SaveMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(entry: MemoryEntry) = memoryRepository.save(entry)
}

class GetMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend fun get(key: String, type: MemoryType): MemoryEntry? = memoryRepository.get(key, type)
    suspend fun getAll(type: MemoryType): List<MemoryEntry> = memoryRepository.getAll(type)
    fun observeAll(type: MemoryType): Flow<List<MemoryEntry>> = memoryRepository.observeAll(type)
    suspend fun clear(type: MemoryType) = memoryRepository.clear(type)
}
