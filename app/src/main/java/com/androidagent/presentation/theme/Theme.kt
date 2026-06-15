package com.androidagent.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AgentPrimary,
    secondary = AgentSecondary,
    background = AgentBackground,
    surface = AgentSurface,
    surfaceVariant = AgentSurfaceVariant,
    error = AgentError,
    onPrimary = AgentOnPrimary,
    onSurface = AgentOnSurface,
    onBackground = AgentOnSurface,
    onSurfaceVariant = AgentOnSurfaceDim
)

private val LightColorScheme = lightColorScheme(
    primary = AgentPrimaryDark,
    secondary = AgentSecondary,
    background = AgentOnPrimary,
    surface = AgentOnPrimary,
    error = AgentError
)

@Composable
fun AndroidAgentTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
