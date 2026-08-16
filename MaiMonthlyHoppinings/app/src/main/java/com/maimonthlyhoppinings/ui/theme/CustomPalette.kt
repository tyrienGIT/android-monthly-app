package com.maimonthlyhoppinings.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.lerp
import com.maimonthlyhoppinings.data.SavedColorTheme
import android.graphics.Color as AndroidColor

fun SavedColorTheme.toPalette(): AppColorPalette {
    return buildPaletteFromModeSeeds(
        lightPrimary = Color(lightPrimaryArgb),
        lightSecondary = Color(lightSecondaryArgb),
        lightTertiary = Color(lightTertiaryArgb),
        darkPrimary = Color(darkPrimaryArgb),
        darkSecondary = Color(darkSecondaryArgb),
        darkTertiary = Color(darkTertiaryArgb),
    )
}

/**
 * Builds balanced light + dark schemes from mode-specific accent seeds.
 * Light accents should read on pale surfaces; dark accents on near-black surfaces.
 */
fun buildPaletteFromModeSeeds(
    lightPrimary: Color,
    lightSecondary: Color,
    lightTertiary: Color,
    darkPrimary: Color,
    darkSecondary: Color,
    darkTertiary: Color,
): AppColorPalette {
    return AppColorPalette(
        swatch = lightPrimary,
        light = buildLightScheme(
            primary = normalizeLightAccent(lightPrimary),
            secondary = normalizeLightAccent(lightSecondary),
            tertiary = normalizeLightAccent(lightTertiary),
        ),
        dark = buildDarkScheme(
            primary = normalizeDarkAccent(darkPrimary),
            secondary = normalizeDarkAccent(darkSecondary),
            tertiary = normalizeDarkAccent(darkTertiary),
        ),
    )
}

/** Convenience: derive a full dual-mode palette from three light-mode seeds. */
fun buildPaletteFromSeeds(
    primary: Color,
    secondary: Color,
    tertiary: Color,
): AppColorPalette {
    val lightPrimary = normalizeLightAccent(primary)
    val lightSecondary = normalizeLightAccent(secondary)
    val lightTertiary = normalizeLightAccent(tertiary)
    return buildPaletteFromModeSeeds(
        lightPrimary = lightPrimary,
        lightSecondary = lightSecondary,
        lightTertiary = lightTertiary,
        darkPrimary = deriveDarkAccent(lightPrimary),
        darkSecondary = deriveDarkAccent(lightSecondary),
        darkTertiary = deriveDarkAccent(lightTertiary),
    )
}

fun deriveDarkAccent(lightAccent: Color): Color {
    val hsv = lightAccent.toHsv()
    val hue = hsv[0]
    val saturation = (hsv[1] * 0.72f).coerceIn(0.28f, 0.82f)
    val value = (0.78f + hsv[2] * 0.12f).coerceIn(0.68f, 0.92f)
    return safeHsvColor(hue, saturation, value)
}

fun deriveLightAccent(darkAccent: Color): Color {
    val hsv = darkAccent.toHsv()
    val hue = hsv[0]
    val saturation = (hsv[1] * 1.15f).coerceIn(0.35f, 0.88f)
    val value = (hsv[2] * 0.55f).coerceIn(0.28f, 0.58f)
    return safeHsvColor(hue, saturation, value)
}

private fun normalizeLightAccent(color: Color): Color {
    val hsv = color.toHsv()
    // Keep accents deep enough for contrast on pale backgrounds.
    val saturation = hsv[1].coerceIn(0.28f, 0.90f)
    val value = hsv[2].coerceIn(0.28f, 0.72f)
    return safeHsvColor(hsv[0], saturation, value)
}

private fun normalizeDarkAccent(color: Color): Color {
    val hsv = color.toHsv()
    // Keep accents bright enough for contrast on dark backgrounds.
    val saturation = hsv[1].coerceIn(0.22f, 0.85f)
    val value = hsv[2].coerceIn(0.62f, 0.94f)
    return safeHsvColor(hsv[0], saturation, value)
}

private fun buildLightScheme(
    primary: Color,
    secondary: Color,
    tertiary: Color,
): ColorScheme {
    val background = tintedNeutral(base = Color(0xFFF6F6F6), tint = primary, amount = 0.05f)
    val surface = tintedNeutral(base = Color(0xFFFCFCFC), tint = primary, amount = 0.03f)
    val surfaceVariant = tintedNeutral(base = Color(0xFFE4E6E8), tint = primary, amount = 0.12f)
    val onBg = Color(0xFF1A1C1E)
    val primaryContainer = lerp(primary, Color.White, 0.82f)
    val secondaryContainer = lerp(secondary, Color.White, 0.82f)
    val tertiaryContainer = lerp(tertiary, Color.White, 0.82f)

    return lightColorScheme(
        primary = primary,
        onPrimary = contrastingOn(primary),
        primaryContainer = primaryContainer,
        onPrimaryContainer = contrastingOn(primaryContainer),
        secondary = secondary,
        onSecondary = contrastingOn(secondary),
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = contrastingOn(secondaryContainer),
        tertiary = tertiary,
        onTertiary = contrastingOn(tertiary),
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = contrastingOn(tertiaryContainer),
        background = background,
        onBackground = onBg,
        surface = surface,
        onSurface = onBg,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = Color(0xFF42474E),
        outline = Color(0xFF72787E),
        outlineVariant = Color(0xFFC2C7CE),
        inverseSurface = Color(0xFF2F3133),
        inverseOnSurface = Color(0xFFF0F1F3),
        inversePrimary = deriveDarkAccent(primary),
        surfaceTint = primary,
        scrim = Color.Black,
    )
}

