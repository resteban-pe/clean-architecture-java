package pe.resteban.library.infrastructure.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Creates and initialises the JDBC {@link Connection} for the H2 in-memory database.
 *
 * <p>On first call to {@link #getConnection()} the schema is created by executing
 * {@code schema.sql} from the classpath. The same connection instance is reused
 * for the lifetime of the application (single-connection, no pool).
 */
public class DataSourceFactory {

    private static final String SCHEMA_FILE = "schema.sql";

    private final AppConfig config;
    private Connection connection;

    public DataSourceFactory(AppConfig config) {
        this.config = config;
    }

    /**
     * Returns the shared JDBC connection, creating and initialising it on first call.
     */
    public Connection getConnection() {
        if (connection == null) {
            connection = createConnection();
            runSchema(connection);
        }
        return connection;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Connection createConnection() {
        try {
            return DriverManager.getConnection(
                    config.getDbUrl(),
                    config.getDbUser(),
                    config.getDbPassword());
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot open JDBC connection: " + e.getMessage(), e);
        }
    }

    private void runSchema(Connection conn) {
        String sql = loadResource(SCHEMA_FILE);
        // Strip comment lines first, then split on ";" to get individual statements
        String stripped = stripComments(sql);
        for (String statement : stripped.split(";")) {
            String trimmed = statement.strip();
            if (trimmed.isEmpty()) continue;
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(trimmed);
            } catch (SQLException e) {
                throw new IllegalStateException(
                        "Failed to execute schema statement: " + trimmed, e);
            }
        }
    }

    /** Removes lines that are pure SQL comments (start with --). */
    private String stripComments(String sql) {
        StringBuilder sb = new StringBuilder();
        for (String line : sql.split("\n")) {
            if (!line.strip().startsWith("--")) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private String loadResource(String name) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(name)) {
            if (in == null) {
                throw new IllegalStateException(name + " not found on classpath");
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + name, e);
        }
    }
}
