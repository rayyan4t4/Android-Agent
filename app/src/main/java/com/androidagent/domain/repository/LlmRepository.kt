package com.androidagent.domain.repository

import com.androidagent.domain.model.LlmConfig
import com.androidagent.domain.model.LlmResponse
import kotlinx.coroutines.flow.Flow

interface LlmRepository {
    suspend fun loadModel(config: LlmConfig): Boolean
    suspend fun unloadModel()
    suspend fun generate(prompt: String, maxTokens: Int = 512): LlmResponse
    fun generateStream(prompt: String, maxTokens: Int = 512): Flow<String>
    suspend fun isModelLoaded(): Boolean
    suspend fun getLoadedModelName(): String
    suspend fun discoverModels(): List<String>
}
