package com.androidagent.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class LlmConfig(
    val modelPath: String,
    val modelName: String = "",
    val contextSize: Int = 2048,
    val threads: Int = 4,
    val gpuLayers: Int = 0,
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val topK: Int = 40,
    val repeatPenalty: Float = 1.1f,
    val maxTokens: Int = 512,
    val seed: Int = -1
)
