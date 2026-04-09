package com.cs4135.elib.catalogue.application.usecases;

import com.cs4135.elib.catalogue.domain.AvailabilityStatus;
import com.cs4135.elib.catalogue.domain.BookCopy;
import com.cs4135.elib.catalogue.domain.InventoryService;
import com.cs4135.elib.catalogue.dto.CopyAvailabilityChangedEvent;
import com.cs4135.elib.catalogue.infrastructure.BookCopyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReleaseBookCopyUseCase {

    @Autowired
    private BookCopyRepository bookCopyRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public void execute(UUID copyId) {
        BookCopy copy = bookCopyRepository.findById(copyId)
            .orElseThrow(() -> new RuntimeException("Copy not found: " + copyId));

        UUID bookId = copy.getBook().getId();

        copy.transitionTo(AvailabilityStatus.AVAILABLE);
        bookCopyRepository.save(copy);

        inventoryService.updateAvailableCount(bookId);

        eventPublisher.publishEvent(
            new CopyAvailabilityChangedEvent(copyId, bookId, AvailabilityStatus.AVAILABLE)
        );
    }
}
