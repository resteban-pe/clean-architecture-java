package pe.resteban.library.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.resteban.library.application.service.MemberService;
import pe.resteban.library.domain.exception.MemberNotFoundException;
import pe.resteban.library.domain.model.Member;
import pe.resteban.library.domain.model.MemberId;
import pe.resteban.library.domain.port.MemberRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberRepository);
    }

    // ── registerMember ────────────────────────────────────────────────────────

    @Test
    @DisplayName("registerMember() calls save() and returns member with correct data")
    void registerMember_callsSaveAndReturnsMember() {
        Member saved = new Member(MemberId.generate(), "Roosevelt Torres", "rt@resteban.pe");
        when(memberRepository.save(any(Member.class))).thenReturn(saved);

        Member result = memberService.registerMember("Roosevelt Torres", "rt@resteban.pe");

        verify(memberRepository, times(1)).save(any(Member.class));
        assertEquals("Roosevelt Torres", result.getName());
        assertEquals("rt@resteban.pe",   result.getEmail());
    }

    @Test
    @DisplayName("registerMember() generates a fresh MemberId (not null)")
    void registerMember_generatesNonNullId() {
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        when(memberRepository.save(any(Member.class))).thenAnswer(inv -> inv.getArgument(0));

        memberService.registerMember("Name", "email@test.com");

        verify(memberRepository).save(captor.capture());
        assertNotNull(captor.getValue().getId());
    }

    // ── findMemberById ────────────────────────────────────────────────────────

    @Test
    @DisplayName("findMemberById() with existing id returns the member")
    void findMemberById_existingId_returnsMember() {
        MemberId id     = MemberId.generate();
        Member   member = new Member(id, "Alice", "alice@test.com");
        when(memberRepository.findById(id)).thenReturn(Optional.of(member));

        Member result = memberService.findMemberById(id.toString());

        assertEquals(member, result);
    }

    @Test
    @DisplayName("findMemberById() with non-existing id throws MemberNotFoundException")
    void findMemberById_nonExistingId_throwsMemberNotFoundException() {
        MemberId id = MemberId.generate();
        when(memberRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(MemberNotFoundException.class,
                () -> memberService.findMemberById(id.toString()));
    }

    // ── findAllMembers ────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAllMembers() delegates to repository and returns all members")
    void findAllMembers_returnsAllMembers() {
        List<Member> members = List.of(
                new Member(MemberId.generate(), "Alice", "alice@test.com"),
                new Member(MemberId.generate(), "Bob",   "bob@test.com"));
        when(memberRepository.findAll()).thenReturn(members);

        List<Member> result = memberService.findAllMembers();

        assertEquals(2, result.size());
        verify(memberRepository, times(1)).findAll();
    }

    // ── Constructor guard ─────────────────────────────────────────────────────

    @Test
    @DisplayName("constructor rejects null repository")
    void constructor_nullRepository_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new MemberService(null));
    }
}
