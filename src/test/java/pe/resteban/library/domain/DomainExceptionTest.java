package pe.resteban.library.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.resteban.library.domain.exception.BookNotFoundException;
import pe.resteban.library.domain.exception.DomainException;
import pe.resteban.library.domain.exception.LoanNotFoundException;
import pe.resteban.library.domain.exception.MemberNotFoundException;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.model.LoanId;
import pe.resteban.library.domain.model.MemberId;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Domain Exceptions")
class DomainExceptionTest {

    // ── DomainException ───────────────────────────────────────────────────────

    @Test
    @DisplayName("DomainException(String, Throwable) preserves message and cause")
    void domainException_messageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        DomainException ex = new DomainException("wrapped", cause);

        assertEquals("wrapped",    ex.getMessage());
        assertSame(cause,          ex.getCause());
    }

    @Test
    @DisplayName("DomainException is unchecked (extends RuntimeException)")
    void domainException_isUnchecked() {
        assertInstanceOf(RuntimeException.class, new DomainException("msg"));
    }

    // ── BookNotFoundException ─────────────────────────────────────────────────

    @Test
    @DisplayName("BookNotFoundException(BookId) includes id in message")
    void bookNotFoundException_byId_messageContainsId() {
        BookId id = BookId.generate();
        BookNotFoundException ex = new BookNotFoundException(id);

        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    @DisplayName("BookNotFoundException(String) preserves custom message")
    void bookNotFoundException_byString_preservesMessage() {
        BookNotFoundException ex = new BookNotFoundException("custom book message");

        assertEquals("custom book message", ex.getMessage());
    }

    @Test
    @DisplayName("BookNotFoundException extends DomainException")
    void bookNotFoundException_extendsDomainException() {
        assertInstanceOf(DomainException.class, new BookNotFoundException("msg"));
    }

    // ── MemberNotFoundException ───────────────────────────────────────────────

    @Test
    @DisplayName("MemberNotFoundException(MemberId) includes id in message")
    void memberNotFoundException_byId_messageContainsId() {
        MemberId id = MemberId.generate();
        MemberNotFoundException ex = new MemberNotFoundException(id);

        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    @DisplayName("MemberNotFoundException(String) preserves custom message")
    void memberNotFoundException_byString_preservesMessage() {
        MemberNotFoundException ex = new MemberNotFoundException("custom member message");

        assertEquals("custom member message", ex.getMessage());
    }

    @Test
    @DisplayName("MemberNotFoundException extends DomainException")
    void memberNotFoundException_extendsDomainException() {
        assertInstanceOf(DomainException.class, new MemberNotFoundException("msg"));
    }

    // ── LoanNotFoundException ─────────────────────────────────────────────────

    @Test
    @DisplayName("LoanNotFoundException(LoanId) includes id in message")
    void loanNotFoundException_byId_messageContainsId() {
        LoanId id = LoanId.generate();
        LoanNotFoundException ex = new LoanNotFoundException(id);

        assertTrue(ex.getMessage().contains(id.toString()));
    }

    @Test
    @DisplayName("LoanNotFoundException(String) preserves custom message")
    void loanNotFoundException_byString_preservesMessage() {
        LoanNotFoundException ex = new LoanNotFoundException("custom loan message");

        assertEquals("custom loan message", ex.getMessage());
    }

    @Test
    @DisplayName("LoanNotFoundException extends DomainException")
    void loanNotFoundException_extendsDomainException() {
        assertInstanceOf(DomainException.class, new LoanNotFoundException("msg"));
    }
}
