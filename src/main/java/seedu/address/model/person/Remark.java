package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

/** Represents a remark for a person. */
public class Remark {
    public final String value;

    public Remark(String value) {
        requireNonNull(value);
        this.value = value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof Remark && value.equals(((Remark) other).value));
    }

    @Override
    public int hashCode() { return Objects.hash(value); }

    @Override
    public String toString() { return value; }
}
