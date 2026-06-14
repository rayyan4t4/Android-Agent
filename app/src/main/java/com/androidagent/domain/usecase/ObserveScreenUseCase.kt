package com.androidagent.domain.usecase

import com.androidagent.domain.model.ScreenState
import com.androidagent.domain.repository.ScreenRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveScreenUseCase @Inject constructor(
    private val screenRepository: ScreenRepository
) {
    suspend fun getOnce(): ScreenState = screenRepository.getScreenState()
    fun observe(): Flow<ScreenState> = screenRepository.observeScreenChanges()
    suspend fun isReady(): Boolean = screenRepository.isAccessibilityEnabled()
}
