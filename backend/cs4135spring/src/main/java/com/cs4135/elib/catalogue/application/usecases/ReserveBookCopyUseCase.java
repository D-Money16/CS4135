package com.cs4135.elib.catalogue.application.usecases;

import com.cs4135.elib.catalogue.domain.AvailabilityStatus;
import com.cs4135.elib.catalogue.domain.BookCopy;
import com.cs4135.elib.catalogue.domain.InventoryService;
import com.cs4135.elib.catalogue.dto.CopyAvailabilityChangedEvent;
import com.cs4135.elib.catalogue.infrastructure.BookCopyRepository;
import com.cs4135.elib.catalogue.infrastructure.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReserveBookCopyUseCase {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public UUID execute(UUID bookId) {
        bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));

        List<BookCopy> available = bookCopyRepository.findAvailableCopiesByBookId(bookId);
        if (available.isEmpty()) {
            throw new RuntimeException("No available copies for book: " + bookId);
        }

        BookCopy copy = available.getFirst();
        copy.transitionTo(AvailabilityStatus.BORROWED);
        bookCopyRepository.save(copy);

        inventoryService.updateAvailableCount(bookId);

        eventPublisher.publishEvent(
            new CopyAvailabilityChangedEvent(copy.getId(), bookId, AvailabilityStatus.BORROWED)
        );

        return copy.getId();
    }
}
