package pe.resteban.library.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.model.Member;
import pe.resteban.library.domain.model.MemberId;
import pe.resteban.library.infrastructure.persistence.JdbcMemberRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JdbcMemberRepository")
class JdbcMemberRepositoryTest extends JdbcTestBase {

    private JdbcMemberRepository repository;
    private MemberId memberId;
    private Member member;

    JdbcMemberRepositoryTest() {
        super("memberdb");
    }

    @BeforeEach
    void setUp() throws Exception {
        try (var stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM member_active_loans");
            stmt.execute("DELETE FROM members");
        }
        repository = new JdbcMemberRepository(connection);
        memberId   = MemberId.generate();
        member     = new Member(memberId, "Roosevelt Torres", "rt@resteban.pe");
    }

    @Test
    @DisplayName("save() and findById() round-trip")
    void save_findById_roundTrip() {
        repository.save(member);

        Member found = repository.findById(memberId).orElseThrow();
        assertEquals("Roosevelt Torres", found.getName());
        assertEquals("rt@resteban.pe",   found.getEmail());
    }

    @Test
    @DisplayName("save() persists active loans")
    void save_withActiveLoans_persistsLoans() {
        BookId bookId = BookId.generate();
        member.borrowBook(bookId);
        repository.save(member);

        Member found = repository.findById(memberId).orElseThrow();
        assertEquals(1, found.getActiveLoans().size());
        assertTrue(found.getActiveLoans().contains(bookId));
    }

    @Test
    @DisplayName("save() replaces active loans on update")
    void save_update_replacesActiveLoans() {
        BookId b1 = BookId.generate();
        BookId b2 = BookId.generate();
        member.borrowBook(b1);
        repository.save(member);

        member.borrowBook(b2);
        repository.save(member);

        Member found = repository.findById(memberId).orElseThrow();
        assertEquals(2, found.getActiveLoans().size());
    }

    @Test
    @DisplayName("findById() returns empty for unknown id")
    void findById_unknown_returnsEmpty() {
        assertTrue(repository.findById(MemberId.generate()).isEmpty());
    }

    @Test
    @DisplayName("findAll() returns all saved members")
    void findAll_returnsAllMembers() {
        repository.save(member);
        repository.save(new Member(MemberId.generate(), "Alice", "alice@test.com"));

        List<Member> all = repository.findAll();
        assertEquals(2, all.size());
    }
}
