package com.androidagent.domain.usecase

import android.graphics.Bitmap
import com.androidagent.domain.model.OcrResult
import com.androidagent.domain.repository.OcrRepository
import javax.inject.Inject

class ExtractTextUseCase @Inject constructor(
    private val ocrRepository: OcrRepository
) {
    suspend operator fun invoke(bitmap: Bitmap): List<OcrResult> = ocrRepository.extractText(bitmap)
    suspend fun isAvailable(): Boolean = ocrRepository.isAvailable()
}
