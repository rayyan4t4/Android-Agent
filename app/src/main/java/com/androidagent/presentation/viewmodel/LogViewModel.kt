package com.androidagent.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidagent.domain.model.AgentLog
import com.androidagent.domain.repository.AgentLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(
    private val logRepository: AgentLogRepository
) : ViewModel() {

    val logs: StateFlow<List<AgentLog>> = logRepository.observeLogs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun clearLogs() {
        viewModelScope.launch { logRepository.clearLogs() }
    }
}
