package pe.resteban.library.infrastructure.persistence;

import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.port.BookRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Infrastructure adapter — in-memory implementation of {@link BookRepository}.
 *
 * <p>Backed by a {@link HashMap} keyed on the string representation of {@link BookId}.
 * Thread-safety is intentionally out of scope for this in-memory adapter.
 * This class contains zero framework annotations; wiring is done manually in the
 * application entry point.
 */
public class InMemoryBookRepository implements BookRepository {

    private final Map<String, Book> store = new HashMap<>();

    @Override
    public Optional<Book> findById(BookId id) {
        return Optional.ofNullable(store.get(id.toString()));
    }

    @Override
    public List<Book> findAll() {
        return new ArrayList<>(store.values());
    }

    /**
     * Persists (insert or update) the given book.
     *
     * <p>The book is stored under its string id; calling {@code save} with an
     * existing id overwrites the previous entry, which is the expected behaviour
     * after mutations like {@code checkOut()} or {@code returnBook()}.
     */
    @Override
    public Book save(Book book) {
        store.put(book.getId().toString(), book);
        return book;
    }

    @Override
    public void deleteById(BookId id) {
        store.remove(id.toString());
    }
}
