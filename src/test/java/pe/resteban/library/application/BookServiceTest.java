package pe.resteban.library.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.resteban.library.application.service.BookService;
import pe.resteban.library.domain.exception.BookNotFoundException;
import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.port.BookRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService")
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository);
    }

    // ── createBook ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createBook() calls save() and returns book with correct data")
    void createBook_callsSaveAndReturnsBook() {
        Book saved = new Book(BookId.generate(), "Clean Architecture", "R. Martin", "978-1");
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        Book result = bookService.createBook("Clean Architecture", "R. Martin", "978-1");

        verify(bookRepository, times(1)).save(any(Book.class));
        assertEquals("Clean Architecture", result.getTitle());
        assertEquals("R. Martin",          result.getAuthor());
        assertEquals("978-1",              result.getIsbn());
    }

    @Test
    @DisplayName("createBook() generates a fresh BookId (not null)")
    void createBook_generatesNonNullId() {
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService.createBook("Title", "Author", "ISBN");

        verify(bookRepository).save(captor.capture());
        assertNotNull(captor.getValue().getId());
    }

    // ── findBookById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findBookById() with existing id returns the book")
    void findBookById_existingId_returnsBook() {
        BookId id   = BookId.generate();
        Book   book = new Book(id, "DDD", "Evans", "978-2");
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        Book result = bookService.findBookById(id.toString());

        assertEquals(book, result);
    }

    @Test
    @DisplayName("findBookById() with non-existing id throws BookNotFoundException")
    void findBookById_nonExistingId_throwsBookNotFoundException() {
        BookId id = BookId.generate();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> bookService.findBookById(id.toString()));
    }

    // ── findAllBooks ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAllBooks() delegates to repository and returns all books")
    void findAllBooks_returnsAllBooks() {
        List<Book> books = List.of(
                new Book(BookId.generate(), "Book A", "Author A", "ISBN-A"),
                new Book(BookId.generate(), "Book B", "Author B", "ISBN-B"));
        when(bookRepository.findAll()).thenReturn(books);

        List<Book> result = bookService.findAllBooks();

        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findAll();
    }

    // ── checkOutBook ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("checkOutBook() makes the book unavailable and saves it")
    void checkOutBook_setsBookUnavailable() {
        BookId id   = BookId.generate();
        Book   book = new Book(id, "Title", "Author", "ISBN");
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.checkOutBook(id.toString());

        assertFalse(result.isAvailable());
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("checkOutBook() with non-existing id throws BookNotFoundException")
    void checkOutBook_nonExistingId_throwsBookNotFoundException() {
        BookId id = BookId.generate();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> bookService.checkOutBook(id.toString()));
    }

    // ── returnBook ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("returnBook() makes the book available again and saves it")
    void returnBook_setsBookAvailable() {
        BookId id   = BookId.generate();
        Book   book = new Book(id, "Title", "Author", "ISBN");
        book.checkOut(); // pre-condition: book is checked out
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        Book result = bookService.returnBook(id.toString());

        assertTrue(result.isAvailable());
        verify(bookRepository).save(book);
    }

    @Test
    @DisplayName("returnBook() with non-existing id throws BookNotFoundException")
    void returnBook_nonExistingId_throwsBookNotFoundException() {
        BookId id = BookId.generate();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> bookService.returnBook(id.toString()));
    }

    // ── deleteBook ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteBook() calls deleteById after finding the book")
    void deleteBook_existingId_callsDeleteById() {
        BookId id   = BookId.generate();
        Book   book = new Book(id, "Title", "Author", "ISBN");
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));

        bookService.deleteBook(id.toString());

        verify(bookRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("deleteBook() with non-existing id throws BookNotFoundException")
    void deleteBook_nonExistingId_throwsBookNotFoundException() {
        BookId id = BookId.generate();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> bookService.deleteBook(id.toString()));
    }

    // ── Constructor guard ─────────────────────────────────────────────────────

    @Test
    @DisplayName("constructor rejects null repository")
    void constructor_nullRepository_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new BookService(null));
    }
}
