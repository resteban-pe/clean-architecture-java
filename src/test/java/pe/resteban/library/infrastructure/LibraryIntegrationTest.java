package pe.resteban.library.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.resteban.library.application.service.BookService;
import pe.resteban.library.application.service.LoanService;
import pe.resteban.library.application.service.MemberService;
import pe.resteban.library.domain.exception.DomainException;
import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.Loan;
import pe.resteban.library.domain.model.LoanStatus;
import pe.resteban.library.domain.model.Member;
import pe.resteban.library.infrastructure.persistence.JdbcBookRepository;
import pe.resteban.library.infrastructure.persistence.JdbcLoanRepository;
import pe.resteban.library.infrastructure.persistence.JdbcMemberRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end integration test — no mocks.
 *
 * <p>Wires real JDBC repositories (H2 in-memory) directly to real services and
 * exercises the complete loan lifecycle.  This verifies that all three layers
 * (domain, application, infrastructure) work correctly together.
 */
@DisplayName("Library end-to-end integration (JDBC, no mocks)")
class LibraryIntegrationTest extends JdbcTestBase {

    private BookService   bookService;
    private MemberService memberService;
    private LoanService   loanService;

    LibraryIntegrationTest() {
        super("integrationdb");
    }

    @BeforeEach
    void setUp() throws Exception {
        // Clean all tables before every test for isolation
        try (var stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM loans");
            stmt.execute("DELETE FROM member_active_loans");
            stmt.execute("DELETE FROM members");
            stmt.execute("DELETE FROM books");
        }

        JdbcBookRepository   bookRepo   = new JdbcBookRepository(connection);
        JdbcMemberRepository memberRepo = new JdbcMemberRepository(connection);
        JdbcLoanRepository   loanRepo   = new JdbcLoanRepository(connection);

        bookService   = new BookService(bookRepo);
        memberService = new MemberService(memberRepo);
        loanService   = new LoanService(loanRepo, bookRepo, memberRepo);
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("full loan lifecycle: create → loan out → return")
    void fullLoanLifecycle() {
        // Arrange
        Book   book   = bookService.createBook("Clean Architecture", "R. Martin", "978-1");
        Member member = memberService.registerMember("Roosevelt Torres", "rt@resteban.pe");

        assertTrue(book.isAvailable(), "book must start available");
        assertEquals(0, member.getActiveLoans().size(), "member must start with no active loans");

        // Act — create loan
        Loan loan = loanService.createLoan(
                book.getId().toString(),
                member.getId().toString());

        // Assert post-loan state (reload from DB)
        Book loanedBook = bookService.findBookById(book.getId().toString());
        assertFalse(loanedBook.isAvailable(), "book must be unavailable after loan");

        Member loanedMember = memberService.findMemberById(member.getId().toString());
        assertEquals(1, loanedMember.getActiveLoans().size(), "member must have 1 active loan");
        assertTrue(loanedMember.getActiveLoans().contains(book.getId()));

        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        assertTrue(loan.getReturnDate().isEmpty());

        // Act — return loan
        Loan returned = loanService.returnLoan(loan.getId().toString());

        // Assert post-return state (reload from DB)
        assertEquals(LoanStatus.RETURNED, returned.getStatus());
        assertTrue(returned.getReturnDate().isPresent(), "return date must be set");

        Book returnedBook = bookService.findBookById(book.getId().toString());
        assertTrue(returnedBook.isAvailable(), "book must be available again after return");

        Member returnedMember = memberService.findMemberById(member.getId().toString());
        assertEquals(0, returnedMember.getActiveLoans().size(), "member must have 0 active loans after return");
    }

    @Test
    @DisplayName("multiple books and members — only correct associations are persisted")
    void multipleEntities_correctAssociations() {
        Book b1 = bookService.createBook("Book One", "Author A", "ISBN-1");
        Book b2 = bookService.createBook("Book Two", "Author B", "ISBN-2");
        Member m1 = memberService.registerMember("Alice", "alice@test.com");
        Member m2 = memberService.registerMember("Bob", "bob@test.com");

        loanService.createLoan(b1.getId().toString(), m1.getId().toString());
        loanService.createLoan(b2.getId().toString(), m2.getId().toString());

        List<Loan> all = loanService.findAllLoans();
        assertEquals(2, all.size());

        // b1 belongs to m1
        assertFalse(bookService.findBookById(b1.getId().toString()).isAvailable());
        assertEquals(1, memberService.findMemberById(m1.getId().toString()).getActiveLoans().size());

        // b2 belongs to m2
        assertFalse(bookService.findBookById(b2.getId().toString()).isAvailable());
        assertEquals(1, memberService.findMemberById(m2.getId().toString()).getActiveLoans().size());
    }

    // ── Domain rule enforcement ───────────────────────────────────────────────

    @Test
    @DisplayName("loaning an unavailable book throws DomainException")
    void loanUnavailableBook_throwsDomainException() {
        Book   book   = bookService.createBook("DDD", "Evans", "978-2");
        Member m1     = memberService.registerMember("Alice", "alice@test.com");
        Member m2     = memberService.registerMember("Bob",   "bob@test.com");

        loanService.createLoan(book.getId().toString(), m1.getId().toString());

        assertThrows(DomainException.class, () ->
                loanService.createLoan(book.getId().toString(), m2.getId().toString()));
    }

    @Test
    @DisplayName("member with 3 active loans cannot borrow a 4th — throws DomainException")
    void memberAtLoanLimit_throwsDomainException() {
        Member member = memberService.registerMember("Max Borrower", "max@test.com");

        for (int i = 0; i < 3; i++) {
            Book b = bookService.createBook("Book " + i, "Author", "ISBN-" + i);
            loanService.createLoan(b.getId().toString(), member.getId().toString());
        }

        Book extra = bookService.createBook("Extra Book", "Author", "ISBN-extra");
        assertThrows(DomainException.class, () ->
                loanService.createLoan(extra.getId().toString(), member.getId().toString()));
    }

    @Test
    @DisplayName("returning an already-returned loan throws DomainException")
    void returnAlreadyReturnedLoan_throwsDomainException() {
        Book   book   = bookService.createBook("Refactoring", "Fowler", "978-3");
        Member member = memberService.registerMember("Charlie", "charlie@test.com");

        Loan loan = loanService.createLoan(
                book.getId().toString(), member.getId().toString());
        loanService.returnLoan(loan.getId().toString());

        assertThrows(DomainException.class, () ->
                loanService.returnLoan(loan.getId().toString()));
    }

    // ── Delete book ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteBook() removes book from catalogue")
    void deleteBook_removesFromCatalogue() {
        Book book = bookService.createBook("Temp Book", "Author", "ISBN-del");
        assertEquals(1, bookService.findAllBooks().size());

        bookService.deleteBook(book.getId().toString());

        assertTrue(bookService.findAllBooks().isEmpty());
    }
}
