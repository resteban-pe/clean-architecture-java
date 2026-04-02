package pe.resteban.library.application.usecase;

import pe.resteban.library.domain.model.Loan;

import java.util.List;

/**
 * Input port — defines all loan-related operations available to the outside world.
 */
public interface LoanUseCase {

    /**
     * Opens a new loan: checks the book out and registers it on the member.
     *
     * @throws pe.resteban.library.domain.exception.BookNotFoundException   if book not found.
     * @throws pe.resteban.library.domain.exception.MemberNotFoundException if member not found.
     * @throws pe.resteban.library.domain.exception.DomainException         if book unavailable
     *                                                                       or member has reached
     *                                                                       the loan limit.
     */
    Loan createLoan(String bookId, String memberId);

    /**
     * Closes an active loan: returns the book to the shelf and updates the member record.
     *
     * @throws pe.resteban.library.domain.exception.LoanNotFoundException   if loan not found.
     * @throws pe.resteban.library.domain.exception.BookNotFoundException   if book not found.
     * @throws pe.resteban.library.domain.exception.MemberNotFoundException if member not found.
     * @throws pe.resteban.library.domain.exception.DomainException         if loan already returned.
     */
    Loan returnLoan(String loanId);

    /** Returns the loan with the given id or throws {@code LoanNotFoundException}. */
    Loan findLoanById(String loanId);

    /** Returns all loans held by the given member. */
    List<Loan> findLoansByMember(String memberId);

    /** Returns every loan in the system. */
    List<Loan> findAllLoans();
}
