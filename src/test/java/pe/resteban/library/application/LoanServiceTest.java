package pe.resteban.library.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.resteban.library.application.service.LoanService;
import pe.resteban.library.domain.exception.BookNotFoundException;
import pe.resteban.library.domain.exception.DomainException;
import pe.resteban.library.domain.exception.LoanNotFoundException;
import pe.resteban.library.domain.exception.MemberNotFoundException;
import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.model.Loan;
import pe.resteban.library.domain.model.LoanId;
import pe.resteban.library.domain.model.LoanStatus;
import pe.resteban.library.domain.model.Member;
import pe.resteban.library.domain.model.MemberId;
import pe.resteban.library.domain.port.BookRepository;
import pe.resteban.library.domain.port.LoanRepository;
import pe.resteban.library.domain.port.MemberRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoanService")
class LoanServiceTest {

    @Mock private LoanRepository   loanRepository;
    @Mock private BookRepository   bookRepository;
    @Mock private MemberRepository memberRepository;

    private LoanService loanService;

    // ── Fixtures ──────────────────────────────────────────────────────────────

    private BookId   bookId;
    private MemberId memberId;
    private Book     availableBook;
    private Member   member;

    @BeforeEach
    void setUp() {
        loanService = new LoanService(loanRepository, bookRepository, memberRepository);

        bookId        = BookId.generate();
        memberId      = MemberId.generate();
        availableBook = new Book(bookId, "Clean Architecture", "R. Martin", "978-1");
        member        = new Member(memberId, "Roosevelt Torres", "rt@resteban.pe");
    }

    // ── createLoan — happy path ───────────────────────────────────────────────

