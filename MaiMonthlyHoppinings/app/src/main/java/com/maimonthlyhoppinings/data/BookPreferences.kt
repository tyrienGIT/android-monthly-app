package com.maimonthlyhoppinings.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.bookDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "book_preferences",
)

class BookPreferences(
    private val context: Context,
) {
    private val booksJsonKey = stringPreferencesKey("books_json")
    private val activeIdKey = stringPreferencesKey("active_book_id")

    val catalog: Flow<BookCatalog> = context.bookDataStore.data.map { prefs ->
        parse(prefs[booksJsonKey], prefs[activeIdKey])
    }

    suspend fun snapshot(): BookCatalog = catalog.first()

    suspend fun ensureDefaultBook() {
        context.bookDataStore.edit { prefs ->
            val current = parse(prefs[booksJsonKey], prefs[activeIdKey])
            if (current.books.isEmpty()) {
                val default = Book.default()
                prefs[booksJsonKey] = encode(listOf(default))
                prefs[activeIdKey] = default.id
                return@edit
            }
            if (current.books.none { it.id == current.activeId }) {
                prefs[activeIdKey] = current.books.first().id
            }
        }
    }

    suspend fun setActive(id: String) {
        context.bookDataStore.edit { prefs ->
            val current = parse(prefs[booksJsonKey], prefs[activeIdKey])
            if (current.book(id) != null) {
                prefs[activeIdKey] = id
            }
        }
    }

    suspend fun upsert(book: Book) {
        context.bookDataStore.edit { prefs ->
            val current = parse(prefs[booksJsonKey], prefs[activeIdKey])
            val next = current.books.filterNot { it.id == book.id } + book
            prefs[booksJsonKey] = encode(next.sortedBy { it.createdAtMillis })
            if (current.books.none { it.id == current.activeId }) {
                prefs[activeIdKey] = book.id
            }
        }
    }

    suspend fun remove(id: String) {
        context.bookDataStore.edit { prefs ->
            val current = parse(prefs[booksJsonKey], prefs[activeIdKey])
            val next = current.books.filterNot { it.id == id }
            if (next.isEmpty()) return@edit
            prefs[booksJsonKey] = encode(next)
            if (current.activeId == id) {
                prefs[activeIdKey] = next.first().id
            }
        }
    }

    private fun parse(json: String?, activeId: String?): BookCatalog {
        val books = decode(json)
        val resolvedActive = when {
            activeId != null && books.any { it.id == activeId } -> activeId
            books.isNotEmpty() -> books.first().id
            else -> Book.DEFAULT_ID
        }
        return BookCatalog(books = books, activeId = resolvedActive)
    }

    private fun encode(books: List<Book>): String {
        val array = JSONArray()
        books.forEach { book ->
            array.put(
                JSONObject()
                    .put("id", book.id)
                    .put("name", book.name)
                    .put("databaseName", book.databaseName)
                    .put("createdAtMillis", book.createdAtMillis),
            )
        }
        return array.toString()
    }

    private fun decode(json: String?): List<Book> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(json)
            buildList {
                for (index in 0 until array.length()) {
                    val obj = array.getJSONObject(index)
                    add(
                        Book(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            databaseName = obj.getString("databaseName"),
                            createdAtMillis = obj.optLong("createdAtMillis"),
                        ),
                    )
                }
            }
        }.getOrElse { emptyList() }
    }
}
