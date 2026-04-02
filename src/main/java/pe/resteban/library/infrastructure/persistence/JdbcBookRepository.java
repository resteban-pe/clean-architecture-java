package pe.resteban.library.infrastructure.persistence;

import pe.resteban.library.domain.model.Book;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.port.BookRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC adapter for {@link BookRepository} backed by H2.
 */
public class JdbcBookRepository implements BookRepository {

    private final Connection connection;

    public JdbcBookRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Book> findById(BookId id) {
        String sql = "SELECT id, title, author, isbn, available FROM books WHERE id = ?";
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
    public List<Book> findAll() {
        String sql = "SELECT id, title, author, isbn, available FROM books";
        List<Book> result = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) result.add(mapRow(rs));
        } catch (SQLException e) {
            throw new IllegalStateException("findAll failed", e);
        }
        return result;
    }

    @Override
    public Book save(Book book) {
        String sql = """
                MERGE INTO books (id, title, author, isbn, available)
                KEY(id) VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1,  book.getId().toString());
            ps.setString(2,  book.getTitle());
            ps.setString(3,  book.getAuthor());
            ps.setString(4,  book.getIsbn());
            ps.setBoolean(5, book.isAvailable());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("save failed", e);
        }
        return book;
    }

    @Override
    public void deleteById(BookId id) {
        String sql = "DELETE FROM books WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("deleteById failed", e);
        }
    }

    // ── Mapping ───────────────────────────────────────────────────────────────

    private Book mapRow(ResultSet rs) throws SQLException {
        BookId id    = BookId.of(rs.getString("id"));
        Book   book  = new Book(id,
                rs.getString("title"),
                rs.getString("author"),
                rs.getString("isbn"));
        if (!rs.getBoolean("available")) book.checkOut();
        return book;
    }
}
