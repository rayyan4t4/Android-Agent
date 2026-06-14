package com.androidagent.data.action

import android.content.Context
import android.content.Intent
import com.androidagent.data.accessibility.AgentAccessibilityService
import com.androidagent.data.capture.ScreenCaptureManager
import com.androidagent.domain.model.*
import com.androidagent.domain.repository.ActionRepository
import com.androidagent.domain.repository.ScreenRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenRepository: ScreenRepository,
    private val captureManager: ScreenCaptureManager
) : ActionRepository {

    override suspend fun execute(action: AgentAction): ActionResult {
        val startTime = System.currentTimeMillis()
        return try {
            val success = when (action) {
                is AgentAction.Tap -> executeTap(action)
                is AgentAction.LongPress -> executeLongPress(action)
                is AgentAction.Swipe -> executeSwipe(action)
                is AgentAction.Scroll -> executeScroll(action)
                is AgentAction.TypeText -> executeTypeText(action)
                is AgentAction.Back -> executeBack()
                is AgentAction.Home -> executeHome()
                is AgentAction.Recents -> executeRecents()
                is AgentAction.Notifications -> executeNotifications()
                is AgentAction.LaunchApp -> executeLaunchApp(action)
                is AgentAction.Screenshot -> executeScreenshot()
                is AgentAction.ReadScreen -> true
                is AgentAction.Wait -> executeWait(action)
                is AgentAction.FindElement -> executeFindElement(action)
            }
            ActionResult(
                success = success,
                action = action,
                message = if (success) "Completed" else "Failed",
                durationMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            ActionResult(
                success = false,
                action = action,
                message = e.message ?: "Unknown error",
                durationMs = System.currentTimeMillis() - startTime
            )
        }
    }

    override suspend fun isAccessibilityReady(): Boolean = AgentAccessibilityService.isRunning()

    private suspend fun executeTap(action: AgentAction.Tap): Boolean {
        val service = getService() ?: return false
        return service.performTap(action.x, action.y)
    }

    private suspend fun executeLongPress(action: AgentAction.LongPress): Boolean {
        val service = getService() ?: return false
        return service.performLongPress(action.x, action.y, action.durationMs)
    }

    private suspend fun executeSwipe(action: AgentAction.Swipe): Boolean {
        val service = getService() ?: return false
        return service.performSwipe(action.startX, action.startY, action.endX, action.endY, action.durationMs)
    }

    private suspend fun executeScroll(action: AgentAction.Scroll): Boolean {
        val service = getService() ?: return false
        val (width, height) = service.getScreenDimensions()
        val centerX = if (action.x >= 0) action.x else width / 2
        val centerY = if (action.y >= 0) action.y else height / 2
        val distance = height / 3

        return when (action.direction) {
            ScrollDirection.UP -> service.performSwipe(centerX, centerY, centerX, centerY + distance, 300)
            ScrollDirection.DOWN -> service.performSwipe(centerX, centerY, centerX, centerY - distance, 300)
            ScrollDirection.LEFT -> service.performSwipe(centerX, centerY, centerX + distance, centerY, 300)
            ScrollDirection.RIGHT -> service.performSwipe(centerX, centerY, centerX - distance, centerY, 300)
        }
    }

    private fun executeTypeText(action: AgentAction.TypeText): Boolean {
        val service = getService() ?: return false
        val focusedNode = findFocusedEditText(service)
        return if (focusedNode != null) {
            service.inputText(focusedNode, action.text)
        } else {
            false
        }
    }

    private fun executeBack(): Boolean {
        val service = getService() ?: return false
        return service.performBack()
    }

    private fun executeHome(): Boolean {
        val service = getService() ?: return false
        return service.performHome()
    }

    private fun executeRecents(): Boolean {
        val service = getService() ?: return false
        return service.performRecents()
    }

    private fun executeNotifications(): Boolean {
        val service = getService() ?: return false
        return service.performNotifications()
    }

    private fun executeLaunchApp(action: AgentAction.LaunchApp): Boolean {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage(action.packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return true
    }

    private suspend fun executeScreenshot(): Boolean {
        return captureManager.captureScreenshot() != null
    }

    private suspend fun executeWait(action: AgentAction.Wait): Boolean {
        delay(action.durationMs)
        return true
    }

    private fun executeFindElement(action: AgentAction.FindElement): Boolean {
        val service = getService() ?: return false
        return service.findNodeByText(action.query) != null
    }

    private fun getService(): AgentAccessibilityService? = AgentAccessibilityService.instance.value

    private fun findFocusedEditText(service: AgentAccessibilityService): android.view.accessibility.AccessibilityNodeInfo? {
        val root = service.rootInActiveWindow ?: return null
        return findFocusedNode(root)
    }

    private fun findFocusedNode(node: android.view.accessibility.AccessibilityNodeInfo): android.view.accessibility.AccessibilityNodeInfo? {
        if (node.isFocused && node.isEditable) return node
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val result = findFocusedNode(child)
            if (result != null) return result
        }
        return null
    }
}
