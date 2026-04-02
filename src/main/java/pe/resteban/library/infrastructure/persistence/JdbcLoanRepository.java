package pe.resteban.library.infrastructure.persistence;

import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.model.Loan;
import pe.resteban.library.domain.model.LoanId;
import pe.resteban.library.domain.model.MemberId;
import pe.resteban.library.domain.port.LoanRepository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC adapter for {@link LoanRepository} backed by H2.
 */
public class JdbcLoanRepository implements LoanRepository {

    private final Connection connection;

    public JdbcLoanRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Loan> findById(LoanId id) {
        String sql = "SELECT id, book_id, member_id, loan_date, return_date, status FROM loans WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("findById failed", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Loan> findByMemberId(MemberId memberId) {
        String sql = "SELECT id, book_id, member_id, loan_date, return_date, status FROM loans WHERE member_id = ?";
        List<Loan> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, memberId.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("findByMemberId failed", e);
        }
        return result;
    }

    @Override
    public List<Loan> findAll() {
        String sql = "SELECT id, book_id, member_id, loan_date, return_date, status FROM loans";
        List<Loan> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new IllegalStateException("findAll failed", e);
        }
        return result;
    }

    @Override
    public Loan save(Loan loan) {
        String sql = """
                MERGE INTO loans (id, book_id, member_id, loan_date, return_date, status)
                KEY(id) VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, loan.getId().toString());
            ps.setString(2, loan.getBookId().toString());
            ps.setString(3, loan.getMemberId().toString());
            ps.setDate(4,   Date.valueOf(loan.getLoanDate()));
            ps.setDate(5,   loan.getReturnDate().map(Date::valueOf).orElse(null));
            ps.setString(6, loan.getStatus().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("save failed", e);
        }
        return loan;
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private Loan mapRow(ResultSet rs) throws SQLException {
        LoanId   id       = LoanId.of(rs.getString("id"));
        BookId   bookId   = BookId.of(rs.getString("book_id"));
        MemberId memberId = MemberId.of(rs.getString("member_id"));
        LocalDate loanDate = rs.getDate("loan_date").toLocalDate();

        Loan loan = new Loan(id, bookId, memberId, loanDate);

        Date returnDate = rs.getDate("return_date");
        if (returnDate != null) {
            loan.close(returnDate.toLocalDate());
        }
        return loan;
    }
}
