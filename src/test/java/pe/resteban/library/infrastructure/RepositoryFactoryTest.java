package pe.resteban.library.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.resteban.library.infrastructure.config.AppConfig;
import pe.resteban.library.infrastructure.config.RepositoryFactory;
import pe.resteban.library.infrastructure.persistence.InMemoryBookRepository;
import pe.resteban.library.infrastructure.persistence.InMemoryLoanRepository;
import pe.resteban.library.infrastructure.persistence.InMemoryMemberRepository;
import pe.resteban.library.infrastructure.persistence.JdbcBookRepository;
import pe.resteban.library.infrastructure.persistence.JdbcLoanRepository;
import pe.resteban.library.infrastructure.persistence.JdbcMemberRepository;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RepositoryFactory")
class RepositoryFactoryTest {

    @Test
    @DisplayName("returns InMemory* repositories when type=inmemory")
    void inmemory_returnsInMemoryAdapters() {
        // application.properties default is inmemory
        RepositoryFactory factory = new RepositoryFactory(new AppConfig());

        assertInstanceOf(InMemoryBookRepository.class,   factory.bookRepository());
        assertInstanceOf(InMemoryMemberRepository.class, factory.memberRepository());
        assertInstanceOf(InMemoryLoanRepository.class,   factory.loanRepository());
    }

    @Test
    @DisplayName("returns Jdbc* repositories when type=jdbc")
    void jdbc_returnsJdbcAdapters() {
        AppConfig jdbcConfig = new AppConfig() {
            @Override public String getRepositoryType() { return "jdbc"; }
            @Override public String getDbUrl()  { return "jdbc:h2:mem:factorytest;DB_CLOSE_DELAY=-1"; }
            @Override public String getDbUser() { return "sa"; }
            @Override public String getDbPassword() { return ""; }
        };
        // Create factory with a DataSourceFactory that also runs the schema
        RepositoryFactory factory = new RepositoryFactory(jdbcConfig) {
            // Override connection so schema runs on the test DB
            {
                var dsf = new pe.resteban.library.infrastructure.config.DataSourceFactory(jdbcConfig);
                var conn = dsf.getConnection();
                JdbcTestBase.runSchemaOn(conn);
            }
        };

        assertInstanceOf(JdbcBookRepository.class,   factory.bookRepository());
        assertInstanceOf(JdbcMemberRepository.class, factory.memberRepository());
        assertInstanceOf(JdbcLoanRepository.class,   factory.loanRepository());
    }
}
