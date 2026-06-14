package com.androidagent.data.memory

import com.androidagent.domain.model.MemoryEntry
import com.androidagent.domain.model.MemoryType
import com.androidagent.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepositoryImpl @Inject constructor(
    private val dao: MemoryDao
) : MemoryRepository {

    override suspend fun save(entry: MemoryEntry) {
        dao.insert(entry.toEntity())
    }

    override suspend fun get(key: String, type: MemoryType): MemoryEntry? {
        val entity = dao.getByKey(key, type.name) ?: return null
        dao.incrementAccess(entity.id)
        return entity.toDomain()
    }

    override suspend fun getAll(type: MemoryType): List<MemoryEntry> {
        return dao.getAllByType(type.name).map { it.toDomain() }
    }

    override fun observeAll(type: MemoryType): Flow<List<MemoryEntry>> {
        return dao.observeByType(type.name).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun deleteExpired() {
        dao.deleteExpired(System.currentTimeMillis())
    }

    override suspend fun clear(type: MemoryType) {
        dao.clearByType(type.name)
    }

    override suspend fun count(type: MemoryType): Int {
        return dao.countByType(type.name)
    }

    private fun MemoryEntry.toEntity() = MemoryEntity(
        id = id,
        type = type.name,
        key = key,
        value = value,
        timestamp = timestamp,
        accessCount = accessCount,
        expiresAt = expiresAt
    )

    private fun MemoryEntity.toDomain() = MemoryEntry(
        id = id,
        type = MemoryType.valueOf(type),
        key = key,
        value = value,
        timestamp = timestamp,
        accessCount = accessCount,
        expiresAt = expiresAt
    )
}
