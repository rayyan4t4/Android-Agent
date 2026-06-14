package com.androidagent.domain.usecase

import com.androidagent.domain.model.LlmConfig
import com.androidagent.domain.model.LlmResponse
import com.androidagent.domain.repository.LlmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RunInferenceUseCase @Inject constructor(
    private val llmRepository: LlmRepository
) {
    suspend fun loadModel(config: LlmConfig): Boolean = llmRepository.loadModel(config)
    suspend fun unloadModel() = llmRepository.unloadModel()
    suspend fun generate(prompt: String, maxTokens: Int = 512): LlmResponse = llmRepository.generate(prompt, maxTokens)
    fun generateStream(prompt: String, maxTokens: Int = 512): Flow<String> = llmRepository.generateStream(prompt, maxTokens)
    suspend fun isReady(): Boolean = llmRepository.isModelLoaded()
    suspend fun getModelName(): String = llmRepository.getLoadedModelName()
    suspend fun discoverModels(): List<String> = llmRepository.discoverModels()
}
