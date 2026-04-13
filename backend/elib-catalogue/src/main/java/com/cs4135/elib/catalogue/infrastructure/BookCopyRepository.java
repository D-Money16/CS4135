package com.cs4135.elib.catalogue.infrastructure;

import com.cs4135.elib.catalogue.domain.AvailabilityStatus;
import com.cs4135.elib.catalogue.domain.BookCopy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BookCopyRepository extends JpaRepository<BookCopy, UUID> {

    List<BookCopy> findByBookId(UUID bookId);

    List<BookCopy> findByBookIdAndStatus(UUID bookId, AvailabilityStatus status);

    default List<BookCopy> findAvailableCopiesByBookId(UUID bookId) {
        return findByBookIdAndStatus(bookId, AvailabilityStatus.AVAILABLE);
    }

    long countByBookIdAndStatus(UUID bookId, AvailabilityStatus status);

    long countByBookId(UUID bookId);
}
