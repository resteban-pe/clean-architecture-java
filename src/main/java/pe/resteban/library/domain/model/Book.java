package pe.resteban.library.domain.model;

import pe.resteban.library.domain.exception.DomainException;

import java.util.Objects;

/**
 * Aggregate root — represents a physical book copy in the library.
 *
 * <p>Invariant: a book can only be checked out when {@code available == true}.
 */
public class Book {

    private final BookId id;
    private String title;
    private String author;
    private String isbn;
    private boolean available;

    public Book(BookId id, String title, String author, String isbn) {
        this.id        = Objects.requireNonNull(id,     "id must not be null");
        this.title     = Objects.requireNonNull(title,  "title must not be null");
        this.author    = Objects.requireNonNull(author, "author must not be null");
        this.isbn      = Objects.requireNonNull(isbn,   "isbn must not be null");
        this.available = true;
    }

    // ── Domain behaviour ─────────────────────────────────────────────────────

    /**
     * Marks the book as checked-out (unavailable).
     *
     * @throws DomainException if the book is already checked out.
     */
    public void checkOut() {
        if (!available) {
            throw new DomainException(
                "Book '" + title + "' (id=" + id + ") is not available for checkout");
        }
        this.available = false;
    }

    /**
     * Marks the book as returned (available again).
     *
     * @throws DomainException if the book was not checked out.
     */
    public void returnBook() {
        if (available) {
            throw new DomainException(
                "Book '" + title + "' (id=" + id + ") was not checked out");
        }
        this.available = true;
    }

    /** @return {@code true} if the book is on the shelf and can be borrowed. */
    public boolean isAvailable() {
        return available;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public BookId getId()     { return id; }
    public String getTitle()  { return title; }
    public String getAuthor() { return author; }
    public String getIsbn()   { return isbn; }

    // ── Equality by identity ──────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book book)) return false;
        return id.equals(book.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() {
        return "Book{id=" + id + ", title='" + title + "', isbn='" + isbn +
               "', available=" + available + '}';
    }
}
