package pe.resteban.library.infrastructure.persistence;

import pe.resteban.library.domain.model.Member;
import pe.resteban.library.domain.model.MemberId;
import pe.resteban.library.domain.port.MemberRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Infrastructure adapter — in-memory implementation of {@link MemberRepository}.
 *
 * <p>Backed by a {@link HashMap} keyed on the string representation of {@link MemberId}.
 */
public class InMemoryMemberRepository implements MemberRepository {

    private final Map<String, Member> store = new HashMap<>();

    @Override
    public Optional<Member> findById(MemberId id) {
        return Optional.ofNullable(store.get(id.toString()));
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    /**
     * Persists (insert or update) the given member.
     *
     * <p>Calling {@code save} after mutations like {@code borrowBook()} or
     * {@code returnBook()} replaces the stale entry with the updated aggregate.
     */
    @Override
    public Member save(Member member) {
        store.put(member.getId().toString(), member);
        return member;
    }
}
