package pe.resteban.library.domain.exception;

import pe.resteban.library.domain.model.BookId;

public class BookNotFoundException extends DomainException {

    public BookNotFoundException(BookId id) {
        super("Book not found with id: " + id);
    }

    public BookNotFoundException(String message) {
        super(message);
    }
}
