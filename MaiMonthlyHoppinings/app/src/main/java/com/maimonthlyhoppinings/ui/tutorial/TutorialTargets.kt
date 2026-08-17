package com.maimonthlyhoppinings.ui.tutorial

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import kotlin.math.abs

class TutorialTargetRegistry {
    private val _targets = mutableStateMapOf<String, Rect>()
    val targets: SnapshotStateMap<String, Rect> = _targets

    fun update(id: String, rect: Rect) {
        val existing = _targets[id]
        if (existing == null || !existing.nearlyEquals(rect)) {
            _targets[id] = rect
        }
    }

    fun remove(id: String) {
        _targets.remove(id)
    }
}

private fun Rect.nearlyEquals(other: Rect): Boolean {
    return abs(left - other.left) < 0.5f &&
        abs(top - other.top) < 0.5f &&
        abs(right - other.right) < 0.5f &&
        abs(bottom - other.bottom) < 0.5f
}

interface TutorialController {
    fun startFullTour()
    fun startSection(section: TutorialSection)
}

private object NoOpTutorialController : TutorialController {
    override fun startFullTour() = Unit
    override fun startSection(section: TutorialSection) = Unit
}

val LocalTutorialTargets = staticCompositionLocalOf { TutorialTargetRegistry() }

val LocalTutorialController = staticCompositionLocalOf<TutorialController> {
    NoOpTutorialController
}

fun Modifier.tutorialTarget(id: String): Modifier = composed {
    val registry = LocalTutorialTargets.current
    DisposableEffect(id, registry) {
        onDispose { registry.remove(id) }
    }
    onGloballyPositioned { coordinates ->
        registry.update(id, coordinates.boundsInWindow())
    }
}

@Composable
fun TutorialHelpAction(section: TutorialSection) {
    val controller = LocalTutorialController.current
    IconButton(onClick = { controller.startSection(section) }) {
        Icon(
            imageVector = Icons.Outlined.HelpOutline,
            contentDescription = "Help",
        )
    }
}
