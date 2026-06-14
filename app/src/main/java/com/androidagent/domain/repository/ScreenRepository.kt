package com.androidagent.domain.repository

import com.androidagent.domain.model.ScreenState
import kotlinx.coroutines.flow.Flow

interface ScreenRepository {
    suspend fun getScreenState(): ScreenState
    fun observeScreenChanges(): Flow<ScreenState>
    suspend fun isAccessibilityEnabled(): Boolean
}
