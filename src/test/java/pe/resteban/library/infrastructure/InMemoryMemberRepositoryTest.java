package pe.resteban.library.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.resteban.library.domain.model.Member;
import pe.resteban.library.domain.model.MemberId;
import pe.resteban.library.infrastructure.persistence.InMemoryMemberRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InMemoryMemberRepository")
class InMemoryMemberRepositoryTest {

    private InMemoryMemberRepository repository;
    private Member member;
    private MemberId memberId;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMemberRepository();
        memberId   = MemberId.generate();
        member     = new Member(memberId, "Roosevelt Torres", "rt@resteban.pe");
    }

    // ── save ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("save() persists the member and returns it")
    void save_persistsAndReturnsMember() {
        Member saved = repository.save(member);

        assertSame(member, saved);
    }

    @Test
    @DisplayName("save() with same id overwrites previous entry")
    void save_sameId_overwritesPreviousEntry() {
        repository.save(member);
        Member updated = new Member(memberId, "Updated Name", "updated@resteban.pe");
        repository.save(updated);

        Member found = repository.findById(memberId).orElseThrow();
        assertEquals("Updated Name", found.getName());
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById() returns present Optional for existing member")
    void findById_existingMember_returnsPresent() {
        repository.save(member);

        Optional<Member> result = repository.findById(memberId);

        assertTrue(result.isPresent());
        assertEquals(member, result.get());
    }

    @Test
    @DisplayName("findById() returns empty Optional for non-existing member")
    void findById_nonExistingMember_returnsEmpty() {
        Optional<Member> result = repository.findById(MemberId.generate());

        assertTrue(result.isEmpty());
    }

    // ── findAll ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll() returns empty list when no members saved")
    void findAll_empty_returnsEmptyList() {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    @DisplayName("findAll() returns all saved members")
    void findAll_withMembers_returnsAllMembers() {
        Member member2 = new Member(MemberId.generate(), "Alice", "alice@test.com");
        repository.save(member);
        repository.save(member2);

        List<Member> result = repository.findAll();

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findAll() returns a defensive copy — mutation does not affect store")
    void findAll_returnsDefensiveCopy() {
        repository.save(member);
        repository.findAll().clear();

        assertEquals(1, repository.findAll().size());
    }
}
