package pe.resteban.library.application.service;

import pe.resteban.library.application.usecase.LoanUseCase;
import pe.resteban.library.domain.exception.BookNotFoundException;
import pe.resteban.library.domain.exception.LoanNotFoundException;
import pe.resteban.library.domain.exception.MemberNotFoundException;
import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.model.Loan;
import pe.resteban.library.domain.model.LoanId;
import pe.resteban.library.domain.model.Member;
import pe.resteban.library.domain.model.MemberId;
import pe.resteban.library.domain.port.BookRepository;
import pe.resteban.library.domain.port.LoanRepository;
import pe.resteban.library.domain.port.MemberRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Application service — orchestrates loan-related use cases.
 *
 * <p>{@link #createLoan} and {@link #returnLoan} coordinate three aggregates
 * (Book, Member, Loan) in a single logical transaction. In a real system this
 * method would be wrapped in a unit-of-work / DB transaction; here the in-memory
 * adapters make that implicit.
 */
public class LoanService implements LoanUseCase {

    private final LoanRepository   loanRepository;
    private final BookRepository   bookRepository;
    private final MemberRepository memberRepository;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       MemberRepository memberRepository) {
        this.loanRepository   = Objects.requireNonNull(loanRepository,   "loanRepository must not be null");
        this.bookRepository   = Objects.requireNonNull(bookRepository,   "bookRepository must not be null");
        this.memberRepository = Objects.requireNonNull(memberRepository, "memberRepository must not be null");
    }

    // ── LoanUseCase ───────────────────────────────────────────────────────────

    /**
     * Opens a new loan.
     *
     * <ol>
     *   <li>Resolves both aggregates — fail fast if either is missing.</li>
     *   <li>Applies domain rules: {@code book.checkOut()} and
     *       {@code member.borrowBook(bookId)} (may throw {@code DomainException}).</li>
     *   <li>Persists the updated aggregates and the new loan.</li>
     * </ol>
     */
    @Override
    public Loan createLoan(String bookIdStr, String memberIdStr) {
        BookId   bookId   = BookId.of(bookIdStr);
        MemberId memberId = MemberId.of(memberIdStr);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        // Domain rules — may throw DomainException
        book.checkOut();
        member.borrowBook(bookId);

        Loan loan = new Loan(LoanId.generate(), bookId, memberId, LocalDate.now());

        // Persist all three in the order: loan first, then aggregates
        Loan savedLoan = loanRepository.save(loan);
        bookRepository.save(book);
        memberRepository.save(member);

        return savedLoan;
    }

    /**
     * Closes an active loan.
     *
     * <ol>
     *   <li>Finds the loan — fail if not found.</li>
     *   <li>Reads bookId and memberId from the loan <em>before</em> closing it.</li>
     *   <li>Applies domain rules: {@code loan.close()}, {@code book.returnBook()},
     *       {@code member.returnBook(bookId)}.</li>
     *   <li>Persists the three updated objects.</li>
     * </ol>
     */
    @Override
    public Loan returnLoan(String loanIdStr) {
        LoanId loanId = LoanId.of(loanIdStr);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));

        // Capture references before state changes
        BookId   bookId   = loan.getBookId();
        MemberId memberId = loan.getMemberId();

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        // Domain rules — may throw DomainException
        loan.close(LocalDate.now());
        book.returnBook();
        member.returnBook(bookId);

        // Persist all three
        Loan savedLoan = loanRepository.save(loan);
        bookRepository.save(book);
        memberRepository.save(member);

        return savedLoan;
    }

    @Override
    public Loan findLoanById(String loanIdStr) {
        LoanId loanId = LoanId.of(loanIdStr);
        return loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotFoundException(loanId));
    }

    @Override
    public List<Loan> findLoansByMember(String memberIdStr) {
        return loanRepository.findByMemberId(MemberId.of(memberIdStr));
    }

    @Override
    public List<Loan> findAllLoans() {
        return loanRepository.findAll();
    }
}
