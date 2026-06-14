package com.androidagent.di

import android.content.Context
import androidx.room.Room
import com.androidagent.data.action.ActionEngine
import com.androidagent.data.capture.ScreenCaptureManager
import com.androidagent.data.llm.LlamaCppEngine
import com.androidagent.data.llm.LlmRepositoryImpl
import com.androidagent.data.llm.ModelManager
import com.androidagent.data.logging.AgentLogRepositoryImpl
import com.androidagent.data.logging.LogDao
import com.androidagent.data.logging.LogDatabase
import com.androidagent.data.memory.MemoryDao
import com.androidagent.data.memory.MemoryDatabase
import com.androidagent.data.memory.MemoryRepositoryImpl
import com.androidagent.data.ocr.LocalOcrEngine
import com.androidagent.data.perception.UnifiedPerceptionEngine
import com.androidagent.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMemoryDatabase(@ApplicationContext context: Context): MemoryDatabase {
        return Room.databaseBuilder(context, MemoryDatabase::class.java, "agent_memory.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideMemoryDao(db: MemoryDatabase): MemoryDao = db.memoryDao()

    @Provides
    @Singleton
    fun provideLogDatabase(@ApplicationContext context: Context): LogDatabase {
        return Room.databaseBuilder(context, LogDatabase::class.java, "agent_logs.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideLogDao(db: LogDatabase): LogDao = db.logDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindScreenRepository(impl: UnifiedPerceptionEngine): ScreenRepository

    @Binds
    @Singleton
    abstract fun bindOcrRepository(impl: LocalOcrEngine): OcrRepository

    @Binds
    @Singleton
    abstract fun bindLlmRepository(impl: LlmRepositoryImpl): LlmRepository

    @Binds
    @Singleton
    abstract fun bindActionRepository(impl: ActionEngine): ActionRepository

    @Binds
    @Singleton
    abstract fun bindMemoryRepository(impl: MemoryRepositoryImpl): MemoryRepository

    @Binds
    @Singleton
    abstract fun bindAgentLogRepository(impl: AgentLogRepositoryImpl): AgentLogRepository
}
