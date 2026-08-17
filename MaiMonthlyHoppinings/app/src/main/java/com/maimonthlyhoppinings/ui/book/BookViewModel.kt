package com.maimonthlyhoppinings.ui.book

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.maimonthlyhoppinings.data.Book
import com.maimonthlyhoppinings.data.BookManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BooksUiState(
    val books: List<Book> = listOf(Book.default()),
    val active: Book = Book.default(),
) {
    val canDelete: Boolean
        get() = books.size > 1
}

class BookViewModel(
    private val bookManager: BookManager,
) : ViewModel() {
    val uiState: StateFlow<BooksUiState> = combine(
        bookManager.books,
        bookManager.activeBook,
    ) { books, active ->
        BooksUiState(books = books, active = active)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BooksUiState(
            books = bookManager.books.value,
            active = bookManager.activeBook.value,
        ),
    )

    fun switchTo(id: String) {
        viewModelScope.launch {
            bookManager.switchTo(id)
        }
    }

    fun create(name: String) {
        viewModelScope.launch {
            bookManager.create(name)
        }
    }

    fun rename(id: String, name: String) {
        viewModelScope.launch {
            bookManager.rename(id, name)
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            bookManager.delete(id)
        }
    }

    companion object {
        fun factory(bookManager: BookManager): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BookViewModel(bookManager) as T
                }
            }
        }
    }
}
