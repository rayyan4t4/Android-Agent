package com.androidagent.data.safety

import com.androidagent.domain.model.*
import com.androidagent.domain.usecase.CheckSafetyUseCase
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SafetyEngineTest {

    private lateinit var checkSafety: CheckSafetyUseCase
    private lateinit var safetyEngine: SafetyEngine

    @Before
    fun setup() {
        checkSafety = CheckSafetyUseCase()
        safetyEngine = SafetyEngine()
    }

    @Test
    fun `navigation actions are safe`() {
        val check = checkSafety(AgentAction.Back(), "some screen")
        assertEquals(RiskLevel.SAFE, check.riskLevel)
        assertFalse(check.requiresApproval)
    }

    @Test
    fun `home action is safe`() {
        val check = checkSafety(AgentAction.Home(), "any context")
        assertEquals(RiskLevel.SAFE, check.riskLevel)
    }

    @Test
    fun `tap in financial context is critical`() {
        val check = checkSafety(AgentAction.Tap(100, 200), "Please confirm your purchase of $9.99")
        assertEquals(RiskLevel.CRITICAL, check.riskLevel)
        assertTrue(check.requiresApproval)
    }

    @Test
    fun `tap in message context is high risk`() {
        val check = checkSafety(AgentAction.Tap(100, 200), "Send message to John")
        assertEquals(RiskLevel.HIGH, check.riskLevel)
        assertTrue(check.requiresApproval)
    }

    @Test
    fun `tap in delete context is critical`() {
        val check = checkSafety(AgentAction.Tap(100, 200), "Delete all files")
        assertEquals(RiskLevel.CRITICAL, check.riskLevel)
        assertTrue(check.requiresApproval)
    }

    @Test
    fun `type text in normal context is low risk`() {
        val check = checkSafety(AgentAction.TypeText("hello"), "search bar")
        assertEquals(RiskLevel.LOW, check.riskLevel)
        assertFalse(check.requiresApproval)
    }

    @Test
    fun `read screen is safe`() {
        val check = checkSafety(AgentAction.ReadScreen(), "anything")
        assertEquals(RiskLevel.SAFE, check.riskLevel)
    }

    @Test
    fun `throttle allows actions within limit`() {
        repeat(29) {
            assertTrue(safetyEngine.checkThrottle())
        }
        assertFalse(safetyEngine.isCurrentlyThrottled())
    }

    @Test
    fun `emergency stop works`() {
        assertFalse(safetyEngine.isEmergencyStopped())
        safetyEngine.triggerEmergencyStop()
        assertTrue(safetyEngine.isEmergencyStopped())
        safetyEngine.resetEmergencyStop()
        assertFalse(safetyEngine.isEmergencyStopped())
    }
}
