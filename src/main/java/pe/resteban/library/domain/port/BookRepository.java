package pe.resteban.library.domain.port;

import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.BookId;

import java.util.List;
import java.util.Optional;

/**
 * Output port — secondary port in Clean Architecture.
 *
 * <p>Defines the persistence contract for {@link Book} aggregates.
 * Infrastructure adapters (in-memory, JPA, etc.) must implement this interface.
 * The domain layer owns this interface; it knows nothing about the implementation.
 */
public interface BookRepository {

    Optional<Book> findById(BookId id);

    List<Book> findAll();

    Book save(Book book);

    void deleteById(BookId id);
}
