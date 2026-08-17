package com.maimonthlyhoppinings.data

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun TextFieldValue.wrapInline(marker: String): TextFieldValue {
    val start = selection.min
    val end = selection.max
    val selected = text.substring(start, end).ifEmpty { "text" }
    val inserted = "$marker$selected$marker"
    val newText = text.replaceRange(start, end, inserted)
    val innerStart = start + marker.length
    return copy(
        text = newText,
        selection = TextRange(innerStart, innerStart + selected.length),
    )
}

fun TextFieldValue.prefixSelectedLines(prefix: String): TextFieldValue {
    val start = selection.min
    val end = selection.max
    val lineStart = text.lastIndexOf('\n', (start - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
    val lineEnd = text.indexOf('\n', end).let { if (it < 0) text.length else it }
    val block = text.substring(lineStart, lineEnd)
    val rewritten = block.lineSequence().joinToString("\n") { line ->
        if (line.startsWith(prefix)) line else prefix + line
    }
    val newText = text.replaceRange(lineStart, lineEnd, rewritten)
    return copy(
        text = newText,
        selection = TextRange(lineStart, lineStart + rewritten.length),
    )
}

fun TextFieldValue.toggleHeading(): TextFieldValue {
    val start = selection.min
    val lineStart = text.lastIndexOf('\n', (start - 1).coerceAtLeast(0)).let { if (it < 0) 0 else it + 1 }
    val lineEnd = text.indexOf('\n', start).let { if (it < 0) text.length else it }
    val line = text.substring(lineStart, lineEnd)
    val hashes = line.takeWhile { it == '#' }.length
    val rest = line.drop(hashes).removePrefix(" ")
    val next = when (hashes) {
        0 -> "# $rest"
        1 -> "## $rest"
        2 -> "### $rest"
        else -> rest
    }
    val newText = text.replaceRange(lineStart, lineEnd, next)
    return copy(
        text = newText,
        selection = TextRange(lineStart + next.length),
    )
}
