package nl.teamrocket.identity.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object: Email address.
 * Immutable; equality is by value. Validated at construction.
 * Invariant: must match RFC 5322 simplified pattern.
 */
public final class Email {

    private static final Pattern RFC_5322 =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final String value;

    public Email(String value) {
        Objects.requireNonNull(value, "Email must not be null");
        String trimmed = value.trim().toLowerCase();
        if (!RFC_5322.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email address: " + value);
        }
        this.value = trimmed;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email email)) return false;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
