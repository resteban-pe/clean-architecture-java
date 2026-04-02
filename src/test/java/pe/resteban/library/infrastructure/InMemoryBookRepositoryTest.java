package pe.resteban.library.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.infrastructure.persistence.InMemoryBookRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryBookRepository")
class InMemoryBookRepositoryTest {

    private InMemoryBookRepository repository;
    private Book book;
    private BookId bookId;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBookRepository();
        bookId     = BookId.generate();
        book       = new Book(bookId, "Clean Architecture", "R. Martin", "978-1");
    }

    // ── save ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save() persists the book and returns it")
    void save_persistsAndReturnsBook() {
        Book saved = repository.save(book);

        assertSame(book, saved);
    }

    @Test
    @DisplayName("save() with same id overwrites previous entry")
    void save_sameId_overwritesPreviousEntry() {
        repository.save(book);
        Book updated = new Book(bookId, "Updated Title", "R. Martin", "978-1");
        repository.save(updated);

        Book found = repository.findById(bookId).orElseThrow();
        assertEquals("Updated Title", found.getTitle());
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById() returns present Optional for existing book")
    void findById_existingBook_returnsPresent() {
        repository.save(book);

        Optional<Book> result = repository.findById(bookId);

        assertTrue(result.isPresent());
        assertEquals(book, result.get());
    }

    @Test
    @DisplayName("findById() returns empty Optional for non-existing book")
    void findById_nonExistingBook_returnsEmpty() {
        Optional<Book> result = repository.findById(BookId.generate());

        assertTrue(result.isEmpty());
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll() returns empty list when no books saved")
    void findAll_empty_returnsEmptyList() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("findAll() returns all saved books")
    void findAll_withBooks_returnsAllBooks() {
        Book book2 = new Book(BookId.generate(), "DDD", "Evans", "978-2");
        repository.save(book);
        repository.save(book2);

        List<Book> result = repository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findAll() returns a defensive copy — mutation does not affect store")
    void findAll_returnsDefensiveCopy() {
        repository.save(book);
        List<Book> first  = repository.findAll();
        first.clear();

        assertEquals(1, repository.findAll().size());
    }

    // ── deleteById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteById() removes an existing book")
    void deleteById_existingBook_removesIt() {
        repository.save(book);

        repository.deleteById(bookId);

        assertTrue(repository.findById(bookId).isEmpty());
    }

    @Test
    @DisplayName("deleteById() on non-existing id is a no-op")
    void deleteById_nonExistingId_noOp() {
        repository.save(book);

        repository.deleteById(BookId.generate()); // different id

        assertEquals(1, repository.findAll().size());
    }
}
