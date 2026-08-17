package com.maimonthlyhoppinings.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

@Composable
fun MarkdownPreview(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (markdown.isBlank()) {
            Text(
                text = "Nothing to preview yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            return
        }
        markdown.lineSequence().forEach { raw ->
            val line = raw.trimEnd()
            when {
                line.startsWith("### ") -> PreviewLine(
                    text = inlineMarkdown(line.removePrefix("### ")),
                    style = MaterialTheme.typography.titleMedium,
                    weight = FontWeight.SemiBold,
                )
                line.startsWith("## ") -> PreviewLine(
                    text = inlineMarkdown(line.removePrefix("## ")),
                    style = MaterialTheme.typography.titleLarge,
                    weight = FontWeight.SemiBold,
                )
                line.startsWith("# ") -> PreviewLine(
                    text = inlineMarkdown(line.removePrefix("# ")),
                    style = MaterialTheme.typography.headlineSmall,
                    weight = FontWeight.Bold,
                )
                line.startsWith("> ") -> Text(
                    text = inlineMarkdown(line.removePrefix("> ")),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier.padding(start = 12.dp, bottom = 6.dp),
                )
                line.startsWith("- ") || line.startsWith("* ") -> Text(
                    text = buildAnnotatedString {
                        append("• ")
                        append(inlineMarkdown(line.drop(2)))
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                line.matches(Regex("""^\d+\.\s+.*""")) -> {
                    val body = line.replaceFirst(Regex("""^\d+\.\s+"""), "")
                    val number = line.takeWhile { it.isDigit() }
                    Text(
                        text = buildAnnotatedString {
                            append("$number. ")
                            append(inlineMarkdown(body))
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                line.isBlank() -> Text(
                    text = " ",
                    style = MaterialTheme.typography.bodySmall,
                )
                else -> PreviewLine(
                    text = inlineMarkdown(line),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun PreviewLine(
    text: AnnotatedString,
    style: androidx.compose.ui.text.TextStyle,
    weight: FontWeight? = null,
) {
    Text(
        text = text,
        style = style,
        fontWeight = weight,
        modifier = Modifier.padding(bottom = 6.dp),
    )
}

private fun inlineMarkdown(source: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < source.length) {
            when {
                source.startsWith("**", i) -> {
                    val close = source.indexOf("**", i + 2)
                    if (close > i) {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(source.substring(i + 2, close))
                        }
                        i = close + 2
                    } else {
                        append(source[i])
                        i++
                    }
                }
                source.startsWith("*", i) -> {
                    val close = source.indexOf("*", i + 1)
                    if (close > i) {
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(source.substring(i + 1, close))
                        }
                        i = close + 1
                    } else {
                        append(source[i])
                        i++
                    }
                }
                else -> {
                    append(source[i])
                    i++
                }
            }
        }
    }
}
