package com.androidagent.data.llm

import com.androidagent.domain.model.ScreenState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptBuilder @Inject constructor() {

    private val systemPrompt = """You are an Android AI Agent. You can observe the screen and perform actions using tools.

RULES:
- Always respond with a tool call in JSON format
- Only use the tools listed below
- Provide reasoning before each tool call
- One tool call per response

TOOLS:
- Tap(x, y): Tap at screen coordinates
- LongPress(x, y, duration): Long press at coordinates
- Swipe(startX, startY, endX, endY, duration): Swipe gesture
- Scroll(direction, x, y): Scroll UP/DOWN/LEFT/RIGHT
- TypeText(text): Type text into focused field
- Back(): Press back button
- Home(): Press home button
- Recents(): Open recent apps
- Notifications(): Open notification shade
- LaunchApp(package): Launch app by package name
- Screenshot(): Take a screenshot
- ReadScreen(): Read screen contents
- Wait(duration): Wait milliseconds
- FindElement(query): Find UI element by text

RESPONSE FORMAT:
<think>Your reasoning about what to do next</think>
<tool>{"name": "ToolName", "params": {"key": "value"}}</tool>"""

    fun buildAgentPrompt(
        goal: String,
        screenState: ScreenState,
        recentActions: List<String>,
        planContext: String
    ): String {
        val sb = StringBuilder()
        sb.appendLine("<|im_start|>system")
        sb.appendLine(systemPrompt)
        sb.appendLine("<|im_end|>")
        sb.appendLine("<|im_start|>user")
        sb.appendLine("GOAL: $goal")
        sb.appendLine()
        if (planContext.isNotBlank()) {
            sb.appendLine("PLAN: $planContext")
            sb.appendLine()
        }
        if (recentActions.isNotEmpty()) {
            sb.appendLine("RECENT ACTIONS:")
            recentActions.takeLast(5).forEach { sb.appendLine("- $it") }
            sb.appendLine()
        }
        sb.appendLine("CURRENT SCREEN:")
        sb.appendLine(screenState.toCompactString())
        sb.appendLine("<|im_end|>")
        sb.appendLine("<|im_start|>assistant")
        return sb.toString()
    }

    fun buildPlanningPrompt(goal: String, screenState: ScreenState): String {
        val sb = StringBuilder()
        sb.appendLine("<|im_start|>system")
        sb.appendLine("You are a planning agent. Break down the user's goal into numbered steps.")
        sb.appendLine("Each step should be a single action the agent can take.")
        sb.appendLine("Respond in this format:")
        sb.appendLine("1. Step description [ToolName]")
        sb.appendLine("2. Step description [ToolName]")
        sb.appendLine("<|im_end|>")
        sb.appendLine("<|im_start|>user")
        sb.appendLine("GOAL: $goal")
        sb.appendLine()
        sb.appendLine("CURRENT SCREEN:")
        sb.appendLine(screenState.toCompactString())
        sb.appendLine("<|im_end|>")
        sb.appendLine("<|im_start|>assistant")
        return sb.toString()
    }

    fun buildVerificationPrompt(
        goal: String,
        screenState: ScreenState,
        actionsTaken: List<String>
    ): String {
        val sb = StringBuilder()
        sb.appendLine("<|im_start|>system")
        sb.appendLine("You verify if a task has been completed. Respond with:")
        sb.appendLine("COMPLETED: true/false")
        sb.appendLine("REASON: brief explanation")
        sb.appendLine("<|im_end|>")
        sb.appendLine("<|im_start|>user")
        sb.appendLine("GOAL: $goal")
        sb.appendLine("ACTIONS TAKEN:")
        actionsTaken.forEach { sb.appendLine("- $it") }
        sb.appendLine("CURRENT SCREEN:")
        sb.appendLine(screenState.toCompactString())
        sb.appendLine("<|im_end|>")
        sb.appendLine("<|im_start|>assistant")
        return sb.toString()
    }

    fun buildReflectionPrompt(
        goal: String,
        lastAction: String,
        result: String,
        screenState: ScreenState
    ): String {
        val sb = StringBuilder()
        sb.appendLine("<|im_start|>system")
        sb.appendLine("Evaluate the result of the last action. Decide if the plan needs adjustment.")
        sb.appendLine("Respond with:")
        sb.appendLine("STATUS: success/failure/partial")
        sb.appendLine("NEXT: continue/replan/retry/abort")
        sb.appendLine("REASON: brief explanation")
        sb.appendLine("<|im_end|>")
        sb.appendLine("<|im_start|>user")
        sb.appendLine("GOAL: $goal")
        sb.appendLine("LAST ACTION: $lastAction")
        sb.appendLine("RESULT: $result")
        sb.appendLine("CURRENT SCREEN:")
        sb.appendLine(screenState.toCompactString())
        sb.appendLine("<|im_end|>")
        sb.appendLine("<|im_start|>assistant")
        return sb.toString()
    }
}
