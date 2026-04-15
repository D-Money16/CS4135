package com.cs4135.elib.lending;

import com.cs4135.elib.lending.infrastructure.LoanRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
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

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the Lending service.
 *
 * - Real Postgres via Testcontainers (no mocked DB)
 * - WireMock stubs for the Catalogue service (contract testing)
 * - Full Spring Boot context with real HTTP
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class LendingIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static WireMockServer catalogueStub;

    static final UUID USER_ID = UUID.randomUUID();
    static final UUID BOOK_ID = UUID.randomUUID();

    @LocalServerPort int port;
    @Autowired LoanRepository loanRepository;

    private RestClient client;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "lending");
        registry.add("spring.datasource.hikari.connection-init-sql", () -> "CREATE SCHEMA IF NOT EXISTS lending");
        registry.add("catalogue.service.url", () -> "http://localhost:" + catalogueStub.port());

        // Disable resilience4j retry/circuit-breaker for deterministic tests
        registry.add("resilience4j.retry.instances.catalogueService.max-attempts", () -> "1");
        registry.add("resilience4j.circuitbreaker.instances.catalogueService.sliding-window-size", () -> "100");
        registry.add("resilience4j.circuitbreaker.instances.catalogueService.failure-rate-threshold", () -> "100");
    }

    @BeforeAll
    static void startWireMock() {
        catalogueStub = new WireMockServer(0);
        catalogueStub.start();
        WireMock.configureFor("localhost", catalogueStub.port());
    }

    @AfterAll
    static void stopWireMock() {
        catalogueStub.stop();
    }

    @BeforeEach
    void setUp() {
        catalogueStub.resetAll();
        loanRepository.deleteAll();
        client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    // ── Helpers ─────────────────────────────────────────────

    private void stubReserveCopySuccess(UUID bookId, UUID copyId) {
        catalogueStub.stubFor(
                post(urlPathMatching("/api/book-catalogue/books/" + bookId + "/copies/reserve"))
                        .willReturn(okJson("{\"copyId\":\"" + copyId + "\"}")));
    }

    private void stubReleaseCopySuccess(UUID copyId) {
        catalogueStub.stubFor(
                post(urlPathMatching("/api/book-catalogue/copies/" + copyId + "/release"))
                        .willReturn(ok()));
    }

    private void stubReserveCopyDown() {
        catalogueStub.stubFor(
                post(urlPathMatching("/api/book-catalogue/books/.*/copies/reserve"))
                        .willReturn(aResponse().withStatus(500).withBody("Service down")));
    }

    private String borrowJson(UUID userId, UUID bookId) {
        return "{\"userId\":\"" + userId + "\",\"bookId\":\"" + bookId + "\"}";
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> borrow(UUID userId, UUID bookId) {
        return client.post()
                .uri("/api/lending/borrow")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(borrowJson(userId, bookId))
                .retrieve()
                .body(Map.class);
    }

    // ── Tests ───────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Borrow book — happy path, Lending calls Catalogue reserve endpoint")
    void borrowBook_happyPath() {
        UUID copyId = UUID.randomUUID();
        stubReserveCopySuccess(BOOK_ID, copyId);

        Map<String, Object> loan = borrow(USER_ID, BOOK_ID);

        assertThat(loan).containsEntry("userId", USER_ID.toString());
        assertThat(loan).containsEntry("bookId", BOOK_ID.toString());
        assertThat(loan).containsEntry("copyId", copyId.toString());
        assertThat(loan).containsEntry("status", "ACTIVE");

        // Verify Lending called the correct Catalogue contract
        catalogueStub.verify(1,
                postRequestedFor(urlPathEqualTo(
                        "/api/book-catalogue/books/" + BOOK_ID + "/copies/reserve")));

        // Verify loan persisted in real DB
        assertThat(loanRepository.findByUserId(USER_ID)).hasSize(1);
    }

    @Test
    @Order(2)
    @DisplayName("Return book — releases copy back to Catalogue")
    void returnBook_happyPath() {
        UUID copyId = UUID.randomUUID();
        stubReserveCopySuccess(BOOK_ID, copyId);

        Map<String, Object> loan = borrow(USER_ID, BOOK_ID);
        String loanId = (String) loan.get("id");

        stubReleaseCopySuccess(copyId);

        Map<String, Object> returned = client.post()
                .uri("/api/lending/loans/" + loanId + "/return")
                .retrieve()
                .body(Map.class);

        assertThat(returned).containsEntry("status", "RETURNED");

        // Verify Lending called Catalogue release endpoint
        catalogueStub.verify(1,
                postRequestedFor(urlPathEqualTo(
                        "/api/book-catalogue/copies/" + copyId + "/release")));
    }

    @Test
    @Order(3)
    @DisplayName("Double return — rejected with 400")
    void returnBook_alreadyReturned_rejected() {
        UUID copyId = UUID.randomUUID();
        stubReserveCopySuccess(BOOK_ID, copyId);
        stubReleaseCopySuccess(copyId);

        Map<String, Object> loan = borrow(USER_ID, BOOK_ID);
        String loanId = (String) loan.get("id");

        // First return succeeds
        client.post().uri("/api/lending/loans/" + loanId + "/return")
                .retrieve().body(Map.class);

        // Second return — expect 400
        ResponseEntity<String> resp = client.post()
                .uri("/api/lending/loans/" + loanId + "/return")
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(4)
    @DisplayName("Max 5 active loans — 6th borrow rejected with 422")
    void borrowBook_maxLoansReached() {
        for (int i = 0; i < 5; i++) {
            UUID copyId = UUID.randomUUID();
            stubReserveCopySuccess(BOOK_ID, copyId);
            borrow(USER_ID, BOOK_ID);
        }

        // 6th borrow rejected
        ResponseEntity<String> resp = client.post()
                .uri("/api/lending/borrow")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(borrowJson(USER_ID, BOOK_ID))
                .retrieve()
                .onStatus(s -> s.is4xxClientError(), (req, res) -> {})
                .toEntity(String.class);

        assertThat(resp.getStatusCode().value()).isEqualTo(422);

        // Catalogue should only have been called 5 times, not 6
        catalogueStub.verify(5,
                postRequestedFor(urlPathMatching("/api/book-catalogue/books/.*/copies/reserve")));
    }

    @Test
    @Order(5)
    @DisplayName("Catalogue down — borrow returns 503 (fallback)")
    void borrowBook_catalogueDown_fallback() {
        stubReserveCopyDown();

        ResponseEntity<String> resp = client.post()
                .uri("/api/lending/borrow")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(borrowJson(USER_ID, BOOK_ID))
                .retrieve()
                .onStatus(s -> s.isError(), (req, res) -> {})
                .toEntity(String.class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    @Order(6)
    @DisplayName("Get user loans — returns list of active loans")
    void getUserLoans() {
        UUID copyId = UUID.randomUUID();
        stubReserveCopySuccess(BOOK_ID, copyId);
        borrow(USER_ID, BOOK_ID);

        List<?> loans = client.get()
                .uri("/api/lending/loans/user/" + USER_ID)
                .retrieve()
                .body(List.class);

        assertThat(loans).hasSize(1);
    }

    @Test
    @Order(7)
    @DisplayName("Contract: Catalogue reserve returns copyId field — consumer contract test")
    void catalogueContract_reserveReturnsCorrectShape() {
        // Documents the exact JSON contract Lending expects from Catalogue's
        // POST /api/book-catalogue/books/{id}/copies/reserve endpoint.
        // If Catalogue changes the response field name, this test breaks.
        UUID copyId = UUID.randomUUID();

        catalogueStub.stubFor(
                post(urlPathMatching("/api/book-catalogue/books/.*/copies/reserve"))
                        .willReturn(okJson("{\"copyId\":\"" + copyId + "\"}")
                                .withHeader("Content-Type", "application/json")));

        Map<String, Object> loan = borrow(USER_ID, BOOK_ID);

        assertThat(loan).containsEntry("copyId", copyId.toString());
    }
}
