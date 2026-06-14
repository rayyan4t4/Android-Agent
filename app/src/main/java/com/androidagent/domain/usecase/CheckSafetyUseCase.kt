package com.androidagent.domain.usecase

import com.androidagent.domain.model.SafetyCheck
import com.androidagent.domain.model.AgentAction
import com.androidagent.domain.model.RiskLevel
import javax.inject.Inject

class CheckSafetyUseCase @Inject constructor() {
    operator fun invoke(action: AgentAction, screenContext: String): SafetyCheck {
        val riskLevel = assessRisk(action, screenContext)
        return SafetyCheck(
            action = action,
            riskLevel = riskLevel,
            reason = getReason(riskLevel, action),
            requiresApproval = riskLevel >= RiskLevel.HIGH
        )
    }

    private fun assessRisk(action: AgentAction, context: String): RiskLevel {
        val lowerContext = context.lowercase()
        val financialKeywords = listOf("pay", "purchase", "buy", "price", "cart", "checkout", "order", "subscribe", "billing", "credit", "debit", "wallet", "bank", "transfer", "send money")
        val messageKeywords = listOf("send", "message", "email", "sms", "chat", "compose", "reply", "post", "tweet", "publish")
        val deleteKeywords = listOf("delete", "remove", "erase", "clear all", "uninstall", "format", "reset")
        val accountKeywords = listOf("password", "account", "sign out", "logout", "deactivate", "settings", "security", "permission")

        val hasFinancial = financialKeywords.any { it in lowerContext }
        val hasMessage = messageKeywords.any { it in lowerContext }
        val hasDelete = deleteKeywords.any { it in lowerContext }
        val hasAccount = accountKeywords.any { it in lowerContext }

        return when (action) {
            is AgentAction.Tap, is AgentAction.LongPress -> {
                when {
                    hasFinancial -> RiskLevel.CRITICAL
                    hasMessage -> RiskLevel.HIGH
                    hasDelete -> RiskLevel.CRITICAL
                    hasAccount -> RiskLevel.HIGH
                    else -> RiskLevel.LOW
                }
            }
            is AgentAction.TypeText -> {
                when {
                    hasFinancial -> RiskLevel.HIGH
                    hasMessage -> RiskLevel.MEDIUM
                    else -> RiskLevel.LOW
                }
            }
            is AgentAction.LaunchApp -> RiskLevel.LOW
            is AgentAction.Back, is AgentAction.Home, is AgentAction.Recents -> RiskLevel.SAFE
            is AgentAction.Screenshot, is AgentAction.ReadScreen -> RiskLevel.SAFE
            is AgentAction.Wait -> RiskLevel.SAFE
            is AgentAction.FindElement -> RiskLevel.SAFE
            is AgentAction.Notifications -> RiskLevel.SAFE
            is AgentAction.Swipe, is AgentAction.Scroll -> RiskLevel.SAFE
        }
    }

    private fun getReason(riskLevel: RiskLevel, action: AgentAction): String {
        return when (riskLevel) {
            RiskLevel.CRITICAL -> "Action may involve financial transaction or data deletion"
            RiskLevel.HIGH -> "Action may send messages or modify account settings"
            RiskLevel.MEDIUM -> "Action involves text input in a sensitive context"
            RiskLevel.LOW -> "Standard interaction"
            RiskLevel.SAFE -> "Read-only or navigation action"
        }
    }
}
