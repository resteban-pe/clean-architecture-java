# clean-architecture-java

![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?logo=apachemaven&logoColor=white)
![JaCoCo](https://img.shields.io/badge/Coverage-80%25%2B-brightgreen)
![License](https://img.shields.io/badge/License-MIT-yellow)

> A **library management system** that demonstrates **pure Clean Architecture in Java 21** —
> no Spring, no Lombok, no framework of any kind.
> Dependency injection is done by hand. The domain knows nothing about the outside world.

---

## Architecture

The dependency rule flows strictly inward. Outer layers depend on inner layers — never the reverse.

```
┌─────────────────────────────────────────────────────────────┐
│                          main/                              │
│       LibraryApp  (manual DI — demo or --cli mode)          │
│                            │                                │
│         ┌──────────────────▼──────────────────┐            │
│         │          infrastructure/             │            │
│         │  persistence/                        │            │
│         │   InMemoryBook/Member/LoanRepository │            │
│         │   JdbcBook/Member/LoanRepository     │            │
│         │  config/                             │            │
│         │   AppConfig  DataSourceFactory       │            │
│         │   RepositoryFactory                  │            │
│         │  cli/                                │            │
│         │   LibraryCLI  (primary adapter)      │            │
│         │                  │                   │            │
│         │    ┌─────────────▼────────────┐      │            │
│         │    │       application/       │      │            │
│         │    │  BookService             │      │            │
│         │    │  MemberService           │      │            │
│         │    │  LoanService             │      │            │
│         │    │           │              │      │            │
│         │    │  ┌────────▼──────────┐   │      │            │
│         │    │  │     domain/       │   │      │            │
│         │    │  │  Book  Member     │   │      │            │
│         │    │  │  Loan  BookId …   │   │      │            │
│         │    │  │  BookRepository   │   │      │            │
│         │    │  │  (port / owned    │   │      │            │
│         │    │  │   by domain)      │   │      │            │
│         │    │  └───────────────────┘   │      │            │
│         │    └──────────────────────────┘      │            │
│         └─────────────────────────────────────-┘            │
└─────────────────────────────────────────────────────────────┘
         Dependency arrow points INWARD only →
```

---

## Package structure

```
src/
├── main/
│   ├── java/pe/resteban/library/
│   │   ├── domain/
│   │   │   ├── model/
│   │   │   │   ├── Book.java              # Aggregate root
│   │   │   │   ├── Member.java            # Aggregate root
│   │   │   │   ├── Loan.java              # Entity
│   │   │   │   ├── BookId.java            # Value Object (record)
│   │   │   │   ├── MemberId.java          # Value Object (record)
│   │   │   │   ├── LoanId.java            # Value Object (record)
│   │   │   │   └── LoanStatus.java        # Enum: ACTIVE | RETURNED
│   │   │   ├── port/
│   │   │   │   ├── BookRepository.java    # Output port (owned by domain)
│   │   │   │   ├── MemberRepository.java
│   │   │   │   └── LoanRepository.java
│   │   │   └── exception/
│   │   │       ├── DomainException.java
│   │   │       ├── BookNotFoundException.java
│   │   │       ├── MemberNotFoundException.java
│   │   │       └── LoanNotFoundException.java
│   │   ├── application/
│   │   │   ├── usecase/
│   │   │   │   ├── BookUseCase.java       # Input port
│   │   │   │   ├── MemberUseCase.java
│   │   │   │   └── LoanUseCase.java
│   │   │   └── service/
│   │   │       ├── BookService.java
│   │   │       ├── MemberService.java
│   │   │       └── LoanService.java
│   │   ├── infrastructure/
│   │   │   ├── persistence/
│   │   │   │   ├── InMemoryBookRepository.java
│   │   │   │   ├── InMemoryMemberRepository.java
│   │   │   │   ├── InMemoryLoanRepository.java
│   │   │   │   ├── JdbcBookRepository.java
│   │   │   │   ├── JdbcMemberRepository.java
│   │   │   │   └── JdbcLoanRepository.java
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java         # Reads application.properties
│   │   │   │   ├── DataSourceFactory.java # H2 connection + schema bootstrap
│   │   │   │   └── RepositoryFactory.java # Selects InMemory or JDBC adapters
│   │   │   └── cli/
│   │   │       └── LibraryCLI.java        # Interactive console adapter
│   │   └── main/
│   │       └── LibraryApp.java            # Entry point — manual DI
│   └── resources/
│       ├── application.properties         # repository.type + db config
│       └── schema.sql                     # DDL for all 4 tables
└── test/java/pe/resteban/library/
    ├── domain/
    │   ├── BookTest.java
    │   ├── MemberTest.java
    │   ├── LoanTest.java
    │   ├── ValueObjectTest.java
    │   └── DomainExceptionTest.java
    ├── application/
    │   ├── BookServiceTest.java
    │   ├── MemberServiceTest.java
    │   └── LoanServiceTest.java
    └── infrastructure/
        ├── InMemoryBookRepositoryTest.java
        ├── InMemoryMemberRepositoryTest.java
        ├── InMemoryLoanRepositoryTest.java
        ├── JdbcTestBase.java              # Shared H2 setup for JDBC tests
        ├── JdbcBookRepositoryTest.java
        ├── JdbcMemberRepositoryTest.java
        ├── JdbcLoanRepositoryTest.java
        ├── AppConfigTest.java
        ├── RepositoryFactoryTest.java
        ├── LibraryIntegrationTest.java    # E2E — no mocks, real JDBC stack
        └── cli/
            └── LibraryCLITest.java        # Simulated console input
```

---

## Domain rules

These invariants are enforced inside the domain model — no service or controller can bypass them.

**1. A book can only be checked out when available**
```java
// Book.java
public void checkOut() {
    if (!available)
        throw new DomainException("Book '" + title + "' is not available for checkout");
    this.available = false;
}
```

**2. A member may hold at most 3 active loans**
```java
// Member.java
public static final int MAX_ACTIVE_LOANS = 3;

public void borrowBook(BookId bookId) {
    if (activeLoans.size() >= MAX_ACTIVE_LOANS)
        throw new DomainException("Member has reached the maximum of 3 active loans");
    activeLoans.add(bookId);
}
```

**3. A loan cannot be closed twice**
```java
// Loan.java
public void close(LocalDate returnDate) {
    if (this.status == LoanStatus.RETURNED)
        throw new DomainException("Loan id=" + id + " is already returned");
    this.returnDate = returnDate;
    this.status = LoanStatus.RETURNED;
}
```

---

## How to run

**Prerequisites:** Java 21, Maven 3.x

```bash
# Clone
git clone https://github.com/resteban-pe/clean-architecture-java.git
cd clean-architecture-java

# Compile
mvn compile

# Run all tests + JaCoCo coverage check (must pass 80% threshold)
# ⚠ application.properties must have repository.type=inmemory before running tests
mvn clean verify

# Run the automated demo (default)
mvn exec:java

# Run the interactive CLI (bash / Git Bash)
mvn exec:java -Dexec.args="--cli"

# Run the interactive CLI (PowerShell)
mvn exec:java "-Dexec.args=--cli"

# Open coverage report (after verify)
# target/site/jacoco/index.html
```

### Demo output

Running `mvn exec:java` (default `inmemory` mode) prints:

```
Repository type : inmemory

=== LIBRARY MANAGEMENT SYSTEM — demo ===
Book created : Book{id=..., title='Clean Architecture', author='Robert C. Martin', isbn='978-0134494166', available=true}
Book created : Book{id=..., title='Domain-Driven Design', author='Eric Evans', isbn='978-0321125217', available=true}
Member registered : Member{id=..., name='Roosevelt Torres', email='roosevelt@resteban.pe', activeLoans=0}

=== CREATE LOAN  (book1 → member) ===
Loan opened   : Loan{id=..., bookId=..., memberId=..., status=ACTIVE}
Book1 status  : available=false
Member loans  : 1 active

=== RETURN LOAN ===
Loan closed   : Loan{id=..., bookId=..., memberId=..., status=RETURNED}
Return date   : 2026-04-02

=== FINAL STATE ===
-- Books --
  Clean Architecture | available=true
  Domain-Driven Design | available=true
-- Members --
  Roosevelt Torres | active loans=0
-- Loans --
  loanId=... | status=RETURNED

=== END ===
```

### Persistence mode

Controlled by `src/main/resources/application.properties` — no code change required:

> **Important:** always restore `repository.type=inmemory` before running `mvn clean verify`.
> The test suite expects the default value to be `inmemory`.

```properties
# Use in-memory HashMaps (default — no DB needed, required for mvn clean verify)
repository.type=inmemory

# Use JDBC with embedded H2 (for manual runs only — change back before running tests)
repository.type=jdbc
db.url=jdbc:h2:mem:librarydb;DB_CLOSE_DELAY=-1
db.user=sa
db.password=
```

### CLI menu

```
── Books ─────────────────────────────
  1. List all books
  2. Add a book
  3. Delete a book
── Members ───────────────────────────
  4. List all members
  5. Register a member
── Loans ─────────────────────────────
  6. List all loans
  7. Create a loan
  8. Return a loan
──────────────────────────────────────
  0. Exit
```

---

## Test coverage

Results from `mvn clean verify` — JaCoCo 0.8.12, enforcement on `BUNDLE / INSTRUCTION`.

| Package | Tests | Notes |
|---|---|---|
| `domain.model` | 41 | Zero mocks — pure Java objects |
| `domain.exception` | 11 | Zero mocks |
| `application.service` | 34 | Mockito mocks output ports |
| `infrastructure.persistence` | 45 | InMemory + JDBC (H2) adapters |
| `infrastructure.config` | 6 | AppConfig + RepositoryFactory |
| `infrastructure.cli` | 16 | Simulated console input/output |
| `integration` | 6 | Real JDBC stack, zero mocks |
| **Total** | **159** | **≥ 80% ✓** |

> `LibraryApp.main()` is intentionally excluded from coverage — testing a wiring entry point that prints to stdout would add noise without value.

---

## Stack

| Tool | Version | Scope |
|---|---|---|
| Java | 21 | Language (`record`, switch expressions) |
| Maven | 3.x | Build |
| JUnit Jupiter | 5.10.2 | Testing |
| Mockito | 5.11.0 | Mocking (application layer tests only) |
| JaCoCo | 0.8.12 | Coverage enforcement |
| H2 | 2.2.224 | Embedded SQL database (runtime + test) |
| exec-maven-plugin | 3.3.0 | Run main without fat jar |

---

## Author

**Roosevelt Esteban Torres**
- GitHub: [github.com/resteban-pe](https://github.com/resteban-pe)
- LinkedIn: [linkedin.com/in/roosevelt-esteban](https://linkedin.com/in/roosevelt-esteban)

---

## License

This project is licensed under the [MIT License](LICENSE).
