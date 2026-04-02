package pe.resteban.library.domain.port;

import pe.resteban.library.domain.model.Member;
import pe.resteban.library.domain.model.MemberId;

import java.util.List;
import java.util.Optional;

/**
 * Output port — persistence contract for {@link Member} aggregates.
 */
public interface MemberRepository {

    Optional<Member> findById(MemberId id);

    List<Member> findAll();

    Member save(Member member);
}
