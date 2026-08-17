package com.maimonthlyhoppinings.ui.trends

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.maimonthlyhoppinings.ui.theme.toComposeColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private const val MIN_INTENSITY = 0
private const val MAX_INTENSITY = 10

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
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.28f)
    val selectedLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.28f)
    val darkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val span = (endEpochDay - startEpochDay).coerceAtLeast(1L)
    val visible = series.filter { it.points.isNotEmpty() }
    val monthTicks = remember(startEpochDay, endEpochDay) {
        monthStarts(startEpochDay, endEpochDay)
    }
    val fillAlpha = if (visible.size <= 1) 0.38f else 0.16f
    val strokeWidth = if (visible.size <= 1) 3.dp else 2.dp

    fun plotFor(width: Float, height: Float, labelGutter: Float, bottomGutter: Float): ChartPlot {
        return ChartPlot(width, height, labelGutter, bottomGutter)
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(startEpochDay, endEpochDay) {
                val gutter = 36.dp.toPx()
                val bottom = 28.dp.toPx()
                detectTapGestures { tap ->
                    val plot = plotFor(size.width.toFloat(), size.height.toFloat(), gutter, bottom)
                    onSelectDay(plot.dayAtX(tap.x, startEpochDay, span))
                }
            }
            .pointerInput(startEpochDay, endEpochDay) {
                val gutter = 36.dp.toPx()
                val bottom = 28.dp.toPx()
                detectHorizontalDragGestures(
                    onDragStart = { start ->
                        val plot = plotFor(size.width.toFloat(), size.height.toFloat(), gutter, bottom)
                        onSelectDay(plot.dayAtX(start.x, startEpochDay, span))
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        val plot = plotFor(size.width.toFloat(), size.height.toFloat(), gutter, bottom)
                        onSelectDay(plot.dayAtX(change.position.x, startEpochDay, span))
                    },
                )
            },
    ) {
        val plot = plotFor(size.width, size.height, 36.dp.toPx(), 28.dp.toPx())

        for (intensity in listOf(0, 5, 10)) {
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
                    x = (x - label.size.width / 2f).coerceIn(plot.left, plot.right - label.size.width),
                    y = plot.bottom + 8.dp.toPx(),
                ),
            )
        }

        visible.forEach { line ->
            val color = line.color.toComposeColor(darkTheme)
            val positions = dailyPositions(line.points, plot, startEpochDay, endEpochDay, span)
            if (positions.size < 2) return@forEach

            val curve = smoothPath(positions)
            val fill = Path().apply {
                addPath(curve)
                lineTo(positions.last().x, plot.bottom)
                lineTo(positions.first().x, plot.bottom)
                close()
            }
            drawPath(
                path = fill,
                brush = Brush.verticalGradient(
                    colors = listOf(color.copy(alpha = fillAlpha), color.copy(alpha = 0.02f)),
                    startY = plot.top,
                    endY = plot.bottom,
                ),
            )
            drawPath(
                path = curve,
                color = color.copy(alpha = 0.92f),
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }

        selectedEpochDay?.let { day ->
            val x = plot.xForDay(day, startEpochDay, span)
            drawLine(
                color = selectedLineColor,
                start = Offset(x, plot.top),
                end = Offset(x, plot.bottom),
                strokeWidth = 1.25.dp.toPx(),
            )
            visible.forEach { line ->
                val intensity = line.points
                    .firstOrNull { it.epochDay == day }
                    ?.intensity
                    ?: 0
                if (intensity > 0) {
                    val color = line.color.toComposeColor(darkTheme)
                    val center = plot.point(day, intensity, startEpochDay, span)
                    drawCircle(color = color, radius = 6.dp.toPx(), center = center)
                    drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = center)
                }
            }
        }
    }
}

private fun dailyPositions(
    points: List<TrendPoint>,
    plot: ChartPlot,
    startEpochDay: Long,
    endEpochDay: Long,
    span: Long,
): List<Offset> {
    val byDay = points.associate { it.epochDay to it.intensity }
    return (startEpochDay..endEpochDay).map { day ->
        plot.point(day, byDay[day] ?: 0, startEpochDay, span)
    }
}

private fun smoothPath(points: List<Offset>): Path {
    val path = Path()
    if (points.isEmpty()) return path
    path.moveTo(points.first().x, points.first().y)
    if (points.size == 1) return path
    for (index in 0 until points.lastIndex) {
        val previous = points.getOrElse(index - 1) { points[index] }
        val current = points[index]
        val next = points[index + 1]
        val after = points.getOrElse(index + 2) { next }
        val control1 = Offset(
            x = current.x + (next.x - previous.x) / 6f,
            y = current.y + (next.y - previous.y) / 6f,
        )
        val control2 = Offset(
            x = next.x - (after.x - current.x) / 6f,
            y = next.y - (after.y - current.y) / 6f,
        )
        path.cubicTo(control1.x, control1.y, control2.x, control2.y, next.x, next.y)
    }
    return path
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

    fun dayAtX(x: Float, startEpochDay: Long, span: Long): Long {
        val t = ((x - left) / (right - left)).coerceIn(0f, 1f)
        return startEpochDay + (t * span).roundToInt().toLong()
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
