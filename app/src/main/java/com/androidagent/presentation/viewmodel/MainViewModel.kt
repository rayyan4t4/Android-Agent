package com.androidagent.presentation.viewmodel

import android.app.Application
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidagent.data.agent.AgentLoop
import com.androidagent.data.capture.ScreenCaptureManager
import com.androidagent.data.safety.SafetyEngine
import com.androidagent.domain.model.AgentState
import com.androidagent.domain.model.LlmConfig
import com.androidagent.domain.usecase.RunInferenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: Application,
    private val agentLoop: AgentLoop,
    private val runInference: RunInferenceUseCase,
    private val captureManager: ScreenCaptureManager,
    private val safetyEngine: SafetyEngine
) : ViewModel() {

    val agentState = agentLoop.state
    val currentGoal = agentLoop.currentGoal
    val currentPlan = agentLoop.currentPlan
    val actionHistory = agentLoop.actionHistory
    val pendingApproval = agentLoop.pendingApproval

    private val _taskInput = MutableStateFlow("")
    val taskInput: StateFlow<String> = _taskInput.asStateFlow()

    private val _isModelLoaded = MutableStateFlow(false)
    val isModelLoaded: StateFlow<Boolean> = _isModelLoaded.asStateFlow()

    private val _modelName = MutableStateFlow("")
    val modelName: StateFlow<String> = _modelName.asStateFlow()

    private val _isAccessibilityEnabled = MutableStateFlow(false)
    val isAccessibilityEnabled: StateFlow<Boolean> = _isAccessibilityEnabled.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    init {
        checkState()
    }

    fun updateTaskInput(input: String) {
        _taskInput.value = input
    }

    fun startAgent() {
        val task = _taskInput.value.trim()
        if (task.isBlank() || !_isModelLoaded.value) return
        agentLoop.start(task, viewModelScope)
    }

    fun stopAgent() {
        agentLoop.stop()
    }

    fun emergencyStop() {
        safetyEngine.triggerEmergencyStop()
        agentLoop.stop()
        _statusMessage.value = "Emergency stop activated"
    }

    fun approveAction() {
        agentLoop.approveAction(true)
    }

    fun denyAction() {
        agentLoop.approveAction(false)
    }

    fun loadModel(path: String) {
        viewModelScope.launch {
            _statusMessage.value = "Loading model..."
            val config = LlmConfig(
                modelPath = path,
                modelName = path.substringAfterLast('/').substringBeforeLast('.'),
                contextSize = 2048,
                threads = 4
            )
            val success = runInference.loadModel(config)
            _isModelLoaded.value = success
            _modelName.value = if (success) config.modelName else ""
            _statusMessage.value = if (success) "Model loaded: ${config.modelName}" else "Failed to load model"
        }
    }

    fun requestScreenCapture() {
        viewModelScope.launch {
            val granted = captureManager.requestPermission()
            if (granted) {
                captureManager.startCapture()
                _statusMessage.value = "Screen capture active"
            }
        }
    }

    fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        application.startActivity(intent)
    }

    fun checkState() {
        viewModelScope.launch {
            _isModelLoaded.value = runInference.isReady()
            _modelName.value = runInference.getModelName()
            _isAccessibilityEnabled.value = com.androidagent.data.accessibility.AgentAccessibilityService.isRunning()
        }
    }
}
