package com.androidagent.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidagent.data.llm.ModelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModelManagerViewModel @Inject constructor(
    private val modelManager: ModelManager
) : ViewModel() {

    private val _models = MutableStateFlow<List<ModelInfo>>(emptyList())
    val models: StateFlow<List<ModelInfo>> = _models.asStateFlow()

    private val _importStatus = MutableStateFlow("")
    val importStatus: StateFlow<String> = _importStatus.asStateFlow()

    init {
        refreshModels()
    }

    fun refreshModels() {
        viewModelScope.launch(Dispatchers.IO) {
            val paths = modelManager.discoverModels()
            _models.value = paths.map { path ->
                ModelInfo(
                    path = path,
                    name = modelManager.getModelName(path),
                    sizeBytes = modelManager.getModelSize(path),
                    isValid = modelManager.isValidModel(path)
                )
            }
        }
    }

    fun importModel(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _importStatus.value = "Importing..."
            val result = modelManager.importModel(path)
            _importStatus.value = if (result != null) "Imported successfully" else "Import failed"
            refreshModels()
        }
    }

    fun deleteModel(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            modelManager.deleteModel(path)
            refreshModels()
        }
    }

    data class ModelInfo(
        val path: String,
        val name: String,
        val sizeBytes: Long,
        val isValid: Boolean
    ) {
        val sizeDisplay: String get() {
            val mb = sizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1024) String.format("%.1f GB", mb / 1024.0)
            else String.format("%.0f MB", mb)
        }
    }
}
