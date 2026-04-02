package pe.resteban.library.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.model.Loan;
import pe.resteban.library.domain.model.LoanId;
import pe.resteban.library.domain.model.LoanStatus;
import pe.resteban.library.domain.model.MemberId;
import pe.resteban.library.infrastructure.persistence.InMemoryLoanRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryLoanRepository")
class InMemoryLoanRepositoryTest {

    private InMemoryLoanRepository repository;
    private MemberId memberId;
    private BookId   bookId;
    private Loan     loan;
    private LoanId   loanId;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLoanRepository();
        memberId   = MemberId.generate();
        bookId     = BookId.generate();
        loanId     = LoanId.generate();
        loan       = new Loan(loanId, bookId, memberId, LocalDate.now());
    }

    // ── save ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save() persists the loan and returns it")
    void save_persistsAndReturnsLoan() {
        Loan saved = repository.save(loan);

        assertSame(loan, saved);
    }

    @Test
    @DisplayName("save() with same id overwrites previous entry (e.g. after close)")
    void save_sameId_overwritesPreviousEntry() {
        repository.save(loan);
        loan.close(LocalDate.now());
        repository.save(loan);

        Loan found = repository.findById(loanId).orElseThrow();
        assertEquals(LoanStatus.RETURNED, found.getStatus());
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById() returns present Optional for existing loan")
    void findById_existingLoan_returnsPresent() {
        repository.save(loan);

        Optional<Loan> result = repository.findById(loanId);

        assertTrue(result.isPresent());
        assertEquals(loan, result.get());
    }

    @Test
    @DisplayName("findById() returns empty Optional for non-existing loan")
    void findById_nonExistingLoan_returnsEmpty() {
        Optional<Loan> result = repository.findById(LoanId.generate());

        assertTrue(result.isEmpty());
    }

    // ── findByMemberId ────────────────────────────────────────────────────────

    @Test
    @DisplayName("findByMemberId() returns only loans for the given member")
    void findByMemberId_returnsLoansForMember() {
        MemberId otherMemberId = MemberId.generate();
        Loan otherLoan = new Loan(LoanId.generate(), BookId.generate(), otherMemberId, LocalDate.now());
        repository.save(loan);
        repository.save(otherLoan);

        List<Loan> result = repository.findByMemberId(memberId);

        assertEquals(1, result.size());
        assertEquals(loan, result.get(0));
    }

    @Test
    @DisplayName("findByMemberId() returns empty list when member has no loans")
    void findByMemberId_noLoans_returnsEmpty() {
        List<Loan> result = repository.findByMemberId(memberId);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByMemberId() returns multiple loans for same member")
    void findByMemberId_multipleLoans_returnsAll() {
        Loan loan2 = new Loan(LoanId.generate(), BookId.generate(), memberId, LocalDate.now());
        repository.save(loan);
        repository.save(loan2);

        List<Loan> result = repository.findByMemberId(memberId);

        assertEquals(2, result.size());
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll() returns empty list when no loans saved")
    void findAll_empty_returnsEmptyList() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("findAll() returns all saved loans")
    void findAll_withLoans_returnsAllLoans() {
        Loan loan2 = new Loan(LoanId.generate(), BookId.generate(), MemberId.generate(), LocalDate.now());
        repository.save(loan);
        repository.save(loan2);

        assertEquals(2, repository.findAll().size());
    }

    @Test
    @DisplayName("findAll() returns a defensive copy — mutation does not affect store")
    void findAll_returnsDefensiveCopy() {
        repository.save(loan);
        repository.findAll().clear();

        assertEquals(1, repository.findAll().size());
    }
}
