package com.androidagent.data.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.androidagent.domain.model.Bounds
import com.androidagent.domain.model.UiNode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AgentAccessibilityService : AccessibilityService() {

    companion object {
        private val _instance = MutableStateFlow<AgentAccessibilityService?>(null)
        val instance: StateFlow<AgentAccessibilityService?> = _instance.asStateFlow()

        fun isRunning(): Boolean = _instance.value != null
    }

    private val _currentNodes = MutableStateFlow<List<UiNode>>(emptyList())
    val currentNodes: StateFlow<List<UiNode>> = _currentNodes.asStateFlow()

    private val _activePackage = MutableStateFlow("")
    val activePackage: StateFlow<String> = _activePackage.asStateFlow()

    private val _activeActivity = MutableStateFlow("")
    val activeActivity: StateFlow<String> = _activeActivity.asStateFlow()

    override fun onServiceConnected() {
        super.onServiceConnected()
        _instance.value = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                event.packageName?.let { _activePackage.value = it.toString() }
                event.className?.let { _activeActivity.value = it.toString() }
                refreshNodes()
            }
            else -> {}
        }
    }

    override fun onInterrupt() {
        _instance.value = null
    }

    override fun onDestroy() {
        super.onDestroy()
        _instance.value = null
    }

    fun refreshNodes() {
        val root = rootInActiveWindow ?: return
        val nodes = UiHierarchyParser.parse(root)
        _currentNodes.value = nodes
        root.recycle()
    }

    fun getScreenDimensions(): Pair<Int, Int> {
        val metrics = resources.displayMetrics
        return Pair(metrics.widthPixels, metrics.heightPixels)
    }

    suspend fun performTap(x: Int, y: Int): Boolean {
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        return dispatchGestureAndWait(gesture)
    }

    suspend fun performLongPress(x: Int, y: Int, durationMs: Long): Boolean {
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
            .build()
        return dispatchGestureAndWait(gesture)
    }

    suspend fun performSwipe(startX: Int, startY: Int, endX: Int, endY: Int, durationMs: Long): Boolean {
        val path = Path().apply {
            moveTo(startX.toFloat(), startY.toFloat())
            lineTo(endX.toFloat(), endY.toFloat())
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, durationMs))
            .build()
        return dispatchGestureAndWait(gesture)
    }

    fun performBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)
    fun performHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)
    fun performRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)
    fun performNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)

    fun findNodeByText(text: String): AccessibilityNodeInfo? {
        val root = rootInActiveWindow ?: return null
        val nodes = root.findAccessibilityNodeInfosByText(text)
        return nodes.firstOrNull()
    }

    fun inputText(node: AccessibilityNodeInfo, text: String): Boolean {
        val arguments = android.os.Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
    }

    private suspend fun dispatchGestureAndWait(gesture: GestureDescription): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        val callback = object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                deferred.complete(true)
            }
            override fun onCancelled(gestureDescription: GestureDescription?) {
                deferred.complete(false)
            }
        }
        dispatchGesture(gesture, callback, null)
        return deferred.await()
    }
}

object UiHierarchyParser {
    fun parse(root: AccessibilityNodeInfo, depth: Int = 0): List<UiNode> {
        val rootNode = parseNode(root, depth) ?: return emptyList()
        return listOf(rootNode)
    }

    private fun parseNode(node: AccessibilityNodeInfo, depth: Int): UiNode? {
        val rect = Rect()
        node.getBoundsInScreen(rect)

        val children = mutableListOf<UiNode>()
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val parsedChild = parseNode(child, depth + 1)
            if (parsedChild != null) {
                children.add(parsedChild)
            }
            child.recycle()
        }

        return UiNode(
            id = node.viewIdResourceName ?: "",
            className = node.className?.toString() ?: "",
            packageName = node.packageName?.toString() ?: "",
            text = node.text?.toString() ?: "",
            contentDescription = node.contentDescription?.toString() ?: "",
            bounds = Bounds(rect.left, rect.top, rect.right, rect.bottom),
            isClickable = node.isClickable,
            isScrollable = node.isScrollable,
            isEditable = node.isEditable,
            isCheckable = node.isCheckable,
            isChecked = node.isChecked,
            isFocusable = node.isFocusable,
            isFocused = node.isFocused,
            isEnabled = node.isEnabled,
            isVisible = node.isVisibleToUser,
            children = children,
            depth = depth
        )
    }
}
