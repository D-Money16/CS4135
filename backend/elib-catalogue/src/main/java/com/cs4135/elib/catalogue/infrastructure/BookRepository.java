package com.cs4135.elib.catalogue.infrastructure;

import com.cs4135.elib.catalogue.domain.AvailabilityStatus;
import com.cs4135.elib.catalogue.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    @Query("SELECT b FROM Book b WHERE b.isbn.value = :isbnValue AND b.deletedAt IS NULL")
    Optional<Book> findByIsbnValue(@Param("isbnValue") String isbnValue);

    List<Book> findAllByDeletedAtIsNull();

    @Query("""
        SELECT DISTINCT b FROM Book b
        LEFT JOIN b.authors a
        LEFT JOIN b.categories c
        WHERE b.deletedAt IS NULL
        AND (CAST(:title AS string) IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', CAST(:title AS string), '%')))
        AND (CAST(:authorName AS string) IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', CAST(:authorName AS string), '%')))
        AND (CAST(:categoryName AS string) IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:categoryName AS string), '%')))
        AND (:status IS NULL OR EXISTS (
            SELECT bc FROM BookCopy bc WHERE bc.book = b AND bc.status = :status
        ))
        """)
    List<Book> searchBooks(
        @Param("title") String title,
        @Param("authorName") String authorName,
        @Param("categoryName") String categoryName,
        @Param("status") AvailabilityStatus status
    );
}
