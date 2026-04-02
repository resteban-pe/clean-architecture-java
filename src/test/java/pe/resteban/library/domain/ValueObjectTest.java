package pe.resteban.library.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import pe.resteban.library.domain.model.BookId;
import pe.resteban.library.domain.model.LoanId;
import pe.resteban.library.domain.model.MemberId;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Value Objects")
class ValueObjectTest {

    @Nested
    @DisplayName("BookId")
    class BookIdTest {

        @Test
        @DisplayName("generate() produces a non-null id")
        void generate_returnsNonNull() {
            assertNotNull(BookId.generate());
        }

        @Test
        @DisplayName("two generate() calls produce different ids")
        void generate_producesUniqueIds() {
            assertNotEquals(BookId.generate(), BookId.generate());
        }

        @Test
        @DisplayName("of(String) round-trips through toString()")
        void of_roundTrip() {
            BookId original = BookId.generate();
            BookId parsed   = BookId.of(original.toString());
            assertEquals(original, parsed);
        }

        @Test
        @DisplayName("of(String) rejects malformed UUID")
        void of_malformedUuid_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> BookId.of("not-a-uuid"));
        }

        @Test
        @DisplayName("constructor rejects null UUID")
        void constructor_nullValue_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> new BookId(null));
        }

        @Test
        @DisplayName("record equality: same UUID → equal")
        void equality_sameUuid() {
            UUID uuid = UUID.randomUUID();
            assertEquals(new BookId(uuid), new BookId(uuid));
        }

        @Test
        @DisplayName("toString() returns the UUID string")
        void toString_returnsUuidString() {
            UUID uuid = UUID.randomUUID();
            assertEquals(uuid.toString(), new BookId(uuid).toString());
        }
    }

    @Nested
    @DisplayName("MemberId")
    class MemberIdTest {

        @Test
        @DisplayName("generate() produces a non-null id")
        void generate_returnsNonNull() {
            assertNotNull(MemberId.generate());
        }

        @Test
        @DisplayName("of(String) round-trips through toString()")
        void of_roundTrip() {
            MemberId original = MemberId.generate();
            MemberId parsed   = MemberId.of(original.toString());
            assertEquals(original, parsed);
        }

        @Test
        @DisplayName("constructor rejects null UUID")
        void constructor_nullValue_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> new MemberId(null));
        }
    }

    @Nested
    @DisplayName("LoanId")
    class LoanIdTest {

        @Test
        @DisplayName("generate() produces a non-null id")
        void generate_returnsNonNull() {
            assertNotNull(LoanId.generate());
        }

        @Test
        @DisplayName("of(String) round-trips through toString()")
        void of_roundTrip() {
            LoanId original = LoanId.generate();
            LoanId parsed   = LoanId.of(original.toString());
            assertEquals(original, parsed);
        }

        @Test
        @DisplayName("constructor rejects null UUID")
        void constructor_nullValue_throwsIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> new LoanId(null));
        }
    }
}
