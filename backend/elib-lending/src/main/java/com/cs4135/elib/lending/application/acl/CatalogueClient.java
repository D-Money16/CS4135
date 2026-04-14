package com.cs4135.elib.lending.application.acl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class CatalogueClient {

    private final RestTemplate restTemplate;

    @Value("${catalogue.service.url}")
    private String catalogueUrl;

    public CatalogueClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UUID reserveCopy(UUID bookId) {
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

    public void releaseCopy(UUID copyId) {
        restTemplate.postForObject(
            catalogueUrl + "/api/book-catalogue/copies/{copyId}/release",
            null,
            Void.class,
            copyId
        );
    }

    private record ReservedCopyResponse(UUID copyId) {}
}
