package pe.resteban.library.domain.model;

import java.util.UUID;

/**
 * Value Object — identity of a Loan.
 */
public record LoanId(UUID value) {

    public LoanId {
        if (value == null) throw new IllegalArgumentException("LoanId value must not be null");
    }

    public static LoanId generate() {
        return new LoanId(UUID.randomUUID());
    }

    public static LoanId of(String uuid) {
        return new LoanId(UUID.fromString(uuid));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
