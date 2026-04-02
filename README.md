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
│              LibraryApp  (manual wiring / DI)               │
│                            │                                │
│         ┌──────────────────▼──────────────────┐            │
│         │          infrastructure/             │            │
│         │   InMemoryBookRepository             │            │
│         │   InMemoryMemberRepository           │            │
│         │   InMemoryLoanRepository             │            │
│         │                  │                  │            │
│         │    ┌─────────────▼────────────┐     │            │
│         │    │       application/       │     │            │
│         │    │  BookService             │     │            │
│         │    │  MemberService           │     │            │
│         │    │  LoanService             │     │            │
│         │    │           │              │     │            │
│         │    │  ┌────────▼──────────┐   │     │            │
│         │    │  │     domain/       │   │     │            │
│         │    │  │  Book  Member     │   │     │            │
│         │    │  │  Loan  BookId … │   │     │            │
│         │    │  │  BookRepository   │   │     │            │
│         │    │  │  (port / owned    │   │     │            │
│         │    │  │   by domain)      │   │     │            │
│         │    │  └───────────────────┘   │     │            │
│         │    └──────────────────────────┘     │            │
│         └────────────────────────────────────-┘            │
└─────────────────────────────────────────────────────────────┘
         Dependency arrow points INWARD only →
```

---

## Package structure

```
src/
├── main/java/pe/resteban/library/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Book.java              # Aggregate root
│   │   │   ├── Member.java            # Aggregate root
│   │   │   ├── Loan.java              # Entity
│   │   │   ├── BookId.java            # Value Object (record)
│   │   │   ├── MemberId.java          # Value Object (record)
│   │   │   ├── LoanId.java            # Value Object (record)
│   │   │   └── LoanStatus.java        # Enum: ACTIVE | RETURNED
│   │   ├── port/
│   │   │   ├── BookRepository.java    # Output port (owned by domain)
│   │   │   ├── MemberRepository.java
│   │   │   └── LoanRepository.java
│   │   └── exception/
│   │       ├── DomainException.java   # Base unchecked exception
│   │       ├── BookNotFoundException.java
│   │       ├── MemberNotFoundException.java
│   │       └── LoanNotFoundException.java
│   ├── application/
│   │   ├── usecase/
│   │   │   ├── BookUseCase.java       # Input port
│   │   │   ├── MemberUseCase.java
│   │   │   └── LoanUseCase.java
│   │   └── service/
│   │       ├── BookService.java
│   │       ├── MemberService.java
│   │       └── LoanService.java
│   ├── infrastructure/
│   │   └── persistence/
│   │       ├── InMemoryBookRepository.java
│   │       ├── InMemoryMemberRepository.java
│   │       └── InMemoryLoanRepository.java
│   └── main/
│       └── LibraryApp.java            # Entry point — manual DI
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
        └── InMemoryLoanRepositoryTest.java
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

# Run all tests
mvn test

# Run tests + JaCoCo coverage check (must pass 80% threshold)
mvn clean verify

# Run the demo application
mvn exec:java

# Open coverage report (after verify)
# target/site/jacoco/index.html
```

---

## Test coverage

Results from `mvn clean verify` — JaCoCo 0.8.12, enforcement on `BUNDLE / INSTRUCTION`.

| Package | Tests | Coverage |
|---|---|---|
| `domain.model` | 41 | ~90% |
| `domain.exception` | 11 | ~100% |
| `application.service` | 34 | ~97% |
| `infrastructure.persistence` | 26 | ~100% |
| **Total** | **112** | **≥ 80% ✓** |

Tests use **JUnit 5** for all layers and **Mockito** only for the application layer (mocking output ports).
The domain layer is tested with zero mocks — pure Java objects only.

---

## Stack

| Tool | Version | Scope |
|---|---|---|
| Java | 21 | Language (`record`, pattern matching) |
| Maven | 3.x | Build |
| JUnit Jupiter | 5.10.2 | Testing |
| Mockito | 5.11.0 | Mocking (test scope) |
| JaCoCo | 0.8.12 | Coverage enforcement |
| exec-maven-plugin | 3.3.0 | Run main without fat jar |

---

## Roadmap

- [ ] JDBC repositories with embedded H2
- [ ] Configurable persistence via `application.properties` (`inmemory` / `jdbc`)
- [ ] Interactive CLI adapter
- [ ] End-to-end integration test (no mocks — real wiring)

---

## Author

**Roosevelt Esteban Torres**
- GitHub: [github.com/resteban-pe](https://github.com/resteban-pe)
- LinkedIn: [linkedin.com/in/roosevelt-esteban](https://linkedin.com/in/roosevelt-esteban)

---

## License

This project is licensed under the [MIT License](LICENSE).
