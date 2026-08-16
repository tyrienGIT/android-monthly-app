package com.maimonthlyhoppinings.ui.event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maimonthlyhoppinings.data.EventTypeLookup
import com.maimonthlyhoppinings.ui.theme.colorForEventType

@Composable
fun eventTypeFieldColors(typeColor: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = typeColor,
    unfocusedBorderColor = typeColor.copy(alpha = 0.75f),
    focusedLabelColor = typeColor,
    unfocusedLabelColor = typeColor.copy(alpha = 0.85f),
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = typeColor,
    focusedPlaceholderColor = typeColor.copy(alpha = 0.55f),
    unfocusedPlaceholderColor = typeColor.copy(alpha = 0.45f),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventTypeDropdown(
    selectedTypeId: String,
    types: EventTypeLookup,
    typeColor: Color,
    onTypeSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = types.label(selectedTypeId),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            label = { Text("Type") },
            leadingIcon = {
                TypeColorDot(color = typeColor)
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = typeColor,
                unfocusedBorderColor = typeColor.copy(alpha = 0.75f),
                focusedLabelColor = typeColor,
                unfocusedLabelColor = typeColor.copy(alpha = 0.85f),
                focusedTextColor = typeColor,
                unfocusedTextColor = typeColor,
                focusedTrailingIconColor = typeColor,
                unfocusedTrailingIconColor = typeColor,
                focusedLeadingIconColor = typeColor,
                unfocusedLeadingIconColor = typeColor,
                cursorColor = typeColor,
            ),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            types.all.forEach { definition ->
                val optionColor = colorForEventType(definition.id, types)
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TypeColorDot(color = optionColor)
                            Text(
                                text = definition.label,
                                color = optionColor,
                                fontWeight = if (definition.id == selectedTypeId) {
                                    FontWeight.SemiBold
                                } else {
                                    FontWeight.Normal
                                },
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                    },
                    onClick = {
                        onTypeSelected(definition.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
fun TypeColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(color),
    )
}
