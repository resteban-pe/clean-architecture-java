package pe.resteban.library.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.infrastructure.persistence.JdbcBookRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JdbcBookRepository")
class JdbcBookRepositoryTest extends JdbcTestBase {

    private JdbcBookRepository repository;
    private BookId bookId;
    private Book book;

    JdbcBookRepositoryTest() {
        super("bookdb");
    }

    @BeforeEach
    void setUp() throws Exception {
        try (var stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM books");
        }
        repository = new JdbcBookRepository(connection);
        bookId     = BookId.generate();
        book       = new Book(bookId, "Clean Architecture", "R. Martin", "978-1");
    }

    @Test
    @DisplayName("save() and findById() round-trip")
    void save_findById_roundTrip() {
        repository.save(book);

        Optional<Book> result = repository.findById(bookId);

        assertTrue(result.isPresent());
        assertEquals("Clean Architecture", result.get().getTitle());
        assertTrue(result.get().isAvailable());
    }

    @Test
    @DisplayName("save() persists checked-out state")
    void save_checkedOutBook_persistsUnavailable() {
        book.checkOut();
        repository.save(book);

        Book found = repository.findById(bookId).orElseThrow();
        assertFalse(found.isAvailable());
    }

    @Test
    @DisplayName("save() with same id updates existing row")
    void save_sameId_updatesRow() {
        repository.save(book);
        book.checkOut();
        repository.save(book);

        assertFalse(repository.findById(bookId).orElseThrow().isAvailable());
    }

    @Test
    @DisplayName("findById() returns empty for unknown id")
    void findById_unknown_returnsEmpty() {
        assertTrue(repository.findById(BookId.generate()).isEmpty());
    }

    @Test
    @DisplayName("findAll() returns all saved books")
    void findAll_returnsAllBooks() {
        repository.save(book);
        repository.save(new Book(BookId.generate(), "DDD", "Evans", "978-2"));

        List<Book> all = repository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("deleteById() removes the book")
    void deleteById_removesBook() {
        repository.save(book);
        repository.deleteById(bookId);

        assertTrue(repository.findById(bookId).isEmpty());
    }

    @Test
    @DisplayName("deleteById() on unknown id is a no-op")
    void deleteById_unknownId_noOp() {
        repository.save(book);
        repository.deleteById(BookId.generate());

        assertEquals(1, repository.findAll().size());
    }
}
