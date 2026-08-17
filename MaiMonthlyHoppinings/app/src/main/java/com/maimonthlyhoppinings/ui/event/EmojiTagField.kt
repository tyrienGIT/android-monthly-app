package com.maimonthlyhoppinings.ui.event

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maimonthlyhoppinings.data.EmojiTags
import com.maimonthlyhoppinings.data.Kaomoji

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmojiTagField(
    emoji: String,
    onEmojiChange: (String) -> Unit,
    typeColor: Color,
    modifier: Modifier = Modifier,
    suggestedMood: String? = null,
) {
    var showPicker by remember { mutableStateOf(false) }
    val selected = EmojiTags.decode(emoji)

    if (showPicker) {
        TagPickerDialog(
            emoji = emoji,
            selected = selected,
            suggestedMood = suggestedMood,
            onEmojiChange = onEmojiChange,
            onDismiss = { showPicker = false },
        )
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Emoji & kaomoji",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = typeColor,
        )
        Text(
            text = if (selected.isEmpty()) {
                "Optional. Up to ${EmojiTags.MAX} — faces or kaomoji."
            } else {
                selected.joinToString("  ")
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        OutlinedButton(
            onClick = { showPicker = true },
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, typeColor),
        ) {
            Text(
                text = if (selected.isEmpty()) "Add tags" else "Change tags",
                color = typeColor,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagPickerDialog(
    emoji: String,
    selected: List<String>,
    suggestedMood: String?,
    onEmojiChange: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    var mood by remember {
        mutableStateOf(Kaomoji.groupForLabel(suggestedMood.orEmpty()).label)
    }
    var customFace by remember { mutableStateOf("") }
    val moodFaces = Kaomoji.groupForLabel(mood).faces

    fun addCustom() {
        val face = customFace.trim()
        if (face.isEmpty()) return
        onEmojiChange(EmojiTags.toggle(emoji, face))
        customFace = ""
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tags") },
        text = {
            Column {
                Text(
                    text = "Pick up to ${EmojiTags.MAX}. Emoji sit in front of the title; kaomoji follow it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                TabRow(selectedTabIndex = tab) {
                    Tab(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        text = { Text("Emoji") },
                    )
                    Tab(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        text = { Text("Kaomoji") },
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (tab == 0) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            EmojiTags.palette.forEach { item ->
                                TagChip(
                                    label = item,
                                    selected = selected.contains(item),
                                    atLimit = selected.size >= EmojiTags.MAX,
                                    emojiSize = true,
                                    onClick = { onEmojiChange(EmojiTags.toggle(emoji, item)) },
                                )
                            }
                        }
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Kaomoji.groups.forEach { group ->
                                FilterChip(
                                    selected = group.label == mood,
                                    onClick = { mood = group.label },
                                    label = { Text(group.label) },
                                )
                            }
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            moodFaces.forEach { face ->
                                TagChip(
                                    label = face,
                                    selected = selected.contains(face),
                                    atLimit = selected.size >= EmojiTags.MAX,
                                    emojiSize = false,
                                    onClick = { onEmojiChange(EmojiTags.toggle(emoji, face)) },
                                )
                            }
                        }
                        OutlinedTextField(
                            value = customFace,
                            onValueChange = { customFace = it.take(40) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Or type your own") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { addCustom() }),
                        )
                        TextButton(
                            onClick = { addCustom() },
                            enabled = customFace.isNotBlank(),
                        ) {
                            Text("Add kaomoji")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        dismissButton = {
            if (selected.isNotEmpty()) {
                TextButton(onClick = { onEmojiChange("") }) {
                    Text("Clear")
                }
            }
        },
    )
}

@Composable
private fun TagChip(
    label: String,
    selected: Boolean,
    atLimit: Boolean,
    emojiSize: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        enabled = selected || !atLimit,
        label = {
            Text(
                text = label,
                fontSize = if (emojiSize) 20.sp else 14.sp,
            )
        },
    )
}
