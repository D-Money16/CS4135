package com.cs4135.elib.catalogue.application.usecases;

import com.cs4135.elib.catalogue.domain.Book;
import com.cs4135.elib.catalogue.domain.BookCopy;
import com.cs4135.elib.catalogue.dto.CatalogueAvailabilityResponse;
import com.cs4135.elib.catalogue.infrastructure.BookCopyRepository;
import com.cs4135.elib.catalogue.infrastructure.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CheckAvailabilityUseCase {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCopyRepository bookCopyRepository;

    public List<CatalogueAvailabilityResponse> execute(UUID bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Book not found: " + bookId));

        return bookCopyRepository.findByBookId(bookId).stream()
            .map(copy -> toResponse(copy, book.getTitle()))
            .toList();
    }

    public CatalogueAvailabilityResponse executeForCopy(UUID copyId) {
        BookCopy copy = bookCopyRepository.findById(copyId)
            .orElseThrow(() -> new RuntimeException("Copy not found: " + copyId));
        return toResponse(copy, copy.getBook().getTitle());
    }

    private CatalogueAvailabilityResponse toResponse(BookCopy copy, String bookTitle) {
        CatalogueAvailabilityResponse response = new CatalogueAvailabilityResponse();
        response.setCopyId(copy.getId());
        response.setStatus(copy.getStatus().name());
        response.setBookTitle(bookTitle);
        return response;
    }
}
