package pe.resteban.library.application.service;

import pe.resteban.library.application.usecase.MemberUseCase;
import pe.resteban.library.domain.exception.MemberNotFoundException;
import pe.resteban.library.domain.model.Member;
import pe.resteban.library.domain.model.MemberId;
import pe.resteban.library.domain.port.MemberRepository;

import java.util.List;
import java.util.Objects;

/**
 * Application service — orchestrates member-related use cases.
 */
public class MemberService implements MemberUseCase {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = Objects.requireNonNull(memberRepository,
                "memberRepository must not be null");
    }

    // ── MemberUseCase ─────────────────────────────────────────────────────────

    @Override
    public Member registerMember(String name, String email) {
        Member member = new Member(MemberId.generate(), name, email);
        return memberRepository.save(member);
    }

    @Override
    public Member findMemberById(String id) {
        MemberId memberId = MemberId.of(id);
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
    }

    @Override
    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }
}
