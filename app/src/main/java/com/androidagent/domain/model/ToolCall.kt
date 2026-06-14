package com.androidagent.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ToolCall(
    val toolName: String,
    val parameters: Map<String, String> = emptyMap(),
    val reasoning: String = ""
) {
    fun toAction(): AgentAction? {
        return when (toolName.lowercase()) {
            "tap" -> {
                val x = parameters["x"]?.toIntOrNull() ?: return null
                val y = parameters["y"]?.toIntOrNull() ?: return null
                AgentAction.Tap(x, y)
            }
            "longpress" -> {
                val x = parameters["x"]?.toIntOrNull() ?: return null
                val y = parameters["y"]?.toIntOrNull() ?: return null
                val duration = parameters["duration"]?.toLongOrNull() ?: 1000L
                AgentAction.LongPress(x, y, duration)
            }
            "swipe" -> {
                val sx = parameters["startX"]?.toIntOrNull() ?: return null
                val sy = parameters["startY"]?.toIntOrNull() ?: return null
                val ex = parameters["endX"]?.toIntOrNull() ?: return null
                val ey = parameters["endY"]?.toIntOrNull() ?: return null
                val duration = parameters["duration"]?.toLongOrNull() ?: 300L
                AgentAction.Swipe(sx, sy, ex, ey, duration)
            }
            "scroll" -> {
                val dir = parameters["direction"]?.uppercase()?.let {
                    try { ScrollDirection.valueOf(it) } catch (_: Exception) { null }
                } ?: ScrollDirection.DOWN
                val x = parameters["x"]?.toIntOrNull() ?: -1
                val y = parameters["y"]?.toIntOrNull() ?: -1
                AgentAction.Scroll(dir, x, y)
            }
            "typetext", "type" -> {
                val text = parameters["text"] ?: return null
                AgentAction.TypeText(text)
            }
            "back" -> AgentAction.Back()
            "home" -> AgentAction.Home()
            "recents" -> AgentAction.Recents()
            "notifications" -> AgentAction.Notifications()
            "launchapp", "launch" -> {
                val pkg = parameters["package"] ?: parameters["packageName"] ?: return null
                AgentAction.LaunchApp(pkg)
            }
            "screenshot" -> AgentAction.Screenshot()
            "readscreen", "read" -> AgentAction.ReadScreen()
            "wait" -> {
                val duration = parameters["duration"]?.toLongOrNull() ?: 1000L
                AgentAction.Wait(duration)
            }
            "findelement", "find" -> {
                val query = parameters["query"] ?: return null
                AgentAction.FindElement(query)
            }
            else -> null
        }
    }
}
