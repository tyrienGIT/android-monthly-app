package com.maimonthlyhoppinings.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.lerp
import com.maimonthlyhoppinings.data.EventTypeColor
import com.maimonthlyhoppinings.data.EventTypeLookup

fun EventTypeColor.toComposeColor(darkTheme: Boolean = false): Color {
    val base = when (this) {
        EventTypeColor.Red -> Color(0xFFE53935)
        EventTypeColor.Purple -> Color(0xFF8E24AA)
        EventTypeColor.Yellow -> Color(0xFFF9A825)
        EventTypeColor.Blue -> Color(0xFF1E88E5)
        EventTypeColor.Green -> Color(0xFF43A047)
    }
    // Soften slightly for dark mode without going fully pastel.
    return if (darkTheme) lerp(base, Color.White, 0.28f) else base
}

fun colorForEventType(
    typeId: String,
    types: EventTypeLookup,
    darkTheme: Boolean = false,
): Color {
    return types.color(typeId).toComposeColor(darkTheme)
}

@Composable
private fun isAppDarkTheme(): Boolean {
    return MaterialTheme.colorScheme.background.luminance() < 0.5f
}

@Composable
fun colorForEventType(typeId: String, types: EventTypeLookup): Color {
    return colorForEventType(typeId, types, darkTheme = isAppDarkTheme())
}

@Composable
fun EventTypeColor.toComposeColor(): Color {
    return toComposeColor(darkTheme = isAppDarkTheme())
}

/** Intensity 1..10 → heat fill from soft wash to solid (calendar heat bars + event rows). */
fun intensityHeatAlpha(intensity: Int): Float {
    val clamped = intensity.coerceIn(1, 10)
    return 0.18f + (clamped / 10f) * 0.82f
}

fun Color.withIntensityHeat(intensity: Int): Color {
    return copy(alpha = intensityHeatAlpha(intensity))
}

@Composable
fun heatContentColor(typeColor: Color, intensity: Int): Color {
    return if (intensity >= 6) {
        if (typeColor.luminance() > 0.55f) Color.Black else Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }
}
