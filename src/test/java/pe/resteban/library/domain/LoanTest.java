package pe.resteban.library.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.resteban.library.domain.exception.DomainException;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.model.Loan;
import pe.resteban.library.domain.model.LoanId;
import pe.resteban.library.domain.model.LoanStatus;
import pe.resteban.library.domain.model.MemberId;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Loan")
class LoanTest {

    private static final LocalDate LOAN_DATE   = LocalDate.of(2026, 1, 1);
    private static final LocalDate RETURN_DATE = LocalDate.of(2026, 1, 15);

    private Loan loan;

    @BeforeEach
    void setUp() {
        loan = new Loan(LoanId.generate(), BookId.generate(), MemberId.generate(), LOAN_DATE);
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("new loan has ACTIVE status")
    void newLoan_statusIsActive() {
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
    }

    @Test
    @DisplayName("new loan has no returnDate")
    void newLoan_returnDateIsEmpty() {
        assertTrue(loan.getReturnDate().isEmpty());
    }

    @Test
    @DisplayName("isActive() returns true for new loan")
    void newLoan_isActive_returnsTrue() {
        assertTrue(loan.isActive());
    }

    // ── close ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("close() changes status to RETURNED")
    void close_activeLoan_setsStatusReturned() {
        loan.close(RETURN_DATE);

        assertEquals(LoanStatus.RETURNED, loan.getStatus());
    }

    @Test
    @DisplayName("close() sets the returnDate")
    void close_activeLoan_setsReturnDate() {
        loan.close(RETURN_DATE);

        assertTrue(loan.getReturnDate().isPresent());
        assertEquals(RETURN_DATE, loan.getReturnDate().get());
    }

    @Test
    @DisplayName("close() makes isActive() return false")
    void close_activeLoan_isActive_returnsFalse() {
        loan.close(RETURN_DATE);

        assertFalse(loan.isActive());
    }

    @Test
    @DisplayName("close() on already-returned loan throws DomainException")
    void close_alreadyReturnedLoan_throwsDomainException() {
        loan.close(RETURN_DATE);

        assertThrows(DomainException.class, () -> loan.close(RETURN_DATE.plusDays(1)));
    }

    @Test
    @DisplayName("close() rejects null returnDate")
    void close_nullReturnDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> loan.close(null));
    }

    // ── Constructor guards ────────────────────────────────────────────────────

    @Test
    @DisplayName("constructor rejects null loanDate")
    void constructor_nullLoanDate_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Loan(LoanId.generate(), BookId.generate(), MemberId.generate(), null));
    }

    // ── Equality ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("two loans with the same id are equal")
    void equals_sameId_returnsTrue() {
        LoanId  id = LoanId.generate();
        Loan a = new Loan(id, BookId.generate(), MemberId.generate(), LOAN_DATE);
        Loan b = new Loan(id, BookId.generate(), MemberId.generate(), LOAN_DATE);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
