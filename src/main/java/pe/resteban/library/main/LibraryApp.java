package pe.resteban.library.main;

import pe.resteban.library.application.service.BookService;
import pe.resteban.library.application.service.LoanService;
import pe.resteban.library.application.service.MemberService;
import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.Loan;
import pe.resteban.library.domain.model.Member;
import pe.resteban.library.infrastructure.persistence.InMemoryBookRepository;
import pe.resteban.library.infrastructure.persistence.InMemoryLoanRepository;
import pe.resteban.library.infrastructure.persistence.InMemoryMemberRepository;

/**
 * Application entry point — manual dependency injection (no framework).
 *
 * <p>Wiring order:
 * <ol>
 *   <li>Repositories (infrastructure adapters)</li>
 *   <li>Services (application layer) receive repositories via constructor</li>
 *   <li>Demo flow executes through the service interfaces</li>
 * </ol>
 */
public class LibraryApp {

    public static void main(String[] args) {

        // ── 1. Repositories ───────────────────────────────────────────────────
        InMemoryBookRepository   bookRepository   = new InMemoryBookRepository();
        InMemoryMemberRepository memberRepository = new InMemoryMemberRepository();
        InMemoryLoanRepository   loanRepository   = new InMemoryLoanRepository();

        // ── 2. Services ───────────────────────────────────────────────────────
        BookService   bookService   = new BookService(bookRepository);
        MemberService memberService = new MemberService(memberRepository);
        LoanService   loanService   = new LoanService(loanRepository, bookRepository, memberRepository);

        // ── 3. Demo flow ──────────────────────────────────────────────────────
        separator("LIBRARY MANAGEMENT SYSTEM — demo");

        // Create 2 books
        Book book1 = bookService.createBook(
                "Clean Architecture", "Robert C. Martin", "978-0134494166");
        Book book2 = bookService.createBook(
                "Domain-Driven Design", "Eric Evans", "978-0321125217");
        print("Book created : " + book1);
        print("Book created : " + book2);

        // Register 1 member
        Member member = memberService.registerMember("Roosevelt Torres", "roosevelt@resteban.pe");
        print("Member registered : " + member);

        // Create a loan — book1 → member
        separator("CREATE LOAN  (book1 → member)");
        Loan loan = loanService.createLoan(
                book1.getId().toString(),
                member.getId().toString());
        print("Loan opened   : " + loan);
        print("Book1 status  : available=" + bookService.findBookById(book1.getId().toString()).isAvailable());
        print("Member loans  : " + memberService.findMemberById(member.getId().toString()).getActiveLoans().size() + " active");

        // Return the loan
        separator("RETURN LOAN");
        Loan returned = loanService.returnLoan(loan.getId().toString());
        print("Loan closed   : " + returned);
        print("Return date   : " + returned.getReturnDate().orElse(null));

        // Final state
        separator("FINAL STATE");
        print("-- Books --");
        bookService.findAllBooks().forEach(b ->
                print("  " + b.getTitle() + " | available=" + b.isAvailable()));

        print("-- Members --");
        memberService.findAllMembers().forEach(m ->
                print("  " + m.getName() + " | active loans=" + m.getActiveLoans().size()));

        print("-- Loans --");
        loanService.findAllLoans().forEach(l ->
                print("  loanId=" + l.getId() + " | status=" + l.getStatus()));

        separator("END");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void print(String msg) {
        System.out.println(msg);
    }

    private static void separator(String title) {
        System.out.println("\n=== " + title + " ===");
    }
}
