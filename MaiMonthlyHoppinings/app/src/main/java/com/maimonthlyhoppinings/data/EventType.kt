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
        EventTypeDefinition("type_1", "Placeholder type 1", EventTypeColor.Red),
        EventTypeDefinition("type_2", "Placeholder type 2", EventTypeColor.Purple),
        EventTypeDefinition("type_3", "Placeholder type 3", EventTypeColor.Yellow),
        EventTypeDefinition("type_4", "Placeholder type 4", EventTypeColor.Blue),
        EventTypeDefinition("type_5", "Placeholder type 5", EventTypeColor.Green),
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
