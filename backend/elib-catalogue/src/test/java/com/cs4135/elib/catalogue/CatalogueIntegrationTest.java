package com.cs4135.elib.catalogue;

import com.cs4135.elib.catalogue.infrastructure.BookCopyRepository;
import com.cs4135.elib.catalogue.infrastructure.BookRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the Catalogue service.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CatalogueIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @LocalServerPort int port;
    @Autowired BookRepository bookRepository;
    @Autowired BookCopyRepository bookCopyRepository;

    private RestClient client;

    static UUID createdBookId;
    static UUID createdCopyId;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "catalogue");
        registry.add("spring.datasource.hikari.connection-init-sql", () -> "CREATE SCHEMA IF NOT EXISTS catalogue");
        registry.add("eureka.client.enabled", () -> "false");
    }

    @BeforeEach
    void setUp() {
        client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    // ── Helpers ─────────────────────────────────────────────

    private String addBookJson(String isbn, String title, int copies) {
        return """
            {
              "isbn": "%s",
              "title": "%s",
              "description": "A test book",
              "publicationYear": 2024,
              "copies": %d,
              "authors": ["Test Author"],
              "categories": ["Fiction"]
            }
            """.formatted(isbn, title, copies);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> addBook(String isbn, String title, int copies) {
        return client.post()
                .uri("/api/book-catalogue/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body(addBookJson(isbn, title, copies))
                .retrieve()
                .body(Map.class);
    }

    // ── Tests ───────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Add book — happy path, book and copies persisted")
    void addBook_happyPath() {
        Map<String, Object> book = addBook("978-3-16-148410-0", "Integration Test Book", 3);

        assertThat(book).containsKey("id");
        assertThat(book).containsEntry("title", "Integration Test Book");
        assertThat(book).containsEntry("totalCopies", 3);
        assertThat(book).containsEntry("availableCopies", 3);

        createdBookId = UUID.fromString((String) book.get("id"));

        assertThat(bookRepository.findById(createdBookId)).isPresent();
        assertThat(bookCopyRepository.findAvailableCopiesByBookId(createdBookId)).hasSize(3);
    }

    @Test
    @Order(2)
    @DisplayName("List all books — returns the added book")
    void listBooks_returnsAddedBook() {
        List<?> books = client.get()
                .uri("/api/book-catalogue/books")
                .retrieve()
                .body(List.class);

        assertThat(books).isNotEmpty();
        assertThat(books).anyMatch(b -> ((Map<?, ?>) b).get("id").equals(createdBookId.toString()));
    }

    @Test
    @Order(3)
    @DisplayName("Search by title — finds matching book")
    void searchBooks_byTitle() {
        List<?> results = client.get()
                .uri("/api/book-catalogue/books/search?title=Integration+Test")
                .retrieve()
                .body(List.class);

        assertThat(results).hasSize(1);
        assertThat(((Map<?, ?>) results.getFirst()).get("id")).isEqualTo(createdBookId.toString());
    }

    @Test
    @Order(4)
    @DisplayName("Search by author — finds matching book")
    void searchBooks_byAuthor() {
        List<?> results = client.get()
                .uri("/api/book-catalogue/books/search?author=Test+Author")
                .retrieve()
                .body(List.class);

        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(b -> ((Map<?, ?>) b).get("id").equals(createdBookId.toString()));
    }

    @Test
    @Order(5)
    @DisplayName("Search by category — finds matching book")
    void searchBooks_byCategory() {
        List<?> results = client.get()
                .uri("/api/book-catalogue/books/search?category=Fiction")
                .retrieve()
                .body(List.class);

        assertThat(results).isNotEmpty();
        assertThat(results).anyMatch(b -> ((Map<?, ?>) b).get("id").equals(createdBookId.toString()));
    }

    @Test
    @Order(6)
    @DisplayName("Duplicate ISBN — rejected")
    void addBook_duplicateIsbn_rejected() {
        ResponseEntity<String> resp = client.post()
                .uri("/api/book-catalogue/books")
                .contentType(MediaType.APPLICATION_JSON)
                .body(addBookJson("978-3-16-148410-0", "Duplicate ISBN Book", 1))
                .retrieve()
                .onStatus(s -> s.isError(), (req, res) -> {})
                .toEntity(String.class);

        assertThat(resp.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @Order(7)
    @DisplayName("Update book — title and description changed")
    @SuppressWarnings("unchecked")
    void updateBook_happyPath() {
        String updateJson = """
            {
              "title": "Updated Title",
              "description": "Updated description",
              "publicationYear": 2025,
              "authors": ["Updated Author"],
              "categories": ["Non-Fiction"]
            }
            """;

        Map<String, Object> updated = client.put()
                .uri("/api/book-catalogue/books/" + createdBookId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(updateJson)
                .retrieve()
                .body(Map.class);

        assertThat(updated).containsEntry("title", "Updated Title");
    }

    @Test
    @Order(8)
    @DisplayName("Check availability — returns copy statuses for book")
    void checkAvailability_happyPath() {
        List<?> availability = client.get()
                .uri("/api/book-catalogue/books/" + createdBookId + "/availability")
                .retrieve()
                .body(List.class);

        assertThat(availability).hasSize(3);
        assertThat(availability).allMatch(a -> "AVAILABLE".equals(((Map<?, ?>) a).get("status")));
    }

    @Test
    @Order(9)
    @DisplayName("Reserve copy — happy path, returns copyId and decrements availableCopies")
    @SuppressWarnings("unchecked")
    void reserveCopy_happyPath() {
        Map<String, Object> reserved = client.post()
                .uri("/api/book-catalogue/books/" + createdBookId + "/copies/reserve")
                .retrieve()
                .body(Map.class);

        assertThat(reserved).containsKey("copyId");
        createdCopyId = UUID.fromString((String) reserved.get("copyId"));

        assertThat(bookCopyRepository.findAvailableCopiesByBookId(createdBookId)).hasSize(2);
    }

    @Test
    @Order(10)
    @DisplayName("Check copy availability — borrowed copy shows BORROWED status")
    void checkCopyAvailability_borrowedCopy() {
        @SuppressWarnings("unchecked")
        Map<String, Object> availability = client.get()
                .uri("/api/book-catalogue/copies/" + createdCopyId + "/availability")
                .retrieve()
                .body(Map.class);

        assertThat(availability).containsEntry("status", "BORROWED");
    }

    @Test
    @Order(11)
    @DisplayName("Release copy — copy becomes AVAILABLE again")
    void releaseCopy_happyPath() {
        ResponseEntity<Void> resp = client.post()
                .uri("/api/book-catalogue/copies/" + createdCopyId + "/release")
                .retrieve()
                .toBodilessEntity();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(bookCopyRepository.findAvailableCopiesByBookId(createdBookId)).hasSize(3);
    }

    @Test
    @Order(12)
    @DisplayName("Reserve copy — no copies available returns 500")
    void reserveCopy_noCopiesAvailable() {
        // Exhaust all copies
        for (int i = 0; i < 3; i++) {
            client.post()
                    .uri("/api/book-catalogue/books/" + createdBookId + "/copies/reserve")
                    .retrieve()
                    .body(Map.class);
        }

        ResponseEntity<String> resp = client.post()
                .uri("/api/book-catalogue/books/" + createdBookId + "/copies/reserve")
                .retrieve()
                .onStatus(s -> s.isError(), (req, res) -> {})
                .toEntity(String.class);

        assertThat(resp.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @Order(13)
    @DisplayName("Delete book with borrowed copies — rejected")
    void deleteBook_withBorrowedCopies_rejected() {
        // One copy is still borrowed from the previous test
        ResponseEntity<String> resp = client.delete()
                .uri("/api/book-catalogue/books/" + createdBookId)
                .retrieve()
                .onStatus(s -> s.isError(), (req, res) -> {})
                .toEntity(String.class);

        assertThat(resp.getStatusCode().is5xxServerError()).isTrue();
    }

    @Test
    @Order(14)
    @DisplayName("Delete book — soft-deletes when no copies are borrowed")
    void deleteBook_happyPath() {
        // Add a fresh book to delete cleanly
        Map<String, Object> book = addBook("978-0-00-000001-0", "Book To Delete", 1);
        UUID bookToDeleteId = UUID.fromString((String) book.get("id"));

        ResponseEntity<Void> resp = client.delete()
                .uri("/api/book-catalogue/books/" + bookToDeleteId)
                .retrieve()
                .toBodilessEntity();

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        assertThat(bookRepository.findById(bookToDeleteId))
                .isPresent()
                .hasValueSatisfying(b -> assertThat(b.isDeleted()).isTrue());

        List<?> books = client.get()
                .uri("/api/book-catalogue/books")
                .retrieve()
                .body(List.class);
        assertThat(books).noneMatch(b -> ((Map<?, ?>) b).get("id").equals(bookToDeleteId.toString()));
    }
}
