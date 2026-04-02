package pe.resteban.library.infrastructure.cli;

import pe.resteban.library.application.usecase.BookUseCase;
import pe.resteban.library.application.usecase.LoanUseCase;
import pe.resteban.library.application.usecase.MemberUseCase;
import pe.resteban.library.domain.exception.DomainException;
import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.Loan;
import pe.resteban.library.domain.model.Member;

import java.util.List;
import java.util.Scanner;

/**
 * Primary adapter — interactive console CLI.
 *
 * <p>Depends only on the input-port interfaces ({@link BookUseCase},
 * {@link MemberUseCase}, {@link LoanUseCase}), never on concrete services
 * or repositories. This preserves the dependency rule.
 */
public class LibraryCLI {

    private final BookUseCase   bookUseCase;
    private final MemberUseCase memberUseCase;
    private final LoanUseCase   loanUseCase;
    private final Scanner       scanner;

    public LibraryCLI(BookUseCase bookUseCase,
                      MemberUseCase memberUseCase,
                      LoanUseCase loanUseCase) {
        this.bookUseCase   = bookUseCase;
        this.memberUseCase = memberUseCase;
        this.loanUseCase   = loanUseCase;
        this.scanner       = new Scanner(System.in);
    }

    /** Starts the menu loop — runs until the user chooses Exit. */
    public void start() {
        println("\n╔══════════════════════════════════════╗");
        println("║   LIBRARY MANAGEMENT SYSTEM — CLI   ║");
        println("╚══════════════════════════════════════╝");

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            System.out.println();
            running = handleChoice(choice);
        }
        println("Goodbye.");
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    private void printMenu() {
        println("\n── Books ─────────────────────────────");
        println("  1. List all books");
        println("  2. Add a book");
        println("  3. Delete a book");
        println("── Members ───────────────────────────");
        println("  4. List all members");
        println("  5. Register a member");
        println("── Loans ─────────────────────────────");
        println("  6. List all loans");
        println("  7. Create a loan");
        println("  8. Return a loan");
        println("──────────────────────────────────────");
        println("  0. Exit");
        print("Choice: ");
    }

    /** Returns {@code false} when the user chooses to exit. */
    private boolean handleChoice(String choice) {
        try {
            switch (choice) {
                case "1" -> listBooks();
                case "2" -> addBook();
                case "3" -> deleteBook();
                case "4" -> listMembers();
                case "5" -> registerMember();
                case "6" -> listLoans();
                case "7" -> createLoan();
                case "8" -> returnLoan();
                case "0" -> { return false; }
                default  -> println("Unknown option — please choose 0-8.");
            }
        } catch (DomainException e) {
            println("[Domain rule violation] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            println("[Invalid input] " + e.getMessage());
        }
        return true;
    }

    // ── Book operations ───────────────────────────────────────────────────────

    private void listBooks() {
        List<Book> books = bookUseCase.findAllBooks();
        if (books.isEmpty()) {
            println("No books in the catalogue.");
            return;
        }
        println(String.format("%-36s  %-30s  %-20s  %-15s  %s",
                "ID", "Title", "Author", "ISBN", "Available"));
        println("─".repeat(115));
        books.forEach(b -> println(String.format("%-36s  %-30s  %-20s  %-15s  %s",
                b.getId(), truncate(b.getTitle(), 30),
                truncate(b.getAuthor(), 20), b.getIsbn(), b.isAvailable())));
    }

    private void addBook() {
        String title  = prompt("Title  : ");
        String author = prompt("Author : ");
        String isbn   = prompt("ISBN   : ");
        Book book = bookUseCase.createBook(title, author, isbn);
        println("Book created — id: " + book.getId());
    }

    private void deleteBook() {
        String id = prompt("Book ID to delete: ");
        bookUseCase.deleteBook(id);
        println("Book deleted.");
    }

    // ── Member operations ─────────────────────────────────────────────────────

    private void listMembers() {
        List<Member> members = memberUseCase.findAllMembers();
        if (members.isEmpty()) {
            println("No members registered.");
            return;
        }
        println(String.format("%-36s  %-25s  %-30s  %s",
                "ID", "Name", "Email", "Active loans"));
        println("─".repeat(100));
        members.forEach(m -> println(String.format("%-36s  %-25s  %-30s  %d",
                m.getId(), truncate(m.getName(), 25),
                truncate(m.getEmail(), 30), m.getActiveLoans().size())));
    }

    private void registerMember() {
        String name  = prompt("Name  : ");
        String email = prompt("Email : ");
        Member member = memberUseCase.registerMember(name, email);
        println("Member registered — id: " + member.getId());
    }

    // ── Loan operations ───────────────────────────────────────────────────────

    private void listLoans() {
        List<Loan> loans = loanUseCase.findAllLoans();
        if (loans.isEmpty()) {
            println("No loans recorded.");
            return;
        }
        println(String.format("%-36s  %-36s  %-36s  %-12s  %-12s  %s",
                "Loan ID", "Book ID", "Member ID", "Loan date", "Return date", "Status"));
        println("─".repeat(145));
        loans.forEach(l -> println(String.format("%-36s  %-36s  %-36s  %-12s  %-12s  %s",
                l.getId(), l.getBookId(), l.getMemberId(),
                l.getLoanDate(),
                l.getReturnDate().map(Object::toString).orElse("—"),
                l.getStatus())));
    }

    private void createLoan() {
        String bookId   = prompt("Book ID   : ");
        String memberId = prompt("Member ID : ");
        Loan loan = loanUseCase.createLoan(bookId, memberId);
        println("Loan created — id: " + loan.getId() + "  |  date: " + loan.getLoanDate());
    }

    private void returnLoan() {
        String loanId = prompt("Loan ID : ");
        Loan loan = loanUseCase.returnLoan(loanId);
        println("Loan returned — status: " + loan.getStatus() +
                "  |  return date: " + loan.getReturnDate().orElse(null));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String prompt(String label) {
        print(label);
        return scanner.nextLine().trim();
    }

    private static void print(String msg)   { System.out.print(msg); }
    private static void println(String msg) { System.out.println(msg); }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
