package com.cs4135.elib.catalogue.infrastructure;

import com.cs4135.elib.catalogue.domain.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthorRepository extends JpaRepository<Author, UUID> {
    Optional<Author> findByNameIgnoreCase(String name);
}
