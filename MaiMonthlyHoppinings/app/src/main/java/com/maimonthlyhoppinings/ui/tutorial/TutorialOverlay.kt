package com.maimonthlyhoppinings.ui.tutorial

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

private val CardShape = RoundedCornerShape(28.dp)

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
    val holePadding = with(density) { 10.dp.toPx() }
    val colors = MaterialTheme.colorScheme
    val glowColor = colors.primary.copy(alpha = 0.28f)
    val ringColor = colors.primary.copy(alpha = 0.45f)

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
            drawRect(Color(0xFF1A1520).copy(alpha = 0.38f))
            if (localRect != null && localRect.width > 1f && localRect.height > 1f) {
                val hole = Rect(
                    left = localRect.left - holePadding,
                    top = localRect.top - holePadding,
                    right = localRect.right + holePadding,
                    bottom = localRect.bottom + holePadding,
                )
                val corner = 24.dp.toPx()
                for (i in 5 downTo 1) {
                    val expand = i * 5.dp.toPx()
                    drawRoundRect(
                        color = glowColor.copy(alpha = 0.045f * (6 - i)),
                        topLeft = Offset(hole.left - expand, hole.top - expand),
                        size = Size(hole.width + expand * 2, hole.height + expand * 2),
                        cornerRadius = CornerRadius(corner + expand * 0.35f),
                    )
                }
                drawRoundRect(
                    color = Color.Transparent,
                    topLeft = Offset(hole.left, hole.top),
                    size = Size(hole.width, hole.height),
                    cornerRadius = CornerRadius(corner),
                    blendMode = BlendMode.Clear,
                )
                drawRoundRect(
                    color = ringColor,
                    topLeft = Offset(hole.left, hole.top),
                    size = Size(hole.width, hole.height),
                    cornerRadius = CornerRadius(corner),
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            var tooltipHeight by remember { mutableIntStateOf(0) }
            val sidePadding = with(density) { 20.dp.roundToPx() }
            val gap = with(density) { 18.dp.roundToPx() }
            val maxWidthPx = constraints.maxWidth
            val maxHeightPx = constraints.maxHeight
            val tooltipWidth = (maxWidthPx - sidePadding * 2).coerceAtMost(
                with(density) { 360.dp.roundToPx() },
            )
            val offset = tooltipOffset(
                target = localRect,
                tooltipWidth = tooltipWidth,
                tooltipHeight = tooltipHeight.coerceAtLeast(with(density) { 168.dp.roundToPx() }),
                maxWidth = maxWidthPx,
                maxHeight = maxHeightPx,
                sidePadding = sidePadding,
                gap = gap,
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset { offset }
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .onSizeChanged { tooltipHeight = it.height },
                shape = CardShape,
                color = colors.surface,
                tonalElevation = 1.dp,
                shadowElevation = 12.dp,
            ) {
                Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
                    StepDots(
                        count = state.steps.size,
                        index = state.index,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    AnimatedContent(
                        targetState = step,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "tutorial-step",
                    ) { current ->
                        Column {
                            Text(
                                text = current.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Medium,
                                color = colors.onSurface,
                            )
                            Text(
                                text = current.body,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = onSkip,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = colors.onSurfaceVariant,
                            ),
                        ) {
                            Text("Skip", fontWeight = FontWeight.Normal)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (!state.isFirst) {
                                TextButton(
                                    onClick = onBack,
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = colors.onSurfaceVariant,
                                    ),
                                ) {
                                    Text("Back", fontWeight = FontWeight.Normal)
                                }
                            }
                            Button(
                                onClick = onNext,
                                shape = RoundedCornerShape(22.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.primaryContainer,
                                    contentColor = colors.onPrimaryContainer,
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                            ) {
                                Text(
                                    text = if (state.isLast) "All set" else "Continue",
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepDots(
    count: Int,
    index: Int,
) {
    if (count <= 1) return
    val active = MaterialTheme.colorScheme.primary
    val idle = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(count) { i ->
            val selected = i == index
            Box(
                modifier = Modifier
                    .size(width = if (selected) 16.dp else 6.dp, height = 6.dp)
                    .clip(CircleShape)
                    .background(if (selected) active.copy(alpha = 0.8f) else idle),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "${index + 1} of $count",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.End,
        )
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
