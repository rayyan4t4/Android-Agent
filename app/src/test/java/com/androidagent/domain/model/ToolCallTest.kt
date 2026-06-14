package com.androidagent.domain.model

import org.junit.Assert.*
import org.junit.Test

class ToolCallTest {

    @Test
    fun `toAction converts tap correctly`() {
        val toolCall = ToolCall("Tap", mapOf("x" to "100", "y" to "200"))
        val action = toolCall.toAction()
        assertTrue(action is AgentAction.Tap)
        assertEquals(100, (action as AgentAction.Tap).x)
        assertEquals(200, action.y)
    }

    @Test
    fun `toAction converts swipe correctly`() {
        val toolCall = ToolCall("Swipe", mapOf("startX" to "10", "startY" to "20", "endX" to "30", "endY" to "40"))
        val action = toolCall.toAction()
        assertTrue(action is AgentAction.Swipe)
        val swipe = action as AgentAction.Swipe
        assertEquals(10, swipe.startX)
        assertEquals(40, swipe.endY)
    }

    @Test
    fun `toAction converts type text correctly`() {
        val toolCall = ToolCall("TypeText", mapOf("text" to "hello world"))
        val action = toolCall.toAction()
        assertTrue(action is AgentAction.TypeText)
        assertEquals("hello world", (action as AgentAction.TypeText).text)
    }

    @Test
    fun `toAction converts back correctly`() {
        val toolCall = ToolCall("Back")
        val action = toolCall.toAction()
        assertTrue(action is AgentAction.Back)
    }

    @Test
    fun `toAction converts launch app correctly`() {
        val toolCall = ToolCall("LaunchApp", mapOf("package" to "com.google.android.youtube"))
        val action = toolCall.toAction()
        assertTrue(action is AgentAction.LaunchApp)
        assertEquals("com.google.android.youtube", (action as AgentAction.LaunchApp).packageName)
    }

    @Test
    fun `toAction converts scroll with direction`() {
        val toolCall = ToolCall("Scroll", mapOf("direction" to "DOWN"))
        val action = toolCall.toAction()
        assertTrue(action is AgentAction.Scroll)
        assertEquals(ScrollDirection.DOWN, (action as AgentAction.Scroll).direction)
    }

    @Test
    fun `toAction returns null for missing required params`() {
        val toolCall = ToolCall("Tap", mapOf("x" to "100"))
        assertNull(toolCall.toAction())
    }

    @Test
    fun `toAction returns null for unknown tool`() {
        val toolCall = ToolCall("UnknownTool")
        assertNull(toolCall.toAction())
    }

    @Test
    fun `toAction handles case insensitive tool names`() {
        val toolCall = ToolCall("typetext", mapOf("text" to "test"))
        assertNotNull(toolCall.toAction())
    }

    @Test
    fun `toAction converts find element`() {
        val toolCall = ToolCall("FindElement", mapOf("query" to "Search"))
        val action = toolCall.toAction()
        assertTrue(action is AgentAction.FindElement)
        assertEquals("Search", (action as AgentAction.FindElement).query)
    }
}
