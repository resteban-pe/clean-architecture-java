package pe.resteban.library.application.usecase;

import pe.resteban.library.domain.model.Member;

import java.util.List;

/**
 * Input port — defines all member-related operations available to the outside world.
 */
public interface MemberUseCase {

    /** Creates and persists a new library member. */
    Member registerMember(String name, String email);

    /** Returns the member with the given id or throws {@code MemberNotFoundException}. */
    Member findMemberById(String id);

    /** Returns all registered members. */
    List<Member> findAllMembers();
}
