package pe.resteban.library.main;

import pe.resteban.library.application.service.BookService;
import pe.resteban.library.application.service.LoanService;
import pe.resteban.library.application.service.MemberService;
import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.Loan;
import pe.resteban.library.domain.model.Member;
import pe.resteban.library.domain.port.BookRepository;
import pe.resteban.library.domain.port.LoanRepository;
import pe.resteban.library.domain.port.MemberRepository;
import pe.resteban.library.infrastructure.cli.LibraryCLI;
import pe.resteban.library.infrastructure.config.AppConfig;
import pe.resteban.library.infrastructure.config.RepositoryFactory;

/**
 * Application entry point — manual dependency injection (no framework).
 *
 * <p>Persistence adapter is selected at runtime via {@code application.properties}:
 * <pre>
 *   repository.type=inmemory   →  in-memory HashMap adapters (default)
 *   repository.type=jdbc       →  JDBC adapters backed by H2 embedded database
 * </pre>
 *
 * <p>Run mode is controlled by the first command-line argument:
 * <pre>
 *   (no args) or --demo   →  automated demo flow
 *   --cli                 →  interactive console menu
 * </pre>
 */
public class LibraryApp {

    public static void main(String[] args) {

        // ── 1. Config + Repository factory ───────────────────────────────────
        AppConfig         config  = new AppConfig();
        RepositoryFactory factory = new RepositoryFactory(config);

        BookRepository   bookRepository   = factory.bookRepository();
        MemberRepository memberRepository = factory.memberRepository();
        LoanRepository   loanRepository   = factory.loanRepository();

        print("Repository type : " + config.getRepositoryType());

        // ── 2. Services ───────────────────────────────────────────────────────
        BookService   bookService   = new BookService(bookRepository);
        MemberService memberService = new MemberService(memberRepository);
        LoanService   loanService   = new LoanService(loanRepository, bookRepository, memberRepository);

        // ── 3. Mode selection ─────────────────────────────────────────────────
        if (args.length > 0 && args[0].equalsIgnoreCase("--cli")) {
            new LibraryCLI(bookService, memberService, loanService).start();
            return;
        }

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
