package com.androidagent.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UiNode(
    val id: String,
    val className: String,
    val packageName: String,
    val text: String,
    val contentDescription: String,
    val bounds: Bounds,
    val isClickable: Boolean,
    val isScrollable: Boolean,
    val isEditable: Boolean,
    val isCheckable: Boolean,
    val isChecked: Boolean,
    val isFocusable: Boolean,
    val isFocused: Boolean,
    val isEnabled: Boolean,
    val isVisible: Boolean,
    val children: List<UiNode>,
    val depth: Int
)

@Serializable
data class Bounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
) {
    val centerX: Int get() = (left + right) / 2
    val centerY: Int get() = (top + bottom) / 2
    val width: Int get() = right - left
    val height: Int get() = bottom - top
}
