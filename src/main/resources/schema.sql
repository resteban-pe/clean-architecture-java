-- ── Books ────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS books (
    id        VARCHAR(36)  NOT NULL PRIMARY KEY,
    title     VARCHAR(255) NOT NULL,
    author    VARCHAR(255) NOT NULL,
    isbn      VARCHAR(50)  NOT NULL,
    available BOOLEAN      NOT NULL DEFAULT TRUE
);

-- ── Members ───────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS members (
    id    VARCHAR(36)  NOT NULL PRIMARY KEY,
    name  VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
);

-- Active loans held by a member (denormalised list of BookId UUIDs)
CREATE TABLE IF NOT EXISTS member_active_loans (
    member_id VARCHAR(36) NOT NULL,
    book_id   VARCHAR(36) NOT NULL,
    PRIMARY KEY (member_id, book_id),
    FOREIGN KEY (member_id) REFERENCES members(id)
);

-- ── Loans ─────────────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS loans (
    id          VARCHAR(36) NOT NULL PRIMARY KEY,
    book_id     VARCHAR(36) NOT NULL,
    member_id   VARCHAR(36) NOT NULL,
    loan_date   DATE        NOT NULL,
    return_date DATE,
    status      VARCHAR(10) NOT NULL DEFAULT 'ACTIVE'
);
