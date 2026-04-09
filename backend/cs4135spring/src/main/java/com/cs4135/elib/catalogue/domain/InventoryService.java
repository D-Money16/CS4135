package com.cs4135.elib.catalogue.domain;

import com.cs4135.elib.catalogue.infrastructure.BookCopyRepository;
import com.cs4135.elib.catalogue.infrastructure.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class InventoryService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Transactional
    public void updateAvailableCount(UUID bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));

        long available = bookCopyRepository.countByBookIdAndStatus(bookId, AvailabilityStatus.AVAILABLE);
        long total = bookCopyRepository.countByBookId(bookId);

        if (available > total) {
            throw new IllegalStateException(
                "Invariant violation for book " + bookId +
                ": availableCopies (" + available + ") exceeds totalCopies (" + total + ")."
            );
        }

        book.setAvailableCopies((int) available);
        book.setTotalCopies((int) total);
        bookRepository.save(book);
    }
}
