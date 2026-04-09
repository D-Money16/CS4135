package com.cs4135.elib.catalogue.application.usecases;

import com.cs4135.elib.catalogue.domain.AvailabilityStatus;
import com.cs4135.elib.catalogue.domain.Book;
import com.cs4135.elib.catalogue.infrastructure.BookCopyRepository;
import com.cs4135.elib.catalogue.infrastructure.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteBookUseCase {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Transactional
    public void execute(UUID bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));

        if (book.isDeleted()) {
            throw new IllegalStateException("Book is already archived: " + bookId);
        }

        long borrowedCount = bookCopyRepository.countByBookIdAndStatus(bookId, AvailabilityStatus.BORROWED);
        long reservedCount = bookCopyRepository.countByBookIdAndStatus(bookId, AvailabilityStatus.RESERVED);
        if (borrowedCount > 0 || reservedCount > 0) {
            throw new IllegalStateException(
                "Cannot archive book " + bookId + ": " + borrowedCount + " copy/copies are currently borrowed " +
                "and " + reservedCount + " copy/copies are reserved. Return all copies before deleting."
            );
        }

        book.softDelete();
        bookRepository.save(book);
    }
}
