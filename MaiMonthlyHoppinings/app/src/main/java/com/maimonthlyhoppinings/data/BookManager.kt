package com.maimonthlyhoppinings.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class BookManager(
    private val context: Context,
    private val store: BookPreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val mutex = Mutex()
    private val session = MutableStateFlow<OpenBook?>(null)

    val catalog: StateFlow<BookCatalog> = store.catalog.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = BookCatalog(books = listOf(Book.default()), activeId = Book.DEFAULT_ID),
    )

    val books: StateFlow<List<Book>> = catalog
        .map { it.books }
        .stateIn(scope, SharingStarted.Eagerly, catalog.value.books)

    val activeBook: StateFlow<Book> = catalog
        .map { it.active }
        .stateIn(scope, SharingStarted.Eagerly, catalog.value.active)

    val database: AppDatabase
        get() = session.value?.database ?: error("Books have not been started")

    val databaseFlow = session
        .map { it?.database }
        .filterNotNull()
        .distinctUntilChanged()

    suspend fun start() {
        mutex.withLock {
            store.ensureDefaultBook()
            openLocked(store.snapshot().active)
        }
    }

    suspend fun switchTo(id: String) {
        mutex.withLock {
            val book = store.snapshot().book(id) ?: return
            if (session.value?.book?.id == id) return
            openLocked(book)
            store.setActive(id)
        }
    }

    suspend fun create(name: String): Book {
        return mutex.withLock {
            val catalog = store.snapshot()
            val resolvedName = Book.sanitizeName(
                name,
                fallback = nextUntitledName(catalog.books),
            )
            val id = UUID.randomUUID().toString()
            val book = Book(
                id = id,
                name = resolvedName,
                databaseName = "mai_book_${id.replace("-", "")}.db",
                createdAtMillis = System.currentTimeMillis(),
            )
            store.upsert(book)
            openLocked(book)
            store.setActive(book.id)
            book
        }
    }

    suspend fun rename(id: String, name: String) {
        mutex.withLock {
            val existing = store.snapshot().book(id) ?: return
            val resolved = Book.sanitizeName(name, fallback = existing.name)
            store.upsert(existing.copy(name = resolved))
        }
    }

    suspend fun delete(id: String) {
        mutex.withLock {
            val catalog = store.snapshot()
            if (catalog.books.size <= 1) return
            val doomed = catalog.book(id) ?: return
            val remaining = catalog.books.filterNot { it.id == id }
            val nextActive = if (catalog.activeId == id) remaining.first() else catalog.active
            if (session.value?.book?.id == id) {
                openLocked(nextActive)
            }
            store.remove(id)
            store.setActive(nextActive.id)
            AppDatabase.release(doomed.databaseName)
            context.deleteDatabase(doomed.databaseName)
        }
    }

    private fun openLocked(book: Book) {
        val previous = session.value
        val database = AppDatabase.open(context, book.databaseName)
        session.value = OpenBook(book, database)
        if (previous != null && previous.book.databaseName != book.databaseName) {
            AppDatabase.release(previous.book.databaseName)
        }
    }

    private fun nextUntitledName(existing: List<Book>): String {
        val used = existing.map { it.name }.toSet()
        var index = existing.size + 1
        while (used.contains("Book $index")) {
            index += 1
        }
        return "Book $index"
    }

    private data class OpenBook(
        val book: Book,
        val database: AppDatabase,
    )
}
