package com.cs4135.elib.bookclub.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class MessageContent {

    private static final int MAX_LENGTH = 2000;

    @Column(name = "content", nullable = false, length = MAX_LENGTH)
    private String value;

    protected MessageContent() {}

    public MessageContent(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "Message content cannot exceed " + MAX_LENGTH + " characters (got " + value.length() + ")"
            );
        }
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageContent other)) return false;
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
