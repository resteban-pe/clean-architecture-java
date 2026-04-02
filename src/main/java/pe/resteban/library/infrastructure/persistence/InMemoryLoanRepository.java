package pe.resteban.library.infrastructure.persistence;

import pe.resteban.library.domain.model.Loan;
import pe.resteban.library.domain.model.LoanId;
import pe.resteban.library.domain.model.MemberId;
import pe.resteban.library.domain.port.LoanRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Infrastructure adapter — in-memory implementation of {@link LoanRepository}.
 *
 * <p>Backed by a {@link HashMap} keyed on the string representation of {@link LoanId}.
 * {@link #findByMemberId} performs a linear scan — acceptable for an in-memory store
 * where the collection is small and there is no secondary index.
 */
public class InMemoryLoanRepository implements LoanRepository {

    private final Map<String, Loan> store = new HashMap<>();

    @Override
    public Optional<Loan> findById(LoanId id) {
        return Optional.ofNullable(store.get(id.toString()));
    }

    @Override
    public List<Loan> findByMemberId(MemberId memberId) {
        return store.values().stream()
                .filter(loan -> loan.getMemberId().equals(memberId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Loan> findAll() {
        return new ArrayList<>(store.values());
    }

    /**
     * Persists (insert or update) the given loan.
     *
     * <p>Calling {@code save} after {@code loan.close()} replaces the active
     * entry with the returned state.
     */
    @Override
    public Loan save(Loan loan) {
        store.put(loan.getId().toString(), loan);
        return loan;
    }
}
