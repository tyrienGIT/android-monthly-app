package com.maimonthlyhoppinings.ui.event

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.maimonthlyhoppinings.data.IntensityHeatDay
import com.maimonthlyhoppinings.ui.theme.withIntensityHeat

@Composable
fun EventIntensityHeatmap(
    days: List<IntensityHeatDay>,
    typeColor: Color,
    startLabel: String,
    endLabel: String,
    modifier: Modifier = Modifier,
) {
    if (days.isEmpty()) return

    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp),
    ) {
        Text(
            text = "Intensity heatmap",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Peak intensity per day across this event",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            days.forEach { day ->
                HeatDayCell(
                    day = day,
                    typeColor = typeColor,
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = startLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${days.size} day${if (days.size == 1) "" else "s"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = endLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        IntensityLegend(
            typeColor = typeColor,
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}

@Composable
private fun HeatDayCell(
    day: IntensityHeatDay,
    typeColor: Color,
) {
    val intensity = day.intensity
    val fill = if (intensity != null) {
        typeColor.withIntensityHeat(intensity)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }
    val label = if (intensity != null) intensity.toString() else "·"

    Box(
        modifier = Modifier
            .width(28.dp)
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(fill)
            .border(
                width = 1.dp,
                color = if (intensity != null) {
                    typeColor.copy(alpha = 0.35f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                },
                shape = RoundedCornerShape(6.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (intensity != null) FontWeight.SemiBold else FontWeight.Normal,
            color = if (intensity != null && intensity >= 6) {
                Color.White.copy(alpha = 0.92f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun IntensityLegend(
    typeColor: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Low",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(4.dp)),
        ) {
            (1..10).forEach { intensity ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(10.dp)
                        .background(typeColor.withIntensityHeat(intensity)),
                )
            }
        }
        Text(
            text = "High",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
