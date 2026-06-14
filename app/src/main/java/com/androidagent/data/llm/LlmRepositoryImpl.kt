package com.androidagent.data.llm

import com.androidagent.domain.model.LlmConfig
import com.androidagent.domain.model.LlmResponse
import com.androidagent.domain.model.StopReason
import com.androidagent.domain.repository.LlmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmRepositoryImpl @Inject constructor(
    private val engine: LlamaCppEngine,
    private val modelManager: ModelManager
) : LlmRepository {

    private var currentConfig: LlmConfig? = null

    override suspend fun loadModel(config: LlmConfig): Boolean = withContext(Dispatchers.IO) {
        val success = engine.loadModel(
            path = config.modelPath,
            contextSize = config.contextSize,
            threads = config.threads,
            gpuLayers = config.gpuLayers
        )
        if (success) currentConfig = config
        success
    }

    override suspend fun unloadModel() = withContext(Dispatchers.IO) {
        engine.unload()
        currentConfig = null
    }

    override suspend fun generate(prompt: String, maxTokens: Int): LlmResponse = withContext(Dispatchers.IO) {
        val config = currentConfig ?: throw IllegalStateException("No model loaded")
        val startTime = System.currentTimeMillis()
        var tokenCount = 0

        val result = engine.generate(
            prompt = prompt,
            maxTokens = maxTokens,
            temperature = config.temperature,
            topP = config.topP,
            topK = config.topK,
            repeatPenalty = config.repeatPenalty,
            onToken = { tokenCount++ }
        )

        val elapsed = System.currentTimeMillis() - startTime
        val promptTokens = engine.getTokenCount(prompt)

        LlmResponse(
            text = result,
            tokensGenerated = tokenCount,
            tokensPrompt = promptTokens,
            generationTimeMs = elapsed,
            tokensPerSecond = if (elapsed > 0) tokenCount * 1000f / elapsed else 0f,
            stopReason = if (tokenCount >= maxTokens) StopReason.MAX_TOKENS else StopReason.END_OF_TEXT
        )
    }

    override fun generateStream(prompt: String, maxTokens: Int): Flow<String> = callbackFlow {
        val config = currentConfig ?: throw IllegalStateException("No model loaded")
        withContext(Dispatchers.IO) {
            engine.generate(
                prompt = prompt,
                maxTokens = maxTokens,
                temperature = config.temperature,
                topP = config.topP,
                topK = config.topK,
                repeatPenalty = config.repeatPenalty,
                onToken = { token -> trySend(token) }
            )
        }
        close()
        awaitClose()
    }

    override suspend fun isModelLoaded(): Boolean = engine.isLoaded

    override suspend fun getLoadedModelName(): String = currentConfig?.modelName ?: ""

    override suspend fun discoverModels(): List<String> = withContext(Dispatchers.IO) {
        modelManager.discoverModels()
    }
}
