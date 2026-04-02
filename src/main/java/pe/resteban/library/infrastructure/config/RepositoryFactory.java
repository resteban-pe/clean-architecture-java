package pe.resteban.library.infrastructure.config;

import pe.resteban.library.domain.port.BookRepository;
import pe.resteban.library.domain.port.LoanRepository;
import pe.resteban.library.domain.port.MemberRepository;
import pe.resteban.library.infrastructure.persistence.InMemoryBookRepository;
import pe.resteban.library.infrastructure.persistence.InMemoryLoanRepository;
import pe.resteban.library.infrastructure.persistence.InMemoryMemberRepository;
import pe.resteban.library.infrastructure.persistence.JdbcBookRepository;
import pe.resteban.library.infrastructure.persistence.JdbcLoanRepository;
import pe.resteban.library.infrastructure.persistence.JdbcMemberRepository;

import java.sql.Connection;

/**
 * Factory that reads {@code repository.type} from {@link AppConfig} and
 * instantiates the correct set of repository adapters.
 *
 * <p>Switching persistence from in-memory to JDBC requires only a one-line
 * change in {@code application.properties}. No production code is modified.
 *
 * <pre>
 *   repository.type=inmemory   →  InMemory* adapters (default)
 *   repository.type=jdbc       →  Jdbc*     adapters + H2
 * </pre>
 */
public class RepositoryFactory {

    private final AppConfig config;
    private final DataSourceFactory dataSourceFactory;

    public RepositoryFactory(AppConfig config) {
        this.config            = config;
        this.dataSourceFactory = new DataSourceFactory(config);
    }

    public BookRepository bookRepository() {
        return switch (config.getRepositoryType()) {
            case "jdbc" -> new JdbcBookRepository(connection());
            default     -> new InMemoryBookRepository();
        };
    }

    public MemberRepository memberRepository() {
        return switch (config.getRepositoryType()) {
            case "jdbc" -> new JdbcMemberRepository(connection());
            default     -> new InMemoryMemberRepository();
        };
    }

    public LoanRepository loanRepository() {
        return switch (config.getRepositoryType()) {
            case "jdbc" -> new JdbcLoanRepository(connection());
            default     -> new InMemoryLoanRepository();
        };
    }

    private Connection connection() {
        return dataSourceFactory.getConnection();
    }
}
