package com.androidagent.data.llm

import com.androidagent.domain.model.ToolCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ToolCallParser @Inject constructor() {

    fun parse(llmOutput: String): Pair<String, ToolCall?> {
        val thinking = extractThinking(llmOutput)
        val toolCall = extractToolCall(llmOutput)
        return Pair(thinking, toolCall)
    }

    private fun extractThinking(output: String): String {
        val thinkRegex = Regex("<think>(.*?)</think>", RegexOption.DOT_MATCHES_ALL)
        return thinkRegex.find(output)?.groupValues?.get(1)?.trim() ?: ""
    }

    private fun extractToolCall(output: String): ToolCall? {
        val toolRegex = Regex("<tool>(.*?)</tool>", RegexOption.DOT_MATCHES_ALL)
        val toolJson = toolRegex.find(output)?.groupValues?.get(1)?.trim() ?: return tryFallbackParse(output)

        return parseToolJson(toolJson)
    }

    private fun parseToolJson(json: String): ToolCall? {
        try {
            val nameRegex = Regex("\"name\"\\s*:\\s*\"([^\"]+)\"")
            val name = nameRegex.find(json)?.groupValues?.get(1) ?: return null

            val params = mutableMapOf<String, String>()
            val paramsRegex = Regex("\"params\"\\s*:\\s*\\{([^}]*)\\}")
            val paramsBlock = paramsRegex.find(json)?.groupValues?.get(1)

            if (paramsBlock != null) {
                val kvRegex = Regex("\"(\\w+)\"\\s*:\\s*(?:\"([^\"]*)\"|(-?\\d+\\.?\\d*))")
                kvRegex.findAll(paramsBlock).forEach { match ->
                    val key = match.groupValues[1]
                    val value = match.groupValues[2].ifEmpty { match.groupValues[3] }
                    params[key] = value
                }
            }

            return ToolCall(
                toolName = name,
                parameters = params,
                reasoning = ""
            )
        } catch (_: Exception) {
            return null
        }
    }

    private fun tryFallbackParse(output: String): ToolCall? {
        val jsonRegex = Regex("\\{[^{}]*\"name\"[^{}]*\\}", RegexOption.DOT_MATCHES_ALL)
        val match = jsonRegex.find(output) ?: return null
        return parseToolJson(match.value)
    }

    fun parseVerificationResult(output: String): Pair<Boolean, String> {
        val completedRegex = Regex("COMPLETED:\\s*(true|false)", RegexOption.IGNORE_CASE)
        val reasonRegex = Regex("REASON:\\s*(.*)", RegexOption.IGNORE_CASE)

        val completed = completedRegex.find(output)?.groupValues?.get(1)?.equals("true", ignoreCase = true) ?: false
        val reason = reasonRegex.find(output)?.groupValues?.get(1)?.trim() ?: ""

        return Pair(completed, reason)
    }

    fun parseReflectionResult(output: String): Pair<String, String> {
        val nextRegex = Regex("NEXT:\\s*(\\w+)", RegexOption.IGNORE_CASE)
        val reasonRegex = Regex("REASON:\\s*(.*)", RegexOption.IGNORE_CASE)

        val next = nextRegex.find(output)?.groupValues?.get(1)?.lowercase() ?: "continue"
        val reason = reasonRegex.find(output)?.groupValues?.get(1)?.trim() ?: ""

        return Pair(next, reason)
    }

    fun parsePlanSteps(output: String): List<Pair<String, String>> {
        val stepRegex = Regex("\\d+\\.\\s*(.+?)\\s*(?:\\[([^\\]]+)\\])?$", RegexOption.MULTILINE)
        return stepRegex.findAll(output).map { match ->
            val description = match.groupValues[1].trim()
            val toolName = match.groupValues[2].trim()
            Pair(description, toolName)
        }.toList()
    }
}
