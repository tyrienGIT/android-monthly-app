package com.maimonthlyhoppinings.data

data class Book(
    val id: String,
    val name: String,
    val databaseName: String,
    val createdAtMillis: Long,
) {
    val isDefault: Boolean
        get() = id == DEFAULT_ID

    fun fileSlug(): String {
        val fromName = name.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(24)
            .ifEmpty { "book" }
        return if (isDefault) fromName else "$fromName-${id.take(8)}"
    }

    companion object {
        const val DEFAULT_ID = "default"
        const val DEFAULT_NAME = "Mai Monthly Hoppinings"
        const val DEFAULT_DATABASE = "mai_monthly_hoppinings.db"
        const val NAME_MAX_LENGTH = 40

        fun default(createdAtMillis: Long = System.currentTimeMillis()): Book {
            return Book(
                id = DEFAULT_ID,
                name = DEFAULT_NAME,
                databaseName = DEFAULT_DATABASE,
                createdAtMillis = createdAtMillis,
            )
        }

        fun sanitizeName(raw: String, fallback: String = "Untitled book"): String {
            return raw.trim().take(NAME_MAX_LENGTH).ifEmpty { fallback }
        }
    }
}

data class BookCatalog(
    val books: List<Book>,
    val activeId: String,
) {
    val active: Book
        get() = books.firstOrNull { it.id == activeId } ?: books.firstOrNull() ?: Book.default()

    fun book(id: String): Book? = books.firstOrNull { it.id == id }
}
