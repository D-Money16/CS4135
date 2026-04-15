package com.cs4135.elib.lending.application.acl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class CatalogueClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogueClient.class);

    private final RestTemplate restTemplate;

    @Value("${catalogue.service.url}")
    private String catalogueUrl;

    public CatalogueClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "catalogueService", fallbackMethod = "reserveCopyFallback")
    @Retry(name = "catalogueService")
    public UUID reserveCopy(UUID bookId) {
        log.info("Attempting to reserve copy for book {}", bookId);
        ReservedCopyResponse response = restTemplate.postForObject(
            catalogueUrl + "/api/book-catalogue/books/{bookId}/copies/reserve",
            null,
            ReservedCopyResponse.class,
            bookId
        );
        if (response == null) {
            throw new RuntimeException("No available copies for book " + bookId);
        }
        return response.copyId();
    }

    @CircuitBreaker(name = "catalogueService", fallbackMethod = "releaseCopyFallback")
    @Retry(name = "catalogueService")
    public void releaseCopy(UUID copyId) {
        log.info("Attempting to release copy {}", copyId);
        restTemplate.postForObject(
            catalogueUrl + "/api/book-catalogue/copies/{copyId}/release",
            null,
            Void.class,
            copyId
        );
    }

    private UUID reserveCopyFallback(UUID bookId, Throwable t) {
        log.error("Circuit breaker triggered for reserveCopy. Book: {}, Reason: {}", bookId, t.getMessage());
        throw new CatalogueServiceUnavailableException(
            "Catalogue service is currently unavailable. Please try again later.");
    }

    private void releaseCopyFallback(UUID copyId, Throwable t) {
        log.error("Circuit breaker triggered for releaseCopy. Copy: {}, Reason: {}", copyId, t.getMessage());
        throw new CatalogueServiceUnavailableException(
            "Catalogue service is currently unavailable. The copy release will be retried.");
    }

    private record ReservedCopyResponse(UUID copyId) {}
}
