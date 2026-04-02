package pe.resteban.library.domain.model;

import java.util.UUID;

/**
 * Value Object — identity of a Member.
 */
public record MemberId(UUID value) {

    public MemberId {
        if (value == null) throw new IllegalArgumentException("MemberId value must not be null");
    }

    public static MemberId generate() {
        return new MemberId(UUID.randomUUID());
    }

    public static MemberId of(String uuid) {
        return new MemberId(UUID.fromString(uuid));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
