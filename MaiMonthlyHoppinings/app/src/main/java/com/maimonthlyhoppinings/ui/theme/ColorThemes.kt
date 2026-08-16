package com.maimonthlyhoppinings.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.maimonthlyhoppinings.data.ColorTheme

/**
 * Balanced light/dark Material schemes for each [ColorTheme].
 * Primary / secondary / tertiary are spaced for contrast; neutrals stay readable.
 */
data class AppColorPalette(
    val swatch: Color,
    val light: ColorScheme,
    val dark: ColorScheme,
)

fun ColorTheme.palette(): AppColorPalette {
    val preset = presetPalette()
    return AppColorPalette(
        swatch = preset.swatch,
        light = preset.light.withDerivedContainers(dark = false),
        dark = preset.dark.withDerivedContainers(dark = true),
    )
}

private fun ColorTheme.presetPalette(): AppColorPalette = when (this) {
    ColorTheme.COASTAL -> AppColorPalette(
        swatch = Color(0xFF2F6F6A),
        light = lightColorScheme(
            primary = Color(0xFF2F6F6A),
            onPrimary = Color.White,
            secondary = Color(0xFF1E4A46),
            onSecondary = Color.White,
            tertiary = Color(0xFFC46B5A),
            onTertiary = Color.White,
            background = Color(0xFFF3F6F5),
            onBackground = Color(0xFF1A2422),
            surface = Color(0xFFFBFCFC),
            onSurface = Color(0xFF1A2422),
            surfaceVariant = Color(0xFFD7E3E0),
            onSurfaceVariant = Color(0xFF3F4F4C),
        ),
        dark = darkColorScheme(
            primary = Color(0xFF7EB8B2),
            onPrimary = Color(0xFF003732),
            secondary = Color(0xFFA7CBC6),
            onSecondary = Color(0xFF0E2F2C),
            tertiary = Color(0xFFFFB4A4),
            onTertiary = Color(0xFF5C1C12),
            background = Color(0xFF101716),
            onBackground = Color(0xFFE1E8E6),
            surface = Color(0xFF1A2221),
            onSurface = Color(0xFFE1E8E6),
            surfaceVariant = Color(0xFF3F4F4C),
            onSurfaceVariant = Color(0xFFBFC9C6),
        ),
    )

    ColorTheme.FOREST -> AppColorPalette(
        swatch = Color(0xFF3E6B45),
        light = lightColorScheme(
            primary = Color(0xFF3E6B45),
            onPrimary = Color.White,
            secondary = Color(0xFF2A4A30),
            onSecondary = Color.White,
            tertiary = Color(0xFFB08D3A),
            onTertiary = Color(0xFF1F1600),
            background = Color(0xFFF4F6F1),
            onBackground = Color(0xFF1C221A),
            surface = Color(0xFFFBFCF9),
            onSurface = Color(0xFF1C221A),
            surfaceVariant = Color(0xFFD9E2D4),
            onSurfaceVariant = Color(0xFF414B3D),
        ),
        dark = darkColorScheme(
            primary = Color(0xFF9BC49F),
            onPrimary = Color(0xFF16351B),
            secondary = Color(0xFFB5C9B4),
            onSecondary = Color(0xFF1C2E1F),
            tertiary = Color(0xFFE2C36E),
            onTertiary = Color(0xFF3A2E00),
            background = Color(0xFF121612),
            onBackground = Color(0xFFE2E7DF),
            surface = Color(0xFF1C211B),
            onSurface = Color(0xFFE2E7DF),
            surfaceVariant = Color(0xFF414B3D),
            onSurfaceVariant = Color(0xFFC1CBBA),
        ),
    )

    ColorTheme.SLATE -> AppColorPalette(
        swatch = Color(0xFF4A6572),
        light = lightColorScheme(
            primary = Color(0xFF4A6572),
            onPrimary = Color.White,
            secondary = Color(0xFF334851),
            onSecondary = Color.White,
            tertiary = Color(0xFF5B87A0),
            onTertiary = Color.White,
            background = Color(0xFFF3F5F7),
            onBackground = Color(0xFF1B2125),
            surface = Color(0xFFFBFCFD),
            onSurface = Color(0xFF1B2125),
            surfaceVariant = Color(0xFFD6DEE3),
            onSurfaceVariant = Color(0xFF40484D),
        ),
        dark = darkColorScheme(
            primary = Color(0xFFA8C1CF),
            onPrimary = Color(0xFF1A3340),
            secondary = Color(0xFFB8C8D0),
            onSecondary = Color(0xFF22333B),
            tertiary = Color(0xFF95C0DA),
            onTertiary = Color(0xFF003547),
            background = Color(0xFF111517),
            onBackground = Color(0xFFE1E6EA),
            surface = Color(0xFF1A1F22),
            onSurface = Color(0xFFE1E6EA),
            surfaceVariant = Color(0xFF40484D),
            onSurfaceVariant = Color(0xFFC0C8CD),
        ),
    )

    ColorTheme.EMBER -> AppColorPalette(
        swatch = Color(0xFFA14A3B),
        light = lightColorScheme(
            primary = Color(0xFFA14A3B),
            onPrimary = Color.White,
            secondary = Color(0xFF6E4E45),
            onSecondary = Color.White,
            tertiary = Color(0xFF8A6A4E),
            onTertiary = Color.White,
            background = Color(0xFFF7F3F1),
            onBackground = Color(0xFF241916),
            surface = Color(0xFFFCF9F8),
            onSurface = Color(0xFF241916),
            surfaceVariant = Color(0xFFE8DDD9),
            onSurfaceVariant = Color(0xFF52433F),
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB4A5),
            onPrimary = Color(0xFF5F180E),
            secondary = Color(0xFFD6B8AF),
            onSecondary = Color(0xFF3D2B25),
            tertiary = Color(0xFFE0C0A0),
            onTertiary = Color(0xFF3F2C18),
            background = Color(0xFF181211),
            onBackground = Color(0xFFF0E3DF),
            surface = Color(0xFF221A18),
            onSurface = Color(0xFFF0E3DF),
            surfaceVariant = Color(0xFF52433F),
            onSurfaceVariant = Color(0xFFD6C3BD),
        ),
    )

    ColorTheme.INK -> AppColorPalette(
        swatch = Color(0xFF2F3F6B),
        light = lightColorScheme(
            primary = Color(0xFF2F3F6B),
            onPrimary = Color.White,
            secondary = Color(0xFF414A63),
            onSecondary = Color.White,
            tertiary = Color(0xFFB07A2E),
            onTertiary = Color.White,
            background = Color(0xFFF3F4F8),
            onBackground = Color(0xFF1A1C24),
            surface = Color(0xFFFBFBFD),
            onSurface = Color(0xFF1A1C24),
            surfaceVariant = Color(0xFFD9DDE8),
            onSurfaceVariant = Color(0xFF434753),
        ),
        dark = darkColorScheme(
            primary = Color(0xFFB0C0F0),
            onPrimary = Color(0xFF1A2754),
            secondary = Color(0xFFC0C6DA),
            onSecondary = Color(0xFF2A3145),
            tertiary = Color(0xFFE6BC72),
            onTertiary = Color(0xFF402D00),
            background = Color(0xFF11131A),
            onBackground = Color(0xFFE2E4EC),
            surface = Color(0xFF1A1C24),
            onSurface = Color(0xFFE2E4EC),
            surfaceVariant = Color(0xFF434753),
            onSurfaceVariant = Color(0xFFC3C7D4),
        ),
    )

    ColorTheme.ORCHARD -> AppColorPalette(
        swatch = Color(0xFF6A4A66),
        light = lightColorScheme(
            primary = Color(0xFF6A4A66),
            onPrimary = Color.White,
            secondary = Color(0xFF4F5E4D),
            onSecondary = Color.White,
            tertiary = Color(0xFF7A6B4A),
            onTertiary = Color.White,
            background = Color(0xFFF6F3F5),
            onBackground = Color(0xFF211C20),
            surface = Color(0xFFFCF9FB),
            onSurface = Color(0xFF211C20),
            surfaceVariant = Color(0xFFE5DBE2),
            onSurfaceVariant = Color(0xFF4D444B),
        ),
        dark = darkColorScheme(
            primary = Color(0xFFD9B7D2),
            onPrimary = Color(0xFF3D243A),
            secondary = Color(0xFFB7C5B3),
            onSecondary = Color(0xFF253325),
            tertiary = Color(0xFFD4C29A),
            onTertiary = Color(0xFF3A2F16),
            background = Color(0xFF161215),
            onBackground = Color(0xFFECE3EA),
            surface = Color(0xFF1F1A1E),
            onSurface = Color(0xFFECE3EA),
            surfaceVariant = Color(0xFF4D444B),
            onSurfaceVariant = Color(0xFFD0C3CC),
        ),
    )

    ColorTheme.GROVE -> AppColorPalette(
        swatch = Color(0xFF5A6B35),
        light = lightColorScheme(
            primary = Color(0xFF5A6B35),
            onPrimary = Color.White,
            secondary = Color(0xFF4A5536),
            onSecondary = Color.White,
            tertiary = Color(0xFFC09A1E),
            onTertiary = Color(0xFF241A00),
            background = Color(0xFFF5F6EF),
            onBackground = Color(0xFF1E2117),
            surface = Color(0xFFFBFCF7),
            onSurface = Color(0xFF1E2117),
            surfaceVariant = Color(0xFFE0E4D2),
            onSurfaceVariant = Color(0xFF464B39),
        ),
        dark = darkColorScheme(
            primary = Color(0xFFB7C98A),
            onPrimary = Color(0xFF2D3A10),
            secondary = Color(0xFFC2C9AE),
            onSecondary = Color(0xFF2C331C),
            tertiary = Color(0xFFE9C64A),
            onTertiary = Color(0xFF3B2F00),
            background = Color(0xFF13150F),
            onBackground = Color(0xFFE5E8DB),
            surface = Color(0xFF1C1F16),
            onSurface = Color(0xFFE5E8DB),
            surfaceVariant = Color(0xFF464B39),
            onSurfaceVariant = Color(0xFFC6CBAF),
        ),
    )

    ColorTheme.CANYON -> AppColorPalette(
        swatch = Color(0xFF8A5A3C),
        light = lightColorScheme(
            primary = Color(0xFF8A5A3C),
            onPrimary = Color.White,
            secondary = Color(0xFF615347),
            onSecondary = Color.White,
            tertiary = Color(0xFFB06A3B),
            onTertiary = Color.White,
            background = Color(0xFFF7F3EF),
            onBackground = Color(0xFF221A14),
            surface = Color(0xFFFCF9F6),
            onSurface = Color(0xFF221A14),
            surfaceVariant = Color(0xFFE8DFD6),
            onSurfaceVariant = Color(0xFF51453C),
        ),
        dark = darkColorScheme(
            primary = Color(0xFFE6B896),
            onPrimary = Color(0xFF4A2812),
            secondary = Color(0xFFD0C2B4),
            onSecondary = Color(0xFF372F27),
            tertiary = Color(0xFFFFB68A),
            onTertiary = Color(0xFF532200),
            background = Color(0xFF171210),
            onBackground = Color(0xFFEFE4DB),
            surface = Color(0xFF211A16),
            onSurface = Color(0xFFEFE4DB),
            surfaceVariant = Color(0xFF51453C),
            onSurfaceVariant = Color(0xFFD5C4B7),
        ),
    )

    ColorTheme.GLACIER -> AppColorPalette(
        swatch = Color(0xFF2F6F86),
        light = lightColorScheme(
            primary = Color(0xFF2F6F86),
            onPrimary = Color.White,
            secondary = Color(0xFF3F5A66),
            onSecondary = Color.White,
            tertiary = Color(0xFF4A8FA6),
            onTertiary = Color.White,
            background = Color(0xFFF1F6F8),
            onBackground = Color(0xFF152024),
            surface = Color(0xFFF8FBFC),
            onSurface = Color(0xFF152024),
            surfaceVariant = Color(0xFFD3E1E7),
            onSurfaceVariant = Color(0xFF3E4D53),
        ),
        dark = darkColorScheme(
            primary = Color(0xFF8DCEE6),
            onPrimary = Color(0xFF003544),
            secondary = Color(0xFFB4C8D1),
            onSecondary = Color(0xFF1F343C),
            tertiary = Color(0xFF9DD4E8),
            onTertiary = Color(0xFF003546),
            background = Color(0xFF0F1518),
            onBackground = Color(0xFFDEE7EB),
            surface = Color(0xFF181E21),
            onSurface = Color(0xFFDEE7EB),
            surfaceVariant = Color(0xFF3E4D53),
            onSurfaceVariant = Color(0xFFBDCBD2),
        ),
    )

    ColorTheme.ROSEWOOD -> AppColorPalette(
        swatch = Color(0xFF8B4F5C),
        light = lightColorScheme(
            primary = Color(0xFF8B4F5C),
            onPrimary = Color.White,
            secondary = Color(0xFF4A5B52),
            onSecondary = Color.White,
            tertiary = Color(0xFF7A5A4E),
            onTertiary = Color.White,
            background = Color(0xFFF7F2F3),
            onBackground = Color(0xFF23181B),
            surface = Color(0xFFFCF8F9),
            onSurface = Color(0xFF23181B),
            surfaceVariant = Color(0xFFE8DCDF),
            onSurfaceVariant = Color(0xFF524145),
        ),
        dark = darkColorScheme(
            primary = Color(0xFFFFB1C0),
            onPrimary = Color(0xFF541D2C),
            secondary = Color(0xFFB5C9BE),
            onSecondary = Color(0xFF22352D),
            tertiary = Color(0xFFE4BFAF),
            onTertiary = Color(0xFF432B21),
            background = Color(0xFF171214),
            onBackground = Color(0xFFF0E2E5),
            surface = Color(0xFF211A1C),
            onSurface = Color(0xFFF0E2E5),
            surfaceVariant = Color(0xFF524145),
            onSurfaceVariant = Color(0xFFD6C3C7),
        ),
    )
}
