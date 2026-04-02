package pe.resteban.library.domain.exception;

/**
 * Base unchecked exception for all domain rule violations.
 * Callers are not forced to catch it; the application layer
 * decides how to handle or surface it.
 */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
