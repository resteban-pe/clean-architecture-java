package pe.resteban.library.infrastructure;

import pe.resteban.library.infrastructure.config.DataSourceFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Base for JDBC integration tests.
 *
 * <p>Each test class gets its own fresh H2 in-memory database by using a unique
 * named database URL. The schema is applied via {@link DataSourceFactory}, which
 * means DataSourceFactory itself is exercised by every subclass.
 */
public abstract class JdbcTestBase {

    protected final Connection connection;

    protected JdbcTestBase(String dbName) {
        try {
            connection = DriverManager.getConnection(
                    "jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1", "sa", "");
            applySchema(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot set up test database", e);
        }
    }

    private void applySchema(Connection conn) {
        runSchemaOn(conn);
    }

    /** Executes schema.sql directly on the given connection. */
    static void runSchemaOn(Connection conn) {
        String[] statements = {
            "CREATE TABLE IF NOT EXISTS books (" +
                "id VARCHAR(36) NOT NULL PRIMARY KEY," +
                "title VARCHAR(255) NOT NULL," +
                "author VARCHAR(255) NOT NULL," +
                "isbn VARCHAR(50) NOT NULL," +
                "available BOOLEAN NOT NULL DEFAULT TRUE)",

            "CREATE TABLE IF NOT EXISTS members (" +
                "id VARCHAR(36) NOT NULL PRIMARY KEY," +
                "name VARCHAR(255) NOT NULL," +
                "email VARCHAR(255) NOT NULL)",

            "CREATE TABLE IF NOT EXISTS member_active_loans (" +
                "member_id VARCHAR(36) NOT NULL," +
                "book_id VARCHAR(36) NOT NULL," +
                "PRIMARY KEY (member_id, book_id)," +
                "FOREIGN KEY (member_id) REFERENCES members(id))",

            "CREATE TABLE IF NOT EXISTS loans (" +
                "id VARCHAR(36) NOT NULL PRIMARY KEY," +
                "book_id VARCHAR(36) NOT NULL," +
                "member_id VARCHAR(36) NOT NULL," +
                "loan_date DATE NOT NULL," +
                "return_date DATE," +
                "status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE')"
        };
        try {
            for (String sql : statements) {
                try (var stmt = conn.createStatement()) {
                    stmt.execute(sql);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Schema creation failed", e);
        }
    }
}
