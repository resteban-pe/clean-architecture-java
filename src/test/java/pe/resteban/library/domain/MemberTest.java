package pe.resteban.library.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.resteban.library.domain.exception.DomainException;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.model.Member;
import pe.resteban.library.domain.model.MemberId;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Member")
class MemberTest {

    private Member member;

    @BeforeEach
    void setUp() {
        member = new Member(MemberId.generate(), "Roosevelt Torres", "roosevelt@resteban.pe");
    }

    // ── Initial state ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("new member has no active loans")
    void newMember_hasNoActiveLoans() {
        assertTrue(member.getActiveLoans().isEmpty());
    }

    // ── borrowBook ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("borrowBook() adds the bookId to activeLoans")
    void borrowBook_addsBookIdToActiveLoans() {
        BookId bookId = BookId.generate();

        member.borrowBook(bookId);

        assertTrue(member.getActiveLoans().contains(bookId));
        assertEquals(1, member.getActiveLoans().size());
    }

    @Test
    @DisplayName("borrowBook() with MAX_ACTIVE_LOANS already active throws DomainException")
    void borrowBook_atLoanLimit_throwsDomainException() {
        for (int i = 0; i < Member.MAX_ACTIVE_LOANS; i++) {
            member.borrowBook(BookId.generate());
        }

        assertThrows(DomainException.class, () -> member.borrowBook(BookId.generate()));
    }

    @Test
    @DisplayName("borrowBook() allows exactly MAX_ACTIVE_LOANS loans")
    void borrowBook_exactlyAtLimit_succeeds() {
        for (int i = 0; i < Member.MAX_ACTIVE_LOANS; i++) {
            member.borrowBook(BookId.generate());
        }

        assertEquals(Member.MAX_ACTIVE_LOANS, member.getActiveLoans().size());
    }

    // ── returnBook ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("returnBook() removes the bookId from activeLoans")
    void returnBook_removesBookIdFromActiveLoans() {
        BookId bookId = BookId.generate();
        member.borrowBook(bookId);

        member.returnBook(bookId);

        assertFalse(member.getActiveLoans().contains(bookId));
        assertTrue(member.getActiveLoans().isEmpty());
    }

    @Test
    @DisplayName("returnBook() with non-existent bookId throws DomainException")
    void returnBook_nonExistentBookId_throwsDomainException() {
        BookId unknown = BookId.generate();

        assertThrows(DomainException.class, () -> member.returnBook(unknown));
    }

    // ── getActiveLoans defensive copy ─────────────────────────────────────────

    @Test
    @DisplayName("getActiveLoans() returns an unmodifiable view")
    void getActiveLoans_returnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
                () -> member.getActiveLoans().add(BookId.generate()));
    }

    // ── Constructor guards ────────────────────────────────────────────────────

    @Test
    @DisplayName("constructor rejects null id")
    void constructor_nullId_throwsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new Member(null, "Name", "email@test.com"));
    }

    // ── Equality ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("two members with the same id are equal")
    void equals_sameId_returnsTrue() {
        MemberId id = MemberId.generate();
        Member a = new Member(id, "Alice", "alice@test.com");
        Member b = new Member(id, "Bob",   "bob@test.com");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }
}
