package com.androidagent.domain.model

import org.junit.Assert.*
import org.junit.Test

class ScreenStateTest {

    @Test
    fun `toCompactString includes app and activity`() {
        val state = ScreenState(
            timestamp = 0,
            activePackage = "com.example.app",
            activeActivity = "MainActivity",
            nodes = emptyList(),
            ocrResults = emptyList(),
            screenWidth = 1080,
            screenHeight = 2340
        )
        val compact = state.toCompactString()
        assertTrue(compact.contains("com.example.app"))
        assertTrue(compact.contains("MainActivity"))
        assertTrue(compact.contains("1080x2340"))
    }

    @Test
    fun `toCompactString includes visible nodes`() {
        val node = UiNode(
            id = "btn_search",
            className = "android.widget.Button",
            packageName = "com.example",
            text = "Search",
            contentDescription = "",
            bounds = Bounds(0, 0, 200, 100),
            isClickable = true,
            isScrollable = false,
            isEditable = false,
            isCheckable = false,
            isChecked = false,
            isFocusable = true,
            isFocused = false,
            isEnabled = true,
            isVisible = true,
            children = emptyList(),
            depth = 0
        )
        val state = ScreenState(
            timestamp = 0,
            activePackage = "test",
            activeActivity = "test",
            nodes = listOf(node),
            ocrResults = emptyList(),
            screenWidth = 1080,
            screenHeight = 2340
        )
        val compact = state.toCompactString()
        assertTrue(compact.contains("Search"))
        assertTrue(compact.contains("clickable"))
    }

    @Test
    fun `toCompactString includes OCR results`() {
        val state = ScreenState(
            timestamp = 0,
            activePackage = "test",
            activeActivity = "test",
            nodes = emptyList(),
            ocrResults = listOf(
                OcrResult("Hello World", Bounds(10, 20, 200, 50), 0.95f)
            ),
            screenWidth = 1080,
            screenHeight = 2340
        )
        val compact = state.toCompactString()
        assertTrue(compact.contains("Hello World"))
        assertTrue(compact.contains("0.95"))
    }

    @Test
    fun `bounds center calculation is correct`() {
        val bounds = Bounds(100, 200, 300, 400)
        assertEquals(200, bounds.centerX)
        assertEquals(300, bounds.centerY)
        assertEquals(200, bounds.width)
        assertEquals(200, bounds.height)
    }
}
