package pe.resteban.library.domain.port;

import pe.resteban.library.domain.model.Loan;
import pe.resteban.library.domain.model.LoanId;
import pe.resteban.library.domain.model.MemberId;

import java.util.List;
import java.util.Optional;

/**
 * Output port — persistence contract for {@link Loan} entities.
 */
public interface LoanRepository {

    Optional<Loan> findById(LoanId id);

    List<Loan> findByMemberId(MemberId memberId);

    List<Loan> findAll();

    Loan save(Loan loan);
}
