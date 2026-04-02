package pe.resteban.library.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.resteban.library.infrastructure.config.AppConfig;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AppConfig")
class AppConfigTest {

    @Test
    @DisplayName("loads repository.type from application.properties")
    void getRepositoryType_returnsConfiguredValue() {
        AppConfig config = new AppConfig();
        // default in application.properties is "inmemory"
        assertEquals("inmemory", config.getRepositoryType());
    }

    @Test
    @DisplayName("getDbUrl() returns non-blank value")
    void getDbUrl_returnsNonBlank() {
        assertFalse(new AppConfig().getDbUrl().isBlank());
    }

    @Test
    @DisplayName("getDbUser() returns non-null value")
    void getDbUser_returnsNonNull() {
        assertNotNull(new AppConfig().getDbUser());
    }

    @Test
    @DisplayName("getDbPassword() returns non-null value")
    void getDbPassword_returnsNonNull() {
        assertNotNull(new AppConfig().getDbPassword());
    }
}
