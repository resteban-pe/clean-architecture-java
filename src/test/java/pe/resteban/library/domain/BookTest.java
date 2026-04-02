package pe.resteban.library.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.resteban.library.domain.exception.DomainException;
import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.BookId;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Book")
class BookTest {

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book(BookId.generate(), "Clean Architecture", "Robert C. Martin", "978-0134494166");
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("new book is available by default")
    void newBook_isAvailable() {
        assertTrue(book.isAvailable());
    }

    // ── checkOut ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("checkOut() on available book sets available to false")
    void checkOut_availableBook_setsUnavailable() {
        book.checkOut();

        assertFalse(book.isAvailable());
    }

    @Test
    @DisplayName("checkOut() on unavailable book throws DomainException")
    void checkOut_unavailableBook_throwsDomainException() {
        book.checkOut(); // first checkout

        assertThrows(DomainException.class, book::checkOut);
    }

    // ── returnBook ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("returnBook() on checked-out book sets available to true")
    void returnBook_checkedOutBook_setsAvailable() {
        book.checkOut();

        book.returnBook();

        assertTrue(book.isAvailable());
    }

    @Test
    @DisplayName("returnBook() on already-available book throws DomainException")
    void returnBook_alreadyAvailableBook_throwsDomainException() {
        assertThrows(DomainException.class, book::returnBook);
    }

    // ── Constructor guards ────────────────────────────────────────────────────

    @Test
    @DisplayName("constructor rejects null id")
    void constructor_nullId_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Book(null, "Title", "Author", "ISBN"));
    }

    @Test
    @DisplayName("constructor rejects null title")
    void constructor_nullTitle_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Book(BookId.generate(), null, "Author", "ISBN"));
    }

    // ── Equality ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("two books with the same id are equal")
    void equals_sameid_returnsTrue() {
        BookId id = BookId.generate();
        Book a = new Book(id, "Title A", "Author A", "ISBN-1");
        Book b = new Book(id, "Title B", "Author B", "ISBN-2");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("two books with different ids are not equal")
    void equals_differentId_returnsFalse() {
        Book other = new Book(BookId.generate(), "Clean Architecture", "Robert C. Martin", "978-0134494166");

        assertNotEquals(book, other);
    }
}
