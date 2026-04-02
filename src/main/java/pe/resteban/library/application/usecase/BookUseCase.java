package pe.resteban.library.application.usecase;

import pe.resteban.library.domain.model.Book;

import java.util.List;

/**
 * Input port — defines all book-related operations available to the outside world.
 *
 * <p>String parameters for IDs allow callers (CLI, HTTP adapters, etc.) to pass raw
 * identifiers; the service is responsible for converting them to typed Value Objects.
 */
public interface BookUseCase {

    /** Creates and persists a new book. */
    Book createBook(String title, String author, String isbn);

    /** Returns the book with the given id or throws {@code BookNotFoundException}. */
    Book findBookById(String id);

    /** Returns all books in the catalogue. */
    List<Book> findAllBooks();

    /**
     * Marks the book as checked-out (unavailable).
     *
     * @throws pe.resteban.library.domain.exception.BookNotFoundException if not found.
     * @throws pe.resteban.library.domain.exception.DomainException       if already checked out.
     */
    Book checkOutBook(String bookId);

    /**
     * Marks the book as returned (available again).
     *
     * @throws pe.resteban.library.domain.exception.BookNotFoundException if not found.
     * @throws pe.resteban.library.domain.exception.DomainException       if not checked out.
     */
    Book returnBook(String bookId);

    /**
     * Removes a book from the catalogue.
     *
     * @throws pe.resteban.library.domain.exception.BookNotFoundException if not found.
     */
    void deleteBook(String bookId);
}
