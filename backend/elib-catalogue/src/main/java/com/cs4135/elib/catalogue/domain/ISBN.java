package com.cs4135.elib.catalogue.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class ISBN {

    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String value;

    protected ISBN() {}

    public ISBN(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("ISBN must not be blank");
        }
        String digits = raw.replaceAll("[-\\s]", "");
        if (digits.length() == 10) {
            if (!digits.matches("\\d{9}[\\dX]")) {
                throw new IllegalArgumentException("Invalid ISBN-10 format: " + raw);
            }
        } else if (digits.length() == 13) {
            if (!digits.matches("\\d{13}")) {
                throw new IllegalArgumentException("Invalid ISBN-13 format: " + raw);
            }
        } else {
            throw new IllegalArgumentException("ISBN must be 10 or 13 digits (got " + digits.length() + "): " + raw);
        }
        this.value = raw;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ISBN other)) return false;
        return Objects.equals(value, other.value);
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
