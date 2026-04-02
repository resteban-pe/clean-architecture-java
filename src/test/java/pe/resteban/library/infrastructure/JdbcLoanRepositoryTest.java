package pe.resteban.library.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.model.Loan;
import pe.resteban.library.domain.model.LoanId;
import pe.resteban.library.domain.model.LoanStatus;
import pe.resteban.library.domain.model.MemberId;
import pe.resteban.library.infrastructure.persistence.JdbcLoanRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JdbcLoanRepository")
class JdbcLoanRepositoryTest extends JdbcTestBase {

    private JdbcLoanRepository repository;
    private LoanId   loanId;
    private BookId   bookId;
    private MemberId memberId;
    private Loan     loan;

    JdbcLoanRepositoryTest() {
        super("loandb");
    }

    @BeforeEach
    void setUp() throws Exception {
        try (var stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM loans");
        }
        repository = new JdbcLoanRepository(connection);
        loanId     = LoanId.generate();
        bookId     = BookId.generate();
        memberId   = MemberId.generate();
        loan       = new Loan(loanId, bookId, memberId, LocalDate.of(2026, 1, 1));
    }

    @Test
    @DisplayName("save() and findById() round-trip — ACTIVE loan")
    void save_findById_activeLoan() {
        repository.save(loan);

        Loan found = repository.findById(loanId).orElseThrow();
        assertEquals(LoanStatus.ACTIVE, found.getStatus());
        assertEquals(bookId,   found.getBookId());
        assertEquals(memberId, found.getMemberId());
        assertTrue(found.getReturnDate().isEmpty());
    }

    @Test
    @DisplayName("save() persists RETURNED status and returnDate")
    void save_returnedLoan_persistsStatusAndDate() {
        loan.close(LocalDate.of(2026, 1, 15));
        repository.save(loan);

        Loan found = repository.findById(loanId).orElseThrow();
        assertEquals(LoanStatus.RETURNED, found.getStatus());
        assertTrue(found.getReturnDate().isPresent());
        assertEquals(LocalDate.of(2026, 1, 15), found.getReturnDate().get());
    }

    @Test
    @DisplayName("save() with same id updates existing row")
    void save_sameId_updatesRow() {
        repository.save(loan);
        loan.close(LocalDate.now());
        repository.save(loan);

        assertEquals(LoanStatus.RETURNED,
                repository.findById(loanId).orElseThrow().getStatus());
    }

    @Test
    @DisplayName("findById() returns empty for unknown id")
    void findById_unknown_returnsEmpty() {
        assertTrue(repository.findById(LoanId.generate()).isEmpty());
    }

    @Test
    @DisplayName("findByMemberId() returns loans for the given member")
    void findByMemberId_returnsMatchingLoans() {
        MemberId other = MemberId.generate();
        Loan otherLoan = new Loan(LoanId.generate(), BookId.generate(), other, LocalDate.now());
        repository.save(loan);
        repository.save(otherLoan);

        List<Loan> result = repository.findByMemberId(memberId);
        assertEquals(1, result.size());
        assertEquals(loanId, result.get(0).getId());
    }

    @Test
    @DisplayName("findByMemberId() returns empty list when no loans")
    void findByMemberId_noLoans_empty() {
        assertTrue(repository.findByMemberId(memberId).isEmpty());
    }

    @Test
    @DisplayName("findAll() returns all saved loans")
    void findAll_returnsAllLoans() {
        repository.save(loan);
        repository.save(new Loan(LoanId.generate(), BookId.generate(), MemberId.generate(), LocalDate.now()));

        assertEquals(2, repository.findAll().size());
    }
}
