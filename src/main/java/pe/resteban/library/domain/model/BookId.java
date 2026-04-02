package pe.resteban.library.domain.model;

import java.util.UUID;

/**
 * Value Object — identity of a Book.
 * Immutable by design; uses Java 21 record.
 */
public record BookId(UUID value) {

    public BookId {
        if (value == null) throw new IllegalArgumentException("BookId value must not be null");
    }

    public static BookId generate() {
        return new BookId(UUID.randomUUID());
    }

    public static BookId of(String uuid) {
        return new BookId(UUID.fromString(uuid));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
