package com.androidagent.data.ocr

import android.graphics.Bitmap
import com.androidagent.domain.model.Bounds
import com.androidagent.domain.model.OcrResult
import com.androidagent.domain.repository.OcrRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class LocalOcrEngine @Inject constructor() : OcrRepository {

    private val recognizer: TextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun extractText(bitmap: Bitmap): List<OcrResult> {
        val image = InputImage.fromBitmap(bitmap, 0)
        return suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    val ocrResults = mutableListOf<OcrResult>()
                    for (block in result.textBlocks) {
                        for (line in block.lines) {
                            val box = line.boundingBox ?: continue
                            ocrResults.add(
                                OcrResult(
                                    text = line.text,
                                    boundingBox = Bounds(box.left, box.top, box.right, box.bottom),
                                    confidence = line.confidence ?: 0f,
                                    language = line.recognizedLanguage ?: ""
                                )
                            )
                        }
                    }
                    continuation.resume(ocrResults)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    override suspend fun isAvailable(): Boolean = true
}
