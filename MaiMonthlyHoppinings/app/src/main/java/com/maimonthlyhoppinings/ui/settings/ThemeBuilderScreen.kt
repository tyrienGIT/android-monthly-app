package com.maimonthlyhoppinings.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maimonthlyhoppinings.ui.theme.buildPaletteFromModeSeeds
import com.maimonthlyhoppinings.ui.theme.deriveDarkAccent
import com.maimonthlyhoppinings.ui.theme.deriveLightAccent
import com.maimonthlyhoppinings.ui.theme.safeHsvColor
import kotlin.math.roundToInt

data class ThemeBuilderSavePayload(
    val name: String,
    val lightPrimaryArgb: Int,
    val lightSecondaryArgb: Int,
    val lightTertiaryArgb: Int,
    val darkPrimaryArgb: Int,
    val darkSecondaryArgb: Int,
    val darkTertiaryArgb: Int,
    val apply: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeBuilderScreen(
    onSave: (ThemeBuilderSavePayload) -> Unit,
    onBack: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var linked by remember { mutableStateOf(true) }

    var lightPrimary by remember { mutableStateOf(HsvColor(168f, 0.55f, 0.44f)) }
    var lightSecondary by remember { mutableStateOf(HsvColor(174f, 0.58f, 0.29f)) }
    var lightTertiary by remember { mutableStateOf(HsvColor(10f, 0.54f, 0.55f)) }

    var darkPrimary by remember {
        mutableStateOf(HsvColor.fromColor(deriveDarkAccent(lightPrimary.toColor())))
    }
    var darkSecondary by remember {
        mutableStateOf(HsvColor.fromColor(deriveDarkAccent(lightSecondary.toColor())))
    }
    var darkTertiary by remember {
        mutableStateOf(HsvColor.fromColor(deriveDarkAccent(lightTertiary.toColor())))
    }

    val lightPrimaryColor = lightPrimary.toColor()
    val lightSecondaryColor = lightSecondary.toColor()
    val lightTertiaryColor = lightTertiary.toColor()
    val darkPrimaryColor = darkPrimary.toColor()
    val darkSecondaryColor = darkSecondary.toColor()
    val darkTertiaryColor = darkTertiary.toColor()

    val previewPalette = remember(
        lightPrimaryColor,
        lightSecondaryColor,
        lightTertiaryColor,
        darkPrimaryColor,
        darkSecondaryColor,
        darkTertiaryColor,
    ) {
        buildPaletteFromModeSeeds(
            lightPrimary = lightPrimaryColor,
            lightSecondary = lightSecondaryColor,
            lightTertiary = lightTertiaryColor,
            darkPrimary = darkPrimaryColor,
            darkSecondary = darkSecondaryColor,
            darkTertiary = darkTertiaryColor,
        )
    }

    fun syncDarkFromLight() {
        darkPrimary = HsvColor.fromColor(deriveDarkAccent(lightPrimary.toColor()))
        darkSecondary = HsvColor.fromColor(deriveDarkAccent(lightSecondary.toColor()))
        darkTertiary = HsvColor.fromColor(deriveDarkAccent(lightTertiary.toColor()))
    }

    fun syncLightFromDark() {
        lightPrimary = HsvColor.fromColor(deriveLightAccent(darkPrimary.toColor()))
        lightSecondary = HsvColor.fromColor(deriveLightAccent(darkSecondary.toColor()))
        lightTertiary = HsvColor.fromColor(deriveLightAccent(darkTertiary.toColor()))
    }

    fun setLinked(enabled: Boolean) {
        linked = enabled
        if (enabled) {
            // Prefer the currently edited tab as the source of truth.
            if (selectedTab == 0) syncDarkFromLight() else syncLightFromDark()
        }
    }

    fun updateLightPrimary(value: HsvColor) {
        lightPrimary = value
        if (linked) darkPrimary = HsvColor.fromColor(deriveDarkAccent(value.toColor()))
    }

    fun updateLightSecondary(value: HsvColor) {
        lightSecondary = value
        if (linked) darkSecondary = HsvColor.fromColor(deriveDarkAccent(value.toColor()))
    }

    fun updateLightTertiary(value: HsvColor) {
        lightTertiary = value
        if (linked) darkTertiary = HsvColor.fromColor(deriveDarkAccent(value.toColor()))
    }

    fun updateDarkPrimary(value: HsvColor) {
        darkPrimary = value
        if (linked) lightPrimary = HsvColor.fromColor(deriveLightAccent(value.toColor()))
    }

    fun updateDarkSecondary(value: HsvColor) {
        darkSecondary = value
        if (linked) lightSecondary = HsvColor.fromColor(deriveLightAccent(value.toColor()))
    }

    fun updateDarkTertiary(value: HsvColor) {
        darkTertiary = value
        if (linked) lightTertiary = HsvColor.fromColor(deriveLightAccent(value.toColor()))
    }

    fun save(apply: Boolean) {
        onSave(
            ThemeBuilderSavePayload(
                name = name.trim().ifEmpty { "Custom theme" },
                lightPrimaryArgb = lightPrimaryColor.toArgb(),
                lightSecondaryArgb = lightSecondaryColor.toArgb(),
                lightTertiaryArgb = lightTertiaryColor.toArgb(),
                darkPrimaryArgb = darkPrimaryColor.toArgb(),
                darkSecondaryArgb = darkSecondaryColor.toArgb(),
                darkTertiaryArgb = darkTertiaryColor.toArgb(),
                apply = apply,
            ),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme builder") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "Define accents for light and dark mode. Link them to keep both in sync.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Theme name") },
                    placeholder = { Text("My theme") },
                )
            }
            item {
                DualModePreview(
                    lightScheme = previewPalette.light,
                    darkScheme = previewPalette.dark,
                )
            }
            item {
                LinkModesRow(
                    linked = linked,
                    onLinkedChange = ::setLinked,
                )
            }
            item {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Light mode") },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Dark mode") },
                    )
                }
            }
            if (selectedTab == 0) {
                item {
                    ColorEditorCard("Primary", lightPrimary, ::updateLightPrimary)
                }
                item {
                    ColorEditorCard("Secondary", lightSecondary, ::updateLightSecondary)
                }
                item {
                    ColorEditorCard("Tertiary", lightTertiary, ::updateLightTertiary)
                }
            } else {
                item {
                    ColorEditorCard("Primary", darkPrimary, ::updateDarkPrimary)
                }
                item {
                    ColorEditorCard("Secondary", darkSecondary, ::updateDarkSecondary)
                }
                item {
                    ColorEditorCard("Tertiary", darkTertiary, ::updateDarkTertiary)
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedButton(
                        onClick = { save(apply = false) },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank(),
                    ) {
                        Text("Save")
                    }
                    Button(
                        onClick = { save(apply = true) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Save & apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkModesRow(
    linked: Boolean,
    onLinkedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (linked) Icons.Filled.Link else Icons.Filled.LinkOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Link light & dark",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = if (linked) {
                    "Editing one mode updates the other automatically"
                } else {
                    "Light and dark can be set independently"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Switch(
            checked = linked,
            onCheckedChange = onLinkedChange,
        )
    }
}

@Composable
private fun DualModePreview(
    lightScheme: androidx.compose.material3.ColorScheme,
    darkScheme: androidx.compose.material3.ColorScheme,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Preview",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ModePreviewChip(
                label = "Light",
                background = lightScheme.background,
                surface = lightScheme.surface,
                primary = lightScheme.primary,
                secondary = lightScheme.secondary,
                tertiary = lightScheme.tertiary,
                modifier = Modifier.weight(1f),
            )
            ModePreviewChip(
                label = "Dark",
                background = darkScheme.background,
                surface = darkScheme.surface,
                primary = darkScheme.primary,
                secondary = darkScheme.secondary,
                tertiary = darkScheme.tertiary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ModePreviewChip(
    label: String,
    background: Color,
    surface: Color,
    primary: Color,
    secondary: Color,
    tertiary: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(background)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = label, color = contrastingText(background), fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(surface),
        )
        PaletteSwatchRow(primary = primary, secondary = secondary, tertiary = tertiary)
    }
}

@Composable
private fun ColorEditorCard(
    title: String,
    hsv: HsvColor,
    onChange: (HsvColor) -> Unit,
) {
    var hue by remember { mutableFloatStateOf(hsv.hue) }
    var saturation by remember { mutableFloatStateOf(hsv.saturation) }
    var value by remember { mutableFloatStateOf(hsv.value) }

    LaunchedEffect(hsv) {
        hue = hsv.hue
        saturation = hsv.saturation
        value = hsv.value
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(hsv.toColor())
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), CircleShape),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "#%06X".format(0xFFFFFF and hsv.toColor().toArgb()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HsvSlider("Hue", hue, 0f..360f) {
            hue = it
            onChange(HsvColor(hue, saturation, value))
        }
        HsvSlider("Saturation", saturation, 0f..1f) {
            saturation = it
            onChange(HsvColor(hue, saturation, value))
        }
        HsvSlider("Brightness", value, 0.15f..1f) {
            value = it
            onChange(HsvColor(hue, saturation, value))
        }
    }
}

@Composable
private fun HsvSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    val safeValue = value.coerceIn(valueRange.start, valueRange.endInclusive)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Text(
                text = if (valueRange.endInclusive <= 1.01f) {
                    "${(safeValue * 100).roundToInt()}%"
                } else {
                    "${safeValue.roundToInt()}°"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Slider(
            value = safeValue,
            onValueChange = onValueChange,
            valueRange = valueRange,
        )
    }
}

private data class HsvColor(
    val hue: Float,
    val saturation: Float,
    val value: Float,
) {
    fun toColor(): Color = safeHsvColor(hue, saturation, value)

    companion object {
        fun fromColor(color: Color): HsvColor {
            val hsv = color.let {
                val arr = FloatArray(3)
                android.graphics.Color.RGBToHSV(
                    (it.red * 255f).toInt().coerceIn(0, 255),
                    (it.green * 255f).toInt().coerceIn(0, 255),
                    (it.blue * 255f).toInt().coerceIn(0, 255),
                    arr,
                )
                arr
            }
            return HsvColor(
                hue = hsv[0].let { h -> if (h.isNaN()) 0f else h.coerceIn(0f, 359.999f) },
                saturation = hsv[1].let { s -> if (s.isNaN()) 0f else s.coerceIn(0f, 1f) },
                value = hsv[2].let { v -> if (v.isNaN()) 0f else v.coerceIn(0f, 1f) },
            )
        }
    }
}

private fun contrastingText(background: Color): Color {
    val luminance = 0.2126f * background.red + 0.7152f * background.green + 0.0722f * background.blue
    return if (luminance > 0.5f) Color(0xFF1A1A1A) else Color.White
}
