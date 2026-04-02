package pe.resteban.library.infrastructure.persistence;

import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.model.Member;
import pe.resteban.library.domain.model.MemberId;
import pe.resteban.library.domain.port.MemberRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC adapter for {@link MemberRepository} backed by H2.
 *
 * <p>Active loans are stored in {@code member_active_loans} and loaded eagerly
 * when reading a {@link Member}.
 */
public class JdbcMemberRepository implements MemberRepository {

    private final Connection connection;

    public JdbcMemberRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Member> findById(MemberId id) {
        String sql = "SELECT id, name, email FROM members WHERE id = ?";
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
    public List<Member> findAll() {
        String sql = "SELECT id, name, email FROM members";
        List<Member> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new IllegalStateException("findAll failed", e);
        }
        return result;
    }

    @Override
    public Member save(Member member) {
        upsertMember(member);
        replaceActiveLoans(member);
        return member;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void upsertMember(Member member) {
        String sql = """
                MERGE INTO members (id, name, email)
                KEY(id) VALUES (?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, member.getId().toString());
            ps.setString(2, member.getName());
            ps.setString(3, member.getEmail());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("upsertMember failed", e);
        }
    }

    private void replaceActiveLoans(Member member) {
        String delete = "DELETE FROM member_active_loans WHERE member_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(delete)) {
            ps.setString(1, member.getId().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("deleteActiveLoans failed", e);
        }

        if (member.getActiveLoans().isEmpty()) return;

        String insert = "INSERT INTO member_active_loans (member_id, book_id) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(insert)) {
            for (BookId bookId : member.getActiveLoans()) {
                ps.setString(1, member.getId().toString());
                ps.setString(2, bookId.toString());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new IllegalStateException("insertActiveLoans failed", e);
        }
    }

    private List<BookId> loadActiveLoans(String memberId) {
        String sql = "SELECT book_id FROM member_active_loans WHERE member_id = ?";
        List<BookId> loans = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) loans.add(BookId.of(rs.getString("book_id")));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("loadActiveLoans failed", e);
        }
        return loans;
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        MemberId id     = MemberId.of(rs.getString("id"));
        Member   member = new Member(id, rs.getString("name"), rs.getString("email"));
        loadActiveLoans(id.toString()).forEach(member::borrowBook);
        return member;
    }
}
