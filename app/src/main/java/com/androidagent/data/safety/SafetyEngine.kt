package com.androidagent.data.safety

import com.androidagent.domain.model.AgentAction
import com.androidagent.domain.model.SafetyCheck
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SafetyEngine @Inject constructor() {

    private val _emergencyStop = MutableStateFlow(false)
    val emergencyStop: StateFlow<Boolean> = _emergencyStop.asStateFlow()

    private val actionTimestamps = mutableListOf<Long>()
    private val maxActionsPerMinute = 30
    private val isThrottled = AtomicBoolean(false)

    fun triggerEmergencyStop() {
        _emergencyStop.value = true
    }

    fun resetEmergencyStop() {
        _emergencyStop.value = false
    }

    fun isEmergencyStopped(): Boolean = _emergencyStop.value

    fun checkThrottle(): Boolean {
        val now = System.currentTimeMillis()
        synchronized(actionTimestamps) {
            actionTimestamps.removeAll { now - it > 60_000 }
            if (actionTimestamps.size >= maxActionsPerMinute) {
                isThrottled.set(true)
                return false
            }
            actionTimestamps.add(now)
            isThrottled.set(false)
        }
        return true
    }

    fun isCurrentlyThrottled(): Boolean = isThrottled.get()
}

@Singleton
class ApprovalManager @Inject constructor() {

    private val _pendingApproval = MutableStateFlow<PendingApproval?>(null)
    val pendingApproval: StateFlow<PendingApproval?> = _pendingApproval.asStateFlow()

    fun requestApproval(check: SafetyCheck): PendingApproval {
        val pending = PendingApproval(check)
        _pendingApproval.value = pending
        return pending
    }

    fun approve() {
        _pendingApproval.value?.let {
            it.approved = true
            it.resolved = true
        }
        _pendingApproval.value = null
    }

    fun deny() {
        _pendingApproval.value?.let {
            it.approved = false
            it.resolved = true
        }
        _pendingApproval.value = null
    }

    data class PendingApproval(
        val check: SafetyCheck,
        var approved: Boolean = false,
        var resolved: Boolean = false
    )
}
