package com.androidagent.data.capture

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class ScreenCaptureManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private var pendingResult: CompletableDeferred<Boolean>? = null
        private var resultCode: Int = 0
        private var resultData: Intent? = null

        fun onPermissionGranted(code: Int, data: Intent) {
            resultCode = code
            resultData = data
            pendingResult?.complete(true)
        }

        fun onPermissionDenied() {
            pendingResult?.complete(false)
        }
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var handlerThread: HandlerThread? = null
    private var handler: Handler? = null
    private var latestBitmap: Bitmap? = null

    val isCapturing: Boolean get() = mediaProjection != null

    suspend fun requestPermission(): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        pendingResult = deferred
        val intent = Intent(context, CaptureActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return deferred.await()
    }

    fun startCapture() {
        if (resultData == null) return

        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        handlerThread = HandlerThread("ScreenCapture").apply { start() }
        handler = Handler(handlerThread!!.looper)

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        imageReader!!.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            latestBitmap?.recycle()
            latestBitmap = imageToBitmap(image, width, height)
            image.close()
        }, handler)

        val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, resultData!!)

        virtualDisplay = mediaProjection!!.createVirtualDisplay(
            "AgentCapture",
            width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface,
            null, handler
        )

        val serviceIntent = Intent(context, CaptureForegroundService::class.java)
        context.startForegroundService(serviceIntent)
    }

    suspend fun captureScreenshot(): Bitmap? {
        if (!isCapturing) return null
        return suspendCancellableCoroutine { continuation ->
            handler?.postDelayed({
                continuation.resume(latestBitmap?.copy(Bitmap.Config.ARGB_8888, false))
            }, 100)
        }
    }

    fun stopCapture() {
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
        mediaProjection?.stop()
        mediaProjection = null
        handlerThread?.quitSafely()
        handlerThread = null
        handler = null
        latestBitmap?.recycle()
        latestBitmap = null

        val serviceIntent = Intent(context, CaptureForegroundService::class.java)
        context.stopService(serviceIntent)
    }

    private fun imageToBitmap(image: Image, width: Int, height: Int): Bitmap {
        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * width

        val bitmap = Bitmap.createBitmap(
            width + rowPadding / pixelStride,
            height,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)

        return if (bitmap.width != width) {
            val cropped = Bitmap.createBitmap(bitmap, 0, 0, width, height)
            bitmap.recycle()
            cropped
        } else {
            bitmap
        }
    }
}
