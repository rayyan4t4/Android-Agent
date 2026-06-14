package com.androidagent.data.memory

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "memory_entries")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "value") val value: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "access_count") val accessCount: Int = 0,
    @ColumnInfo(name = "expires_at") val expiresAt: Long = 0
)

@Dao
interface MemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MemoryEntity)

    @Query("SELECT * FROM memory_entries WHERE `key` = :key AND type = :type LIMIT 1")
    suspend fun getByKey(key: String, type: String): MemoryEntity?

    @Query("SELECT * FROM memory_entries WHERE type = :type ORDER BY timestamp DESC")
    suspend fun getAllByType(type: String): List<MemoryEntity>

    @Query("SELECT * FROM memory_entries WHERE type = :type ORDER BY timestamp DESC")
    fun observeByType(type: String): Flow<List<MemoryEntity>>

    @Query("DELETE FROM memory_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM memory_entries WHERE expires_at > 0 AND expires_at < :now")
    suspend fun deleteExpired(now: Long)

    @Query("DELETE FROM memory_entries WHERE type = :type")
    suspend fun clearByType(type: String)

    @Query("SELECT COUNT(*) FROM memory_entries WHERE type = :type")
    suspend fun countByType(type: String): Int

    @Query("UPDATE memory_entries SET access_count = access_count + 1 WHERE id = :id")
    suspend fun incrementAccess(id: Long)
}

@Database(entities = [MemoryEntity::class], version = 1, exportSchema = false)
abstract class MemoryDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
}
