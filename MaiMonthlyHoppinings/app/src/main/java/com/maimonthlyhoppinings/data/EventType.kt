package com.maimonthlyhoppinings.data

enum class EventTypeColor {
    Red,
    Purple,
    Yellow,
    Blue,
    Green,
}

data class EventTypeDefinition(
    val label: String,
    val color: EventTypeColor,
)

object EventType {
    val definitions: List<EventTypeDefinition> = listOf(
        EventTypeDefinition("Placeholder type 1", EventTypeColor.Red),
        EventTypeDefinition("Placeholder type 2", EventTypeColor.Purple),
        EventTypeDefinition("Placeholder type 3", EventTypeColor.Yellow),
        EventTypeDefinition("Placeholder type 4", EventTypeColor.Blue),
        EventTypeDefinition("Placeholder type 5", EventTypeColor.Green),
    )

    val options: List<String> = definitions.map { it.label }

    val default: String = options.first()

    fun isValid(value: String): Boolean = value in options

    fun colorFor(label: String): EventTypeColor {
        return definitions.firstOrNull { it.label == label }?.color ?: EventTypeColor.Red
    }
}
