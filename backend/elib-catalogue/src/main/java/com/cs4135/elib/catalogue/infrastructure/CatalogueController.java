package com.cs4135.elib.catalogue.infrastructure;

import com.cs4135.elib.catalogue.application.usecases.*;
import com.cs4135.elib.catalogue.domain.AvailabilityStatus;
import com.cs4135.elib.catalogue.domain.Book;
import com.cs4135.elib.catalogue.dto.CatalogueAvailabilityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/book-catalogue")
public class CatalogueController {

    @Autowired private AddBookUseCase addBookUseCase;
    @Autowired private CheckAvailabilityUseCase checkAvailabilityUseCase;
    @Autowired private ReserveBookCopyUseCase reserveBookCopyUseCase;
    @Autowired private ReleaseBookCopyUseCase releaseBookCopyUseCase;
    @Autowired private SearchBooksUseCase searchBooksUseCase;
    @Autowired private DeleteBookUseCase deleteBookUseCase;
    @Autowired private UpdateBookUseCase updateBookUseCase;

    @Autowired private RoleChecker roleChecker;

    @PostMapping("/books")
    public Book addBook(@RequestBody AddBookRequest request, jakarta.servlet.http.HttpServletRequest httpRequest) {
        roleChecker.requireRole(httpRequest, "ADMIN");
        return addBookUseCase.execute(
            request.isbn(), request.title(), request.description(),
            request.publicationYear(), request.copies(),
            request.authors(), request.categories()
        );
    }

    @GetMapping("/books")
    public List<Book> listBooks() {
        return addBookUseCase.findAll();
    }

    @GetMapping("/books/search")
    public List<Book> searchBooks(
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String author,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) AvailabilityStatus availability
    ) {
        return searchBooksUseCase.execute(title, author, category, availability);
    }

    @PutMapping("/books/{bookId}")
    public Book updateBook(@PathVariable UUID bookId, @RequestBody UpdateBookRequest request) {
        return updateBookUseCase.execute(
            bookId, request.title(), request.description(),
            request.publicationYear(), request.authors(), request.categories()
        );
    }

    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID bookId) {
        deleteBookUseCase.execute(bookId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/books/{bookId}/availability")
    public List<CatalogueAvailabilityResponse> checkAvailability(@PathVariable UUID bookId) {
        return checkAvailabilityUseCase.execute(bookId);
    }

    @GetMapping("/copies/{copyId}/availability")
    public CatalogueAvailabilityResponse checkCopyAvailability(@PathVariable UUID copyId) {
        return checkAvailabilityUseCase.executeForCopy(copyId);
    }

    @PostMapping("/books/{bookId}/copies/reserve")
    public ResponseEntity<ReservedCopyResponse> reserveCopy(@PathVariable UUID bookId) {
        UUID copyId = reserveBookCopyUseCase.execute(bookId);
        return ResponseEntity.ok(new ReservedCopyResponse(copyId));
    }

    @PostMapping("/copies/{copyId}/release")
    public ResponseEntity<Void> releaseCopy(@PathVariable UUID copyId) {
        releaseBookCopyUseCase.execute(copyId);
        return ResponseEntity.noContent().build();
    }

    record AddBookRequest(
        String isbn,
        String title,
        String description,
        Integer publicationYear,
        int copies,
        List<String> authors,
        List<String> categories
    ) {}

    record UpdateBookRequest(
        String title,
        String description,
        Integer publicationYear,
        List<String> authors,
        List<String> categories
    ) {}

    record ReservedCopyResponse(UUID copyId) {}
}
