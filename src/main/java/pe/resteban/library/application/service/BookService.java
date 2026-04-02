package pe.resteban.library.application.service;

import pe.resteban.library.application.usecase.BookUseCase;
import pe.resteban.library.domain.exception.BookNotFoundException;
import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.port.BookRepository;

import java.util.List;
import java.util.Objects;

/**
 * Application service — orchestrates book-related use cases.
 *
 * <p>Contains no framework annotations. Dependencies are injected exclusively
 * via constructor, making this class trivially testable without a container.
 */
public class BookService implements BookUseCase {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = Objects.requireNonNull(bookRepository,
                "bookRepository must not be null");
    }

    // ── BookUseCase ───────────────────────────────────────────────────────────

    @Override
    public Book createBook(String title, String author, String isbn) {
        Book book = new Book(BookId.generate(), title, author, isbn);
        return bookRepository.save(book);
    }

    @Override
    public Book findBookById(String id) {
        BookId bookId = BookId.of(id);
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
    }

    @Override
    public List<Book> findAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book checkOutBook(String id) {
        BookId bookId = BookId.of(id);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        book.checkOut();
        return bookRepository.save(book);
    }

    @Override
    public Book returnBook(String id) {
        BookId bookId = BookId.of(id);
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        book.returnBook();
        return bookRepository.save(book);
    }

    @Override
    public void deleteBook(String id) {
        BookId bookId = BookId.of(id);
        bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        bookRepository.deleteById(bookId);
    }
}
