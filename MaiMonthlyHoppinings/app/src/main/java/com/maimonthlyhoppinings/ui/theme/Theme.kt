package com.maimonthlyhoppinings.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.maimonthlyhoppinings.data.ColorTheme
import com.maimonthlyhoppinings.data.ThemeMode

@Composable
fun MaiMonthlyHoppiningsTheme(
    themeMode: ThemeMode = ThemeMode.default,
    palette: AppColorPalette = ColorTheme.default.palette(),
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    MaterialTheme(
        colorScheme = if (darkTheme) palette.dark else palette.light,
        content = content,
    )
}