    @Test
    @DisplayName("createLoan() happy path: loan created, book checked out, member updated")
    void createLoan_happyPath_loanCreatedAndAggregatesUpdated() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(availableBook));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookRepository.save(availableBook)).thenReturn(availableBook);
        when(memberRepository.save(member)).thenReturn(member);

        Loan result = loanService.createLoan(bookId.toString(), memberId.toString());

        // Loan saved
        verify(loanRepository, times(1)).save(any(Loan.class));
        // Book checked out and saved
        assertFalse(availableBook.isAvailable());
        verify(bookRepository, times(1)).save(availableBook);
        // Member updated and saved
        assertTrue(member.getActiveLoans().contains(bookId));
        verify(memberRepository, times(1)).save(member);
        // Loan properties
        assertEquals(bookId,   result.getBookId());
        assertEquals(memberId, result.getMemberId());
        assertEquals(LoanStatus.ACTIVE, result.getStatus());
        assertNotNull(result.getId());
    }

    @Test
    @DisplayName("createLoan() sets loanDate to today")
    void createLoan_setsLoanDateToToday() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(availableBook));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(loanRepository.save(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookRepository.save(any())).thenReturn(availableBook);
        when(memberRepository.save(any())).thenReturn(member);

        Loan result = loanService.createLoan(bookId.toString(), memberId.toString());

        assertEquals(LocalDate.now(), result.getLoanDate());
    }

    // ── createLoan — book unavailable ─────────────────────────────────────────

    @Test
    @DisplayName("createLoan() with already checked-out book throws DomainException")
    void createLoan_bookNotAvailable_throwsDomainException() {
        availableBook.checkOut(); // make unavailable
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(availableBook));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        assertThrows(DomainException.class,
                () -> loanService.createLoan(bookId.toString(), memberId.toString()));

        // Loan must NOT be persisted
        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("createLoan() with non-existing book throws BookNotFoundException")
    void createLoan_bookNotFound_throwsBookNotFoundException() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
                () -> loanService.createLoan(bookId.toString(), memberId.toString()));

        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("createLoan() with non-existing member throws MemberNotFoundException")
    void createLoan_memberNotFound_throwsMemberNotFoundException() {
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(availableBook));
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class,
                () -> loanService.createLoan(bookId.toString(), memberId.toString()));

        verify(loanRepository, never()).save(any());
    }

    @Test
    @DisplayName("createLoan() with member at loan limit throws DomainException")
    void createLoan_memberAtLoanLimit_throwsDomainException() {
        // Fill the member to the limit
        for (int i = 0; i < Member.MAX_ACTIVE_LOANS; i++) {
            member.borrowBook(BookId.generate());
        }
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(availableBook));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        assertThrows(DomainException.class,
                () -> loanService.createLoan(bookId.toString(), memberId.toString()));

        verify(loanRepository, never()).save(any());
    }

    // ── returnLoan — happy path ───────────────────────────────────────────────

    @Test
    @DisplayName("returnLoan() happy path: loan closed, book returned, member updated")
    void returnLoan_happyPath_loanClosedAndAggregatesUpdated() {
        // Pre-condition: member has the book, book is checked out
        availableBook.checkOut();
        member.borrowBook(bookId);

        LoanId loanId = LoanId.generate();
        Loan   loan   = new Loan(loanId, bookId, memberId, LocalDate.now().minusDays(7));

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(availableBook));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(loanRepository.save(loan)).thenReturn(loan);
        when(bookRepository.save(availableBook)).thenReturn(availableBook);
        when(memberRepository.save(member)).thenReturn(member);

        Loan result = loanService.returnLoan(loanId.toString());

        assertEquals(LoanStatus.RETURNED, result.getStatus());
        assertTrue(result.getReturnDate().isPresent());
        assertTrue(availableBook.isAvailable());
        assertFalse(member.getActiveLoans().contains(bookId));

        verify(loanRepository,   times(1)).save(loan);
        verify(bookRepository,   times(1)).save(availableBook);
        verify(memberRepository, times(1)).save(member);
    }

    // ── returnLoan — failure paths ────────────────────────────────────────────

    @Test
    @DisplayName("returnLoan() with non-existing loan throws LoanNotFoundException")
    void returnLoan_loanNotFound_throwsLoanNotFoundException() {
        LoanId loanId = LoanId.generate();
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class,
                () -> loanService.returnLoan(loanId.toString()));

        verify(bookRepository,   never()).findById(any());
        verify(memberRepository, never()).findById(any());
    }

    @Test
    @DisplayName("returnLoan() with already-returned loan throws DomainException")
    void returnLoan_alreadyReturnedLoan_throwsDomainException() {
        availableBook.checkOut();
        member.borrowBook(bookId);

        LoanId loanId = LoanId.generate();
        Loan   loan   = new Loan(loanId, bookId, memberId, LocalDate.now().minusDays(7));
        loan.close(LocalDate.now().minusDays(1)); // already returned

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(availableBook));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        assertThrows(DomainException.class,
                () -> loanService.returnLoan(loanId.toString()));

        // Nothing should be persisted on failure
        verify(loanRepository,   never()).save(any());
        verify(bookRepository,   never()).save(any());
        verify(memberRepository, never()).save(any());
    }

    // ── findLoanById ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findLoanById() with existing id returns the loan")
    void findLoanById_existingId_returnsLoan() {
        LoanId loanId = LoanId.generate();
        Loan   loan   = new Loan(loanId, bookId, memberId, LocalDate.now());
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));

        Loan result = loanService.findLoanById(loanId.toString());

        assertEquals(loan, result);
    }

    @Test
    @DisplayName("findLoanById() with non-existing id throws LoanNotFoundException")
    void findLoanById_nonExistingId_throwsLoanNotFoundException() {
        LoanId loanId = LoanId.generate();
        when(loanRepository.findById(loanId)).thenReturn(Optional.empty());

        assertThrows(LoanNotFoundException.class,
                () -> loanService.findLoanById(loanId.toString()));
    }

    // ── findLoansByMember ─────────────────────────────────────────────────────

    @Test
    @DisplayName("findLoansByMember() delegates to repository with correct MemberId")
    void findLoansByMember_delegatesToRepository() {
        Loan loan = new Loan(LoanId.generate(), bookId, memberId, LocalDate.now());
        when(loanRepository.findByMemberId(memberId)).thenReturn(List.of(loan));

        List<Loan> result = loanService.findLoansByMember(memberId.toString());

        assertEquals(1, result.size());
        verify(loanRepository, times(1)).findByMemberId(memberId);
    }

    // ── findAllLoans ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAllLoans() delegates to repository")
    void findAllLoans_delegatesToRepository() {
        when(loanRepository.findAll()).thenReturn(List.of());

        loanService.findAllLoans();

        verify(loanRepository, times(1)).findAll();
    }

    // ── Constructor guard ─────────────────────────────────────────────────────

    @Test
    @DisplayName("constructor rejects null loanRepository")
    void constructor_nullLoanRepository_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new LoanService(null, bookRepository, memberRepository));
    }

    @Test
    @DisplayName("constructor rejects null bookRepository")
    void constructor_nullBookRepository_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new LoanService(loanRepository, null, memberRepository));
    }

    @Test
    @DisplayName("constructor rejects null memberRepository")
    void constructor_nullMemberRepository_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new LoanService(loanRepository, bookRepository, null));
    }
}
