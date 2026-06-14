package com.androidagent.data.llm

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlamaCppEngine @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        init {
            System.loadLibrary("llama-android")
        }
    }

    private var modelPtr: Long = 0L
    private var contextPtr: Long = 0L

    val isLoaded: Boolean get() = modelPtr != 0L && contextPtr != 0L

    fun loadModel(
        path: String,
        contextSize: Int = 2048,
        threads: Int = 4,
        gpuLayers: Int = 0
    ): Boolean {
        if (isLoaded) unload()
        modelPtr = nativeLoadModel(path, gpuLayers)
        if (modelPtr == 0L) return false
        contextPtr = nativeCreateContext(modelPtr, contextSize, threads)
        if (contextPtr == 0L) {
            nativeFreeModel(modelPtr)
            modelPtr = 0L
            return false
        }
        return true
    }

    fun generate(
        prompt: String,
        maxTokens: Int = 512,
        temperature: Float = 0.7f,
        topP: Float = 0.9f,
        topK: Int = 40,
        repeatPenalty: Float = 1.1f,
        stopSequences: List<String> = listOf("</s>", "<|im_end|>", "<|end|>"),
        onToken: ((String) -> Unit)? = null
    ): String {
        if (!isLoaded) throw IllegalStateException("Model not loaded")
        return nativeGenerate(
            contextPtr,
            prompt,
            maxTokens,
            temperature,
            topP,
            topK,
            repeatPenalty,
            stopSequences.toTypedArray(),
            onToken
        )
    }

    fun getTokenCount(text: String): Int {
        if (!isLoaded) return 0
        return nativeTokenCount(contextPtr, text)
    }

    fun unload() {
        if (contextPtr != 0L) {
            nativeFreeContext(contextPtr)
            contextPtr = 0L
        }
        if (modelPtr != 0L) {
            nativeFreeModel(modelPtr)
            modelPtr = 0L
        }
    }

    fun clearContext() {
        if (contextPtr != 0L) {
            nativeClearContext(contextPtr)
        }
    }

    private external fun nativeLoadModel(path: String, gpuLayers: Int): Long
    private external fun nativeCreateContext(modelPtr: Long, contextSize: Int, threads: Int): Long
    private external fun nativeFreeModel(modelPtr: Long)
    private external fun nativeFreeContext(contextPtr: Long)
    private external fun nativeClearContext(contextPtr: Long)
    private external fun nativeGenerate(
        contextPtr: Long,
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        topP: Float,
        topK: Int,
        repeatPenalty: Float,
        stopSequences: Array<String>,
        callback: ((String) -> Unit)?
    ): String
    private external fun nativeTokenCount(contextPtr: Long, text: String): Int
}
