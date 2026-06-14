package com.androidagent.data.llm

import android.content.Context
import android.os.Environment
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val modelsDir: File
        get() {
            val dir = File(context.filesDir, "models")
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun discoverModels(): List<String> {
        val paths = mutableListOf<String>()
        modelsDir.listFiles()?.filter { it.extension == "gguf" }?.forEach {
            paths.add(it.absolutePath)
        }
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        downloads?.listFiles()?.filter { it.extension == "gguf" }?.forEach {
            paths.add(it.absolutePath)
        }
        return paths
    }

    fun importModel(sourcePath: String): String? {
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists() || sourceFile.extension != "gguf") return null
        val destFile = File(modelsDir, sourceFile.name)
        if (destFile.exists()) return destFile.absolutePath
        sourceFile.copyTo(destFile)
        return destFile.absolutePath
    }

    fun deleteModel(path: String): Boolean {
        val file = File(path)
        return if (file.exists() && file.parentFile == modelsDir) {
            file.delete()
        } else false
    }

    fun getModelName(path: String): String {
        return File(path).nameWithoutExtension
    }

    fun getModelSize(path: String): Long {
        return File(path).length()
    }

    fun isValidModel(path: String): Boolean {
        val file = File(path)
        if (!file.exists() || file.length() < 1024) return false
        file.inputStream().use { stream ->
            val magic = ByteArray(4)
            stream.read(magic)
            return magic[0] == 'G'.code.toByte() &&
                    magic[1] == 'G'.code.toByte() &&
                    magic[2] == 'U'.code.toByte() &&
                    magic[3] == 'F'.code.toByte()
        }
    }
}