private fun buildDarkScheme(
    primary: Color,
    secondary: Color,
    tertiary: Color,
): ColorScheme {
    val background = tintedNeutral(base = Color(0xFF101214), tint = primary, amount = 0.10f)
    val surface = tintedNeutral(base = Color(0xFF1A1C1E), tint = primary, amount = 0.12f)
    val surfaceVariant = tintedNeutral(base = Color(0xFF2B3036), tint = primary, amount = 0.16f)
    val onBg = Color(0xFFE2E2E6)
    val primaryContainer = lerp(primary, Color.Black, 0.55f)
    val secondaryContainer = lerp(secondary, Color.Black, 0.55f)
    val tertiaryContainer = lerp(tertiary, Color.Black, 0.55f)

    return darkColorScheme(
        primary = primary,
        onPrimary = contrastingOn(primary),
        primaryContainer = primaryContainer,
        onPrimaryContainer = contrastingOn(primaryContainer),
        secondary = secondary,
        onSecondary = contrastingOn(secondary),
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = contrastingOn(secondaryContainer),
        tertiary = tertiary,
        onTertiary = contrastingOn(tertiary),
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = contrastingOn(tertiaryContainer),
        background = background,
        onBackground = onBg,
        surface = surface,
        onSurface = onBg,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = Color(0xFFC2C7CE),
        outline = Color(0xFF8C9198),
        outlineVariant = Color(0xFF42474E),
        inverseSurface = Color(0xFFE2E2E6),
        inverseOnSurface = Color(0xFF2F3133),
        inversePrimary = deriveLightAccent(primary),
        surfaceTint = primary,
        scrim = Color.Black,
    )
}

private fun tintedNeutral(base: Color, tint: Color, amount: Float): Color {
    return lerp(base, tint, amount.coerceIn(0f, 1f))
}

/**
 * Material3 [lightColorScheme]/[darkColorScheme] leave unspecified container roles on the
 * default purple baseline. Derive them from the scheme's accents so FABs/chips follow the theme.
 */
fun ColorScheme.withDerivedContainers(dark: Boolean): ColorScheme {
    val primaryContainer = if (dark) {
        lerp(primary, Color.Black, 0.55f)
    } else {
        lerp(primary, Color.White, 0.82f)
    }
    val secondaryContainer = if (dark) {
        lerp(secondary, Color.Black, 0.55f)
    } else {
        lerp(secondary, Color.White, 0.82f)
    }
    val tertiaryContainer = if (dark) {
        lerp(tertiary, Color.Black, 0.55f)
    } else {
        lerp(tertiary, Color.White, 0.82f)
    }
    return copy(
        primaryContainer = primaryContainer,
        onPrimaryContainer = contrastingOn(primaryContainer),
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = contrastingOn(secondaryContainer),
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = contrastingOn(tertiaryContainer),
        surfaceTint = primary,
        inversePrimary = if (dark) deriveLightAccent(primary) else deriveDarkAccent(primary),
    )
}

private fun contrastingOn(background: Color): Color {
    return if (background.luminance() > 0.45f) Color(0xFF1A1C1E) else Color(0xFFF8F9FA)
}

fun Color.toHsv(): FloatArray {
    val hsv = FloatArray(3)
    AndroidColor.RGBToHSV(
        (red * 255f).toInt().coerceIn(0, 255),
        (green * 255f).toInt().coerceIn(0, 255),
        (blue * 255f).toInt().coerceIn(0, 255),
        hsv,
    )
    // Compose Color.hsv rejects NaN / out-of-range values (can crash theme apply).
    hsv[0] = hsv[0].safeHue()
    hsv[1] = hsv[1].coerceIn(0f, 1f).nanTo(0f)
    hsv[2] = hsv[2].coerceIn(0f, 1f).nanTo(0f)
    return hsv
}

fun Float.safeHue(): Float {
    if (isNaN() || isInfinite()) return 0f
    return (this % 360f).let { if (it < 0f) it + 360f else it }.coerceIn(0f, 359.999f)
}

private fun Float.nanTo(fallback: Float): Float = if (isNaN() || isInfinite()) fallback else this

fun safeHsvColor(hue: Float, saturation: Float, value: Float): Color {
    return Color.hsv(
        hue = hue.safeHue(),
        saturation = saturation.coerceIn(0f, 1f).nanTo(0f),
        value = value.coerceIn(0f, 1f).nanTo(0f),
    )
}
