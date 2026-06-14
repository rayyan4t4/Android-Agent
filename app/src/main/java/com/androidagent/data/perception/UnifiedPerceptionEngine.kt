package com.androidagent.data.perception

import com.androidagent.data.accessibility.AgentAccessibilityService
import com.androidagent.data.capture.ScreenCaptureManager
import com.androidagent.data.ocr.LocalOcrEngine
import com.androidagent.domain.model.ScreenState
import com.androidagent.domain.repository.ScreenRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnifiedPerceptionEngine @Inject constructor(
    private val ocrEngine: LocalOcrEngine,
    private val captureManager: ScreenCaptureManager
) : ScreenRepository {

    override suspend fun getScreenState(): ScreenState {
        val service = AgentAccessibilityService.instance.value
        val nodes = service?.currentNodes?.value ?: emptyList()
        val activePackage = service?.activePackage?.value ?: ""
        val activeActivity = service?.activeActivity?.value ?: ""
        val (width, height) = service?.getScreenDimensions() ?: Pair(1080, 2340)

        val ocrResults = if (captureManager.isCapturing) {
            val bitmap = captureManager.captureScreenshot()
            bitmap?.let {
                try {
                    ocrEngine.extractText(it)
                } catch (_: Exception) {
                    emptyList()
                } finally {
                    it.recycle()
                }
            } ?: emptyList()
        } else {
            emptyList()
        }

        return ScreenState(
            timestamp = System.currentTimeMillis(),
            activePackage = activePackage,
            activeActivity = activeActivity,
            nodes = nodes,
            ocrResults = ocrResults,
            screenWidth = width,
            screenHeight = height
        )
    }

    override fun observeScreenChanges(): Flow<ScreenState> = flow {
        while (true) {
            emit(getScreenState())
            delay(1000)
        }
    }

    override suspend fun isAccessibilityEnabled(): Boolean {
        return AgentAccessibilityService.isRunning()
    }
}
