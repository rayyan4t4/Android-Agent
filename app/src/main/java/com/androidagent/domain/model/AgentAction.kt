package com.androidagent.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class AgentAction {
    abstract val description: String

    @Serializable
    data class Tap(val x: Int, val y: Int, override val description: String = "Tap at ($x, $y)") : AgentAction()

    @Serializable
    data class LongPress(val x: Int, val y: Int, val durationMs: Long = 1000, override val description: String = "Long press at ($x, $y)") : AgentAction()

    @Serializable
    data class Swipe(val startX: Int, val startY: Int, val endX: Int, val endY: Int, val durationMs: Long = 300, override val description: String = "Swipe from ($startX,$startY) to ($endX,$endY)") : AgentAction()

    @Serializable
    data class Scroll(val direction: ScrollDirection, val x: Int = -1, val y: Int = -1, override val description: String = "Scroll $direction") : AgentAction()

    @Serializable
    data class TypeText(val text: String, override val description: String = "Type: $text") : AgentAction()

    @Serializable
    data class Back(override val description: String = "Press Back") : AgentAction()

    @Serializable
    data class Home(override val description: String = "Press Home") : AgentAction()

    @Serializable
    data class Recents(override val description: String = "Open Recents") : AgentAction()

    @Serializable
    data class Notifications(override val description: String = "Open Notifications") : AgentAction()

    @Serializable
    data class LaunchApp(val packageName: String, override val description: String = "Launch $packageName") : AgentAction()

    @Serializable
    data class Screenshot(override val description: String = "Take Screenshot") : AgentAction()

    @Serializable
    data class ReadScreen(override val description: String = "Read Screen") : AgentAction()

    @Serializable
    data class Wait(val durationMs: Long = 1000, override val description: String = "Wait ${durationMs}ms") : AgentAction()

    @Serializable
    data class FindElement(val query: String, override val description: String = "Find: $query") : AgentAction()
}

@Serializable
enum class ScrollDirection {
    UP, DOWN, LEFT, RIGHT
}
