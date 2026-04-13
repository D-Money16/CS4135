package com.cs4135.elib.catalogue.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "authors")
@Getter
@NoArgsConstructor
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    public Author(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Author name must not be blank");
        }
        this.name = name;
    }
}
