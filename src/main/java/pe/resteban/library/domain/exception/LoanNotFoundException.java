package pe.resteban.library.domain.exception;

import pe.resteban.library.domain.model.LoanId;

public class LoanNotFoundException extends DomainException {

    public LoanNotFoundException(LoanId id) {
        super("Loan not found with id: " + id);
    }

    public LoanNotFoundException(String message) {
        super(message);
    }
}
