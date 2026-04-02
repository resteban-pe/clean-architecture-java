package pe.resteban.library.infrastructure.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads {@code application.properties} from the classpath.
 *
 * <p>Provides typed accessors for each known property so the rest of the
 * infrastructure layer never has to deal with raw strings or missing-key
 * exceptions.
 */
public class AppConfig {

    private static final String PROPERTIES_FILE = "application.properties";

    private final Properties props = new Properties();

    public AppConfig() {
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream(PROPERTIES_FILE)) {
            if (in == null) {
                throw new IllegalStateException(
                        PROPERTIES_FILE + " not found on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load " + PROPERTIES_FILE, e);
        }
    }

    /** Returns {@code inmemory} or {@code jdbc}. */
    public String getRepositoryType() {
        return props.getProperty("repository.type", "inmemory").trim().toLowerCase();
    }

    public String getDbUrl() {
        return props.getProperty("db.url", "jdbc:h2:mem:librarydb;DB_CLOSE_DELAY=-1");
    }

    public String getDbUser() {
        return props.getProperty("db.user", "sa");
    }

    public String getDbPassword() {
        return props.getProperty("db.password", "");
    }
}
