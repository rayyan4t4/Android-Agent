package com.androidagent.data.llm

import com.androidagent.domain.model.Bounds
import com.androidagent.domain.model.OcrResult
import com.androidagent.domain.model.ScreenState
import com.androidagent.domain.model.UiNode
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PromptBuilderTest {

    private lateinit var promptBuilder: PromptBuilder

    @Before
    fun setup() {
        promptBuilder = PromptBuilder()
    }

    private fun createScreenState() = ScreenState(
        timestamp = 0,
        activePackage = "com.google.android.youtube",
        activeActivity = "com.google.android.youtube.HomeActivity",
        nodes = listOf(
            UiNode("search", "android.widget.ImageView", "com.google.android.youtube",
                "", "Search", Bounds(900, 50, 1000, 150),
                true, false, false, false, false, true, false, true, true, emptyList(), 0)
        ),
        ocrResults = listOf(OcrResult("Trending", Bounds(50, 200, 300, 250), 0.9f)),
        screenWidth = 1080,
        screenHeight = 2340
    )

    @Test
    fun `buildAgentPrompt includes system prompt`() {
        val prompt = promptBuilder.buildAgentPrompt("test goal", createScreenState(), emptyList(), "")
        assertTrue(prompt.contains("Android AI Agent"))
        assertTrue(prompt.contains("TOOLS:"))
    }

    @Test
    fun `buildAgentPrompt includes goal`() {
        val prompt = promptBuilder.buildAgentPrompt("Open YouTube", createScreenState(), emptyList(), "")
        assertTrue(prompt.contains("GOAL: Open YouTube"))
    }

    @Test
    fun `buildAgentPrompt includes screen state`() {
        val prompt = promptBuilder.buildAgentPrompt("test", createScreenState(), emptyList(), "")
        assertTrue(prompt.contains("com.google.android.youtube"))
        assertTrue(prompt.contains("Search"))
    }

    @Test
    fun `buildAgentPrompt includes recent actions`() {
        val actions = listOf("Tap at (100, 200) -> OK", "TypeText: hello -> OK")
        val prompt = promptBuilder.buildAgentPrompt("test", createScreenState(), actions, "")
        assertTrue(prompt.contains("RECENT ACTIONS:"))
        assertTrue(prompt.contains("Tap at (100, 200)"))
    }

    @Test
    fun `buildPlanningPrompt includes goal and screen`() {
        val prompt = promptBuilder.buildPlanningPrompt("Search for Flutter tutorials", createScreenState())
        assertTrue(prompt.contains("Search for Flutter tutorials"))
        assertTrue(prompt.contains("planning agent"))
    }

    @Test
    fun `buildVerificationPrompt includes all sections`() {
        val prompt = promptBuilder.buildVerificationPrompt("test goal", createScreenState(), listOf("action1"))
        assertTrue(prompt.contains("GOAL: test goal"))
        assertTrue(prompt.contains("ACTIONS TAKEN:"))
        assertTrue(prompt.contains("COMPLETED:"))
    }

    @Test
    fun `buildAgentPrompt uses ChatML format`() {
        val prompt = promptBuilder.buildAgentPrompt("test", createScreenState(), emptyList(), "")
        assertTrue(prompt.contains("<|im_start|>system"))
        assertTrue(prompt.contains("<|im_end|>"))
        assertTrue(prompt.contains("<|im_start|>assistant"))
    }
}
