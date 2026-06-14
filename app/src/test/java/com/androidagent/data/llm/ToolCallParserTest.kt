package com.androidagent.data.llm

import com.androidagent.domain.model.ToolCall
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ToolCallParserTest {

    private lateinit var parser: ToolCallParser

    @Before
    fun setup() {
        parser = ToolCallParser()
    }

    @Test
    fun `parse valid tool call with think and tool tags`() {
        val output = """
            <think>I need to tap the search button at coordinates 540, 120</think>
            <tool>{"name": "Tap", "params": {"x": "540", "y": "120"}}</tool>
        """.trimIndent()

        val (thinking, toolCall) = parser.parse(output)
        assertEquals("I need to tap the search button at coordinates 540, 120", thinking)
        assertNotNull(toolCall)
        assertEquals("Tap", toolCall!!.toolName)
        assertEquals("540", toolCall.parameters["x"])
        assertEquals("120", toolCall.parameters["y"])
    }

    @Test
    fun `parse tool call without think tags`() {
        val output = """<tool>{"name": "Back", "params": {}}</tool>"""
        val (thinking, toolCall) = parser.parse(output)
        assertEquals("", thinking)
        assertNotNull(toolCall)
        assertEquals("Back", toolCall!!.toolName)
    }

    @Test
    fun `parse tool call with numeric params`() {
        val output = """<tool>{"name": "Swipe", "params": {"startX": 100, "startY": 500, "endX": 100, "endY": 200}}</tool>"""
        val (_, toolCall) = parser.parse(output)
        assertNotNull(toolCall)
        assertEquals("100", toolCall!!.parameters["startX"])
        assertEquals("200", toolCall.parameters["endY"])
    }

    @Test
    fun `parse fallback JSON without tool tags`() {
        val output = """I will tap the button {"name": "Tap", "params": {"x": "300", "y": "600"}}"""
        val (_, toolCall) = parser.parse(output)
        assertNotNull(toolCall)
        assertEquals("Tap", toolCall!!.toolName)
    }

    @Test
    fun `parse returns null for invalid output`() {
        val output = "I don't know what to do"
        val (_, toolCall) = parser.parse(output)
        assertNull(toolCall)
    }

    @Test
    fun `parseVerificationResult extracts completed and reason`() {
        val output = "COMPLETED: true\nREASON: The search results are visible"
        val (completed, reason) = parser.parseVerificationResult(output)
        assertTrue(completed)
        assertEquals("The search results are visible", reason)
    }

    @Test
    fun `parseVerificationResult defaults to false`() {
        val output = "something unrelated"
        val (completed, _) = parser.parseVerificationResult(output)
        assertFalse(completed)
    }

    @Test
    fun `parseReflectionResult extracts next action`() {
        val output = "STATUS: success\nNEXT: continue\nREASON: Action completed successfully"
        val (next, reason) = parser.parseReflectionResult(output)
        assertEquals("continue", next)
        assertEquals("Action completed successfully", reason)
    }

    @Test
    fun `parsePlanSteps extracts numbered steps`() {
        val output = """
            1. Open YouTube app [LaunchApp]
            2. Find the search icon [FindElement]
            3. Tap the search icon [Tap]
            4. Type Flutter tutorials [TypeText]
            5. Submit the search [Tap]
        """.trimIndent()

        val steps = parser.parsePlanSteps(output)
        assertEquals(5, steps.size)
        assertEquals("Open YouTube app", steps[0].first)
        assertEquals("LaunchApp", steps[0].second)
        assertEquals("Type Flutter tutorials", steps[3].first)
        assertEquals("TypeText", steps[3].second)
    }
}
