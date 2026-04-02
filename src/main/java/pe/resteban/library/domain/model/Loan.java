package pe.resteban.library.domain.model;

import pe.resteban.library.domain.exception.DomainException;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Entity — represents a single borrowing transaction.
 *
 * <p>{@code returnDate} is absent while the loan is {@link LoanStatus#ACTIVE}.
 */
public class Loan {

    private final LoanId   id;
    private final BookId   bookId;
    private final MemberId memberId;
    private final LocalDate loanDate;
    private LocalDate       returnDate;
    private LoanStatus      status;

    public Loan(LoanId id, BookId bookId, MemberId memberId, LocalDate loanDate) {
        this.id       = Objects.requireNonNull(id,       "id must not be null");
        this.bookId   = Objects.requireNonNull(bookId,   "bookId must not be null");
        this.memberId = Objects.requireNonNull(memberId, "memberId must not be null");
        this.loanDate = Objects.requireNonNull(loanDate, "loanDate must not be null");
        this.status   = LoanStatus.ACTIVE;
    }

    // ── Domain behaviour ──────────────────────────────────────────────────────

    /**
     * Closes the loan by recording the return date.
     *
     * @throws DomainException if the loan is already returned.
     */
    public void close(LocalDate returnDate) {
        Objects.requireNonNull(returnDate, "returnDate must not be null");
        if (this.status == LoanStatus.RETURNED) {
            throw new DomainException("Loan id=" + id + " is already returned");
        }
        this.returnDate = returnDate;
        this.status     = LoanStatus.RETURNED;
    }

    public boolean isActive() {
        return status == LoanStatus.ACTIVE;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public LoanId   getId()         { return id; }
    public BookId   getBookId()     { return bookId; }
    public MemberId getMemberId()   { return memberId; }
    public LocalDate getLoanDate()  { return loanDate; }
    public LoanStatus getStatus()   { return status; }

    /** Present only after the loan has been closed. */
    public Optional<LocalDate> getReturnDate() {
        return Optional.ofNullable(returnDate);
    }

    // ── Equality by identity ──────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loan loan)) return false;
        return id.equals(loan.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() {
        return "Loan{id=" + id + ", bookId=" + bookId +
               ", memberId=" + memberId + ", status=" + status + '}';
    }
}
