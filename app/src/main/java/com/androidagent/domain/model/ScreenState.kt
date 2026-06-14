package com.androidagent.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ScreenState(
    val timestamp: Long,
    val activePackage: String,
    val activeActivity: String,
    val nodes: List<UiNode>,
    val ocrResults: List<OcrResult>,
    val screenWidth: Int,
    val screenHeight: Int
) {
    fun toCompactString(): String {
        val sb = StringBuilder()
        sb.appendLine("App: $activePackage")
        sb.appendLine("Activity: $activeActivity")
        sb.appendLine("Screen: ${screenWidth}x${screenHeight}")
        sb.appendLine("Elements:")
        flattenNodes(nodes, sb, 0)
        if (ocrResults.isNotEmpty()) {
            sb.appendLine("OCR Text:")
            ocrResults.forEach { ocr ->
                sb.appendLine("  \"${ocr.text}\" at (${ocr.boundingBox.left},${ocr.boundingBox.top})-(${ocr.boundingBox.right},${ocr.boundingBox.bottom}) conf=${String.format("%.2f", ocr.confidence)}")
            }
        }
        return sb.toString()
    }

    private fun flattenNodes(nodes: List<UiNode>, sb: StringBuilder, indent: Int) {
        val prefix = "  ".repeat(indent)
        for (node in nodes) {
            if (!node.isVisible) continue
            val attrs = mutableListOf<String>()
            if (node.text.isNotBlank()) attrs.add("text=\"${node.text}\"")
            if (node.contentDescription.isNotBlank()) attrs.add("desc=\"${node.contentDescription}\"")
            if (node.isClickable) attrs.add("clickable")
            if (node.isScrollable) attrs.add("scrollable")
            if (node.isEditable) attrs.add("editable")
            if (node.isCheckable) attrs.add("checkable=${node.isChecked}")
            attrs.add("bounds=(${node.bounds.left},${node.bounds.top},${node.bounds.right},${node.bounds.bottom})")
            val className = node.className.substringAfterLast('.')
            sb.appendLine("$prefix[$className] ${attrs.joinToString(" ")}")
            if (node.children.isNotEmpty()) {
                flattenNodes(node.children, sb, indent + 1)
            }
        }
    }
}
