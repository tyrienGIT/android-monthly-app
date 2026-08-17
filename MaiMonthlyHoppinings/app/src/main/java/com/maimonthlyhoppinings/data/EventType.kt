package com.maimonthlyhoppinings.data

enum class EventTypeColor {
    Red,
    Purple,
    Yellow,
    Blue,
    Green,
    Orange,
    Teal,
    Pink,
    Brown,
    Gray,
    ;

    companion object {
        fun fromName(raw: String): EventTypeColor {
            return entries.firstOrNull { it.name == raw } ?: Red
        }
    }
}

data class EventTypeDefinition(
    val id: String,
    val label: String,
    val color: EventTypeColor,
)

object EventType {
    val seeds: List<EventTypeDefinition> = listOf(
        EventTypeDefinition("type_1", "Period", EventTypeColor.Red),
        EventTypeDefinition("type_2", "Anxious", EventTypeColor.Purple),
        EventTypeDefinition("type_3", "Happy", EventTypeColor.Yellow),
        EventTypeDefinition("type_4", "Sad", EventTypeColor.Blue),
        EventTypeDefinition("type_5", "Cramps", EventTypeColor.Green),
    )

    const val defaultId: String = "type_1"

    fun seedIdForLabel(label: String): String {
        return seeds.firstOrNull { it.label == label }?.id ?: defaultId
    }

    fun seedLabel(id: String): String {
        return seeds.firstOrNull { it.id == id }?.label ?: seeds.first().label
    }

    fun seedColor(id: String): EventTypeColor {
        return seeds.firstOrNull { it.id == id }?.color ?: EventTypeColor.Red
    }

    fun isValidId(id: String, stored: List<EventTypeEntity> = emptyList()): Boolean {
        return stored.any { it.id == id } || seeds.any { it.id == id }
    }
}

data class EventTypeLookup(
    private val types: List<EventTypeEntity>,
) {
    val all: List<EventTypeEntity> = types.ifEmpty { EventType.seeds.map { it.toEntity() } }

    fun label(id: String): String {
        return types.firstOrNull { it.id == id }?.label ?: EventType.seedLabel(id)
    }

    fun color(id: String): EventTypeColor {
        return types.firstOrNull { it.id == id }?.colorEnum() ?: EventType.seedColor(id)
    }
}

fun EventTypeDefinition.toEntity(): EventTypeEntity {
    return EventTypeEntity(id = id, label = label, color = color.name)
}
