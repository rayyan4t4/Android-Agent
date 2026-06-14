package com.androidagent.domain.repository

import android.graphics.Bitmap
import com.androidagent.domain.model.OcrResult

interface OcrRepository {
    suspend fun extractText(bitmap: Bitmap): List<OcrResult>
    suspend fun isAvailable(): Boolean
}
