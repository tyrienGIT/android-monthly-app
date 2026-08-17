package com.maimonthlyhoppinings.ui.trends

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.maimonthlyhoppinings.ui.theme.toComposeColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.hypot

private const val MIN_INTENSITY = 1
private const val MAX_INTENSITY = 10
private const val CONNECT_GAP_DAYS = 4L

private val monthTickFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())

@Composable
fun TrendsChart(
    series: List<TrendSeries>,
    startEpochDay: Long,
    endEpochDay: Long,
    selectedEpochDay: Long?,
    onSelectDay: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    val selectedLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val span = (endEpochDay - startEpochDay).coerceAtLeast(1L)
    val visible = series.filter { it.visible && it.points.isNotEmpty() }
    val monthTicks = remember(startEpochDay, endEpochDay) {
        monthStarts(startEpochDay, endEpochDay)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .pointerInput(visible, startEpochDay, endEpochDay) {
                detectTapGestures { tap ->
                    val plot = ChartPlot(
                        width = size.width.toFloat(),
                        height = size.height.toFloat(),
                        labelGutter = 36.dp.toPx(),
                        bottomGutter = 28.dp.toPx(),
                    )
                    val hit = visible
                        .flatMap { it.points }
                        .minByOrNull { point ->
                            val pos = plot.point(point.epochDay, point.intensity, startEpochDay, span)
                            hypot(pos.x - tap.x, pos.y - tap.y)
                        }
                    if (hit != null) {
                        val pos = plot.point(hit.epochDay, hit.intensity, startEpochDay, span)
                        val distance = hypot(pos.x - tap.x, pos.y - tap.y)
                        onSelectDay(if (distance <= 36.dp.toPx()) hit.epochDay else null)
                    } else {
                        onSelectDay(null)
                    }
                }
            },
    ) {
        val plot = ChartPlot(
            width = size.width,
            height = size.height,
            labelGutter = 36.dp.toPx(),
            bottomGutter = 28.dp.toPx(),
        )

        for (intensity in MIN_INTENSITY..MAX_INTENSITY) {
            if (intensity % 2 != 0 && intensity != MIN_INTENSITY) continue
            val y = plot.yForIntensity(intensity)
            drawLine(
                color = gridColor,
                start = Offset(plot.left, y),
                end = Offset(plot.right, y),
                strokeWidth = 1.dp.toPx(),
            )
            val label = textMeasurer.measure(intensity.toString(), labelStyle)
            drawText(
                textLayoutResult = label,
                topLeft = Offset(
                    x = plot.left - label.size.width - 8.dp.toPx(),
                    y = y - label.size.height / 2f,
                ),
            )
        }

        monthTicks.forEach { epochDay ->
            val x = plot.xForDay(epochDay, startEpochDay, span)
            val label = textMeasurer.measure(
                LocalDate.ofEpochDay(epochDay).format(monthTickFormatter),
                labelStyle,
            )
            drawText(
                textLayoutResult = label,
                topLeft = Offset(
                    x = x - label.size.width / 2f,
                    y = plot.bottom + 8.dp.toPx(),
                ),
            )
        }

        selectedEpochDay?.let { day ->
            val x = plot.xForDay(day, startEpochDay, span)
            drawLine(
                color = selectedLineColor,
                start = Offset(x, plot.top),
                end = Offset(x, plot.bottom),
                strokeWidth = 1.5.dp.toPx(),
            )
        }

        visible.forEach { line ->
            val color = line.color.toComposeColor(darkTheme)
            val positions = line.points.map { point ->
                plot.point(point.epochDay, point.intensity, startEpochDay, span)
            }
            line.points.zipWithNext().forEachIndexed { index, (from, to) ->
                if (to.epochDay - from.epochDay <= CONNECT_GAP_DAYS) {
                    drawLine(
                        color = color,
                        start = positions[index],
                        end = positions[index + 1],
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
            line.points.forEachIndexed { index, point ->
                val center = positions[index]
                val selected = point.epochDay == selectedEpochDay
                drawCircle(
                    color = color,
                    radius = if (selected) 7.dp.toPx() else 4.5.dp.toPx(),
                    center = center,
                )
                if (selected) {
                    drawCircle(
                        color = Color.White,
                        radius = 2.5.dp.toPx(),
                        center = center,
                    )
                }
            }
        }
    }
}

private data class ChartPlot(
    val width: Float,
    val height: Float,
    val labelGutter: Float,
    val bottomGutter: Float,
) {
    val left: Float = labelGutter
    val right: Float = width - 8f
    val top: Float = 8f
    val bottom: Float = height - bottomGutter

    fun xForDay(epochDay: Long, startEpochDay: Long, span: Long): Float {
        val t = (epochDay - startEpochDay).toFloat() / span.toFloat()
        return left + t.coerceIn(0f, 1f) * (right - left)
    }

    fun yForIntensity(intensity: Int): Float {
        val t = (intensity - MIN_INTENSITY).toFloat() / (MAX_INTENSITY - MIN_INTENSITY).toFloat()
        return bottom - t.coerceIn(0f, 1f) * (bottom - top)
    }

    fun point(epochDay: Long, intensity: Int, startEpochDay: Long, span: Long): Offset {
        return Offset(xForDay(epochDay, startEpochDay, span), yForIntensity(intensity))
    }
}

private fun monthStarts(startEpochDay: Long, endEpochDay: Long): List<Long> {
    val ticks = mutableListOf<Long>()
    var cursor = LocalDate.ofEpochDay(startEpochDay).withDayOfMonth(1)
    if (cursor.toEpochDay() < startEpochDay) {
        cursor = cursor.plusMonths(1)
    }
    while (cursor.toEpochDay() <= endEpochDay) {
        ticks += cursor.toEpochDay()
        cursor = cursor.plusMonths(1)
    }
    if (ticks.isEmpty()) {
        ticks += startEpochDay
    }
    return ticks
}
