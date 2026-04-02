package pe.resteban.library.infrastructure.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.resteban.library.application.usecase.BookUseCase;
import pe.resteban.library.application.usecase.LoanUseCase;
import pe.resteban.library.application.usecase.MemberUseCase;
import pe.resteban.library.domain.exception.DomainException;
import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.model.Loan;
import pe.resteban.library.domain.model.LoanId;
import pe.resteban.library.domain.model.Member;
import pe.resteban.library.domain.model.MemberId;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the interactive CLI adapter.
 *
 * <p>System.in is replaced with a {@link ByteArrayInputStream} before each
 * {@link LibraryCLI} is constructed so that the internal {@link java.util.Scanner}
 * reads from our scripted input.  System.out is silenced to keep test output clean.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LibraryCLI")
class LibraryCLITest {

    @Mock BookUseCase   bookUseCase;
    @Mock MemberUseCase memberUseCase;
    @Mock LoanUseCase   loanUseCase;

    private InputStream originalIn;
    private PrintStream originalOut;

    @BeforeEach
    void captureStreams() {
        originalIn  = System.in;
        originalOut = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
    }

    @AfterEach
    void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    /** Redirects System.in to the given script, then builds the CLI. */
    private LibraryCLI cli(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        return new LibraryCLI(bookUseCase, memberUseCase, loanUseCase);
    }

    // ── Exit ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("option 0 exits immediately")
    void option0_exitsImmediately() {
        cli("0\n").start();           // must not block
    }

    @Test
    @DisplayName("unknown option shows error, then exits on 0")
    void unknownOption_showsError() {
        cli("9\n0\n").start();
    }

    // ── Books ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("option 1 — empty catalogue")
    void option1_listBooks_empty() {
        when(bookUseCase.findAllBooks()).thenReturn(Collections.emptyList());
        cli("1\n0\n").start();
        verify(bookUseCase).findAllBooks();
    }

    @Test
    @DisplayName("option 1 — non-empty catalogue (covers truncate for long strings)")
    void option1_listBooks_nonEmpty() {
        Book book = new Book(BookId.generate(),
                "A Title That Is Definitely Longer Than Thirty Characters Here",
                "An Author Name That Is Way Over Twenty Characters",
                "978-0134494166");
        when(bookUseCase.findAllBooks()).thenReturn(List.of(book));
        cli("1\n0\n").start();
        verify(bookUseCase).findAllBooks();
    }

    @Test
    @DisplayName("option 2 — add book")
    void option2_addBook() {
        Book book = new Book(BookId.generate(), "Clean Architecture", "R. Martin", "978-1");
        when(bookUseCase.createBook("Clean Architecture", "R. Martin", "978-1")).thenReturn(book);
        cli("2\nClean Architecture\nR. Martin\n978-1\n0\n").start();
        verify(bookUseCase).createBook("Clean Architecture", "R. Martin", "978-1");
    }

    @Test
    @DisplayName("option 3 — delete book")
    void option3_deleteBook() {
        String id = BookId.generate().toString();
        doNothing().when(bookUseCase).deleteBook(id);
        cli("3\n" + id + "\n0\n").start();
        verify(bookUseCase).deleteBook(id);
    }

    // ── Members ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("option 4 — empty member list")
    void option4_listMembers_empty() {
        when(memberUseCase.findAllMembers()).thenReturn(Collections.emptyList());
        cli("4\n0\n").start();
        verify(memberUseCase).findAllMembers();
    }

    @Test
    @DisplayName("option 4 — non-empty member list (covers truncate for long name/email)")
    void option4_listMembers_nonEmpty() {
        Member member = new Member(MemberId.generate(),
                "A Name That Is Way Over Twenty Five Characters Long Indeed",
                "a.very.long.email.address.that.exceeds.thirty.chars@example.com");
        when(memberUseCase.findAllMembers()).thenReturn(List.of(member));
        cli("4\n0\n").start();
        verify(memberUseCase).findAllMembers();
    }

    @Test
    @DisplayName("option 5 — register member")
    void option5_registerMember() {
        Member member = new Member(MemberId.generate(), "Alice", "alice@test.com");
        when(memberUseCase.registerMember("Alice", "alice@test.com")).thenReturn(member);
        cli("5\nAlice\nalice@test.com\n0\n").start();
        verify(memberUseCase).registerMember("Alice", "alice@test.com");
    }

    // ── Loans ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("option 6 — empty loan list")
    void option6_listLoans_empty() {
        when(loanUseCase.findAllLoans()).thenReturn(Collections.emptyList());
        cli("6\n0\n").start();
        verify(loanUseCase).findAllLoans();
    }

    @Test
    @DisplayName("option 6 — non-empty loan list (ACTIVE, no return date)")
    void option6_listLoans_nonEmpty_active() {
        Loan loan = new Loan(LoanId.generate(), BookId.generate(), MemberId.generate(),
                LocalDate.of(2026, 1, 1));
        when(loanUseCase.findAllLoans()).thenReturn(List.of(loan));
        cli("6\n0\n").start();
        verify(loanUseCase).findAllLoans();
    }

    @Test
    @DisplayName("option 6 — non-empty loan list (RETURNED, with return date)")
    void option6_listLoans_nonEmpty_returned() {
        Loan loan = new Loan(LoanId.generate(), BookId.generate(), MemberId.generate(),
                LocalDate.of(2026, 1, 1));
        loan.close(LocalDate.of(2026, 1, 15));
        when(loanUseCase.findAllLoans()).thenReturn(List.of(loan));
        cli("6\n0\n").start();
        verify(loanUseCase).findAllLoans();
    }

    @Test
    @DisplayName("option 7 — create loan")
    void option7_createLoan() {
        String bookId   = BookId.generate().toString();
        String memberId = MemberId.generate().toString();
        Loan loan = new Loan(LoanId.generate(), BookId.of(bookId), MemberId.of(memberId),
                LocalDate.of(2026, 1, 1));
        when(loanUseCase.createLoan(bookId, memberId)).thenReturn(loan);
        cli("7\n" + bookId + "\n" + memberId + "\n0\n").start();
        verify(loanUseCase).createLoan(bookId, memberId);
    }

    @Test
    @DisplayName("option 8 — return loan")
    void option8_returnLoan() {
        String loanId = LoanId.generate().toString();
        Loan loan = new Loan(LoanId.of(loanId), BookId.generate(), MemberId.generate(),
                LocalDate.of(2026, 1, 1));
        loan.close(LocalDate.of(2026, 1, 15));
        when(loanUseCase.returnLoan(loanId)).thenReturn(loan);
        cli("8\n" + loanId + "\n0\n").start();
        verify(loanUseCase).returnLoan(loanId);
    }

    // ── Exception handling ────────────────────────────────────────────────────

    @Test
    @DisplayName("DomainException is caught and printed, loop continues")
    void domainException_isCaughtAndLoopContinues() {
        when(bookUseCase.findAllBooks()).thenThrow(new DomainException("book unavailable"));
        cli("1\n0\n").start();
        verify(bookUseCase).findAllBooks();
    }

    @Test
    @DisplayName("IllegalArgumentException is caught and printed, loop continues")
    void illegalArgumentException_isCaughtAndLoopContinues() {
        when(bookUseCase.createBook(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("bad isbn"));
        cli("2\nTitle\nAuthor\nbad\n0\n").start();
        verify(bookUseCase).createBook("Title", "Author", "bad");
    }
}
