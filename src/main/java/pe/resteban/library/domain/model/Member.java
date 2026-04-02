package pe.resteban.library.domain.model;

import pe.resteban.library.domain.exception.DomainException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root — represents a library member.
 *
 * <p>Invariant: a member may hold at most {@value #MAX_ACTIVE_LOANS} active loans at once.
 */
public class Member {

    public static final int MAX_ACTIVE_LOANS = 3;

    private final MemberId id;
    private String name;
    private String email;
    private final List<BookId> activeLoans;

    public Member(MemberId id, String name, String email) {
        this.id          = Objects.requireNonNull(id,    "id must not be null");
        this.name        = Objects.requireNonNull(name,  "name must not be null");
        this.email       = Objects.requireNonNull(email, "email must not be null");
        this.activeLoans = new ArrayList<>();
    }

    // ── Domain behaviour ──────────────────────────────────────────────────────

    /**
     * Registers a new loan for the given book.
     *
     * @throws DomainException if the member already holds {@value #MAX_ACTIVE_LOANS} loans.
     */
    public void borrowBook(BookId bookId) {
        Objects.requireNonNull(bookId, "bookId must not be null");
        if (activeLoans.size() >= MAX_ACTIVE_LOANS) {
            throw new DomainException(
                "Member '" + name + "' has reached the maximum of " +
                MAX_ACTIVE_LOANS + " active loans");
        }
        activeLoans.add(bookId);
    }

    /**
     * Removes the loan record for the given book.
     *
     * @throws DomainException if the member has no active loan for that book.
     */
    public void returnBook(BookId bookId) {
        Objects.requireNonNull(bookId, "bookId must not be null");
        boolean removed = activeLoans.remove(bookId);
        if (!removed) {
            throw new DomainException(
                "Member '" + name + "' has no active loan for book id=" + bookId);
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public MemberId getId()                  { return id; }
    public String getName()                  { return name; }
    public String getEmail()                 { return email; }
    public List<BookId> getActiveLoans()     { return Collections.unmodifiableList(activeLoans); }

    // ── Equality by identity ──────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member member)) return false;
        return id.equals(member.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() {
        return "Member{id=" + id + ", name='" + name + "', activeLoans=" + activeLoans.size() + '}';
    }
}
