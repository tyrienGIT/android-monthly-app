package com.maimonthlyhoppinings.ui.tutorial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

@Composable
fun TutorialOverlay(
    state: TutorialUiState,
    targets: Map<String, Rect>,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    val step = state.step ?: return
    val targetRect = step.targetId?.let { targets[it] }
    var overlayOrigin by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    val holePadding = with(density) { 6.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(8f)
            .onGloballyPositioned { coordinates ->
                overlayOrigin = coordinates.positionInWindow()
            }
            .pointerInput(Unit) { detectTapGestures { } },
    ) {
        val localRect = targetRect?.translate(-overlayOrigin.x, -overlayOrigin.y)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
        ) {
            drawRect(Color.Black.copy(alpha = 0.58f))
            if (localRect != null && localRect.width > 1f && localRect.height > 1f) {
                val hole = Rect(
                    left = localRect.left - holePadding,
                    top = localRect.top - holePadding,
                    right = localRect.right + holePadding,
                    bottom = localRect.bottom + holePadding,
                )
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(hole.left, hole.top),
                    size = Size(hole.width, hole.height),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                    blendMode = BlendMode.Clear,
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            var tooltipHeight by remember { mutableIntStateOf(0) }
            val sidePadding = with(density) { 16.dp.roundToPx() }
            val gap = with(density) { 12.dp.roundToPx() }
            val maxWidthPx = constraints.maxWidth
            val maxHeightPx = constraints.maxHeight
            val tooltipWidth = (maxWidthPx - sidePadding * 2).coerceAtMost(
                with(density) { 400.dp.roundToPx() },
            )
            val offset = tooltipOffset(
                target = localRect,
                tooltipWidth = tooltipWidth,
                tooltipHeight = tooltipHeight.coerceAtLeast(with(density) { 160.dp.roundToPx() }),
                maxWidth = maxWidthPx,
                maxHeight = maxHeightPx,
                sidePadding = sidePadding,
                gap = gap,
            )

            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset { offset }
                    .widthIn(max = 400.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .onSizeChanged { tooltipHeight = it.height },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text(
                        text = step.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = step.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(onClick = onSkip) {
                            Text("Skip")
                        }
                        Row {
                            if (!state.isFirst) {
                                TextButton(onClick = onBack) {
                                    Text("Back")
                                }
                            }
                            TextButton(onClick = onNext) {
                                Text(if (state.isLast) "Done" else "Next")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun tooltipOffset(
    target: Rect?,
    tooltipWidth: Int,
    tooltipHeight: Int,
    maxWidth: Int,
    maxHeight: Int,
    sidePadding: Int,
    gap: Int,
): IntOffset {
    val x = sidePadding
    if (target == null || target.width <= 1f || target.height <= 1f) {
        return IntOffset(
            x = x,
            y = ((maxHeight - tooltipHeight) / 2).coerceAtLeast(sidePadding),
        )
    }
    val below = target.bottom.roundToInt() + gap
    val above = target.top.roundToInt() - gap - tooltipHeight
    val y = when {
        below + tooltipHeight <= maxHeight - sidePadding -> below
        above >= sidePadding -> above
        else -> ((maxHeight - tooltipHeight) / 2).coerceAtLeast(sidePadding)
    }
    return IntOffset(
        x = x.coerceAtMost((maxWidth - tooltipWidth - sidePadding).coerceAtLeast(0)),
        y = y.coerceIn(sidePadding, (maxHeight - tooltipHeight - sidePadding).coerceAtLeast(sidePadding)),
    )
}
