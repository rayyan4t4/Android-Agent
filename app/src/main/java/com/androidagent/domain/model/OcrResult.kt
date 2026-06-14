package com.androidagent.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class OcrResult(
    val text: String,
    val boundingBox: Bounds,
    val confidence: Float,
    val language: String = ""
)
