package com.maimonthlyhoppinings.data

object EmojiTags {
    const val MAX = 3
    private const val SEPARATOR = "\u001F"

    val palette: List<String> = listOf(
        "😊", "🥰", "😌", "😔", "😢", "😭", "😩", "😡", "😰", "😴",
        "🩸", "🤕", "💊", "🌡️", "💧", "🤢", "🚽",
        "⚡", "🔥", "💤", "☁️", "🌈", "✨", "💫",
        "❤️", "💔", "⭐", "🏠", "💼", "🍽️", "🏃", "🛁", "🎵",
    )

    fun decode(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return raw.split(SEPARATOR)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(MAX)
    }

    fun encode(tags: List<String>): String {
        return tags
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(MAX)
            .joinToString(SEPARATOR)
    }

    fun display(raw: String): String {
        val tags = decode(raw)
        val emoji = tags.filterNot(Kaomoji::isKaomoji).joinToString("")
        val faces = tags.filter(Kaomoji::isKaomoji)
        return buildString {
            append(emoji)
            if (emoji.isNotEmpty() && faces.isNotEmpty()) append("  ")
            append(faces.joinToString("  "))
        }
    }

    fun prefix(raw: String, text: String): String {
        val tags = decode(raw)
        val emoji = tags.filterNot(Kaomoji::isKaomoji).joinToString("")
        val faces = tags.filter(Kaomoji::isKaomoji)
        val withEmoji = if (emoji.isEmpty()) text else "$emoji $text"
        return if (faces.isEmpty()) withEmoji else "$withEmoji  ${faces.joinToString("  ")}"
    }

    fun toggle(raw: String, emoji: String): String {
        val current = decode(raw).toMutableList()
        if (current.contains(emoji)) {
            current.remove(emoji)
        } else if (current.size < MAX) {
            current.add(emoji)
        }
        return encode(current)
    }
}
