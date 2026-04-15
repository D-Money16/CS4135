package com.cs4135.elib.lending.application.acl;

import com.cs4135.elib.lending.dto.LoanCreatedEvent;
import com.cs4135.elib.lending.dto.LoanOverdueEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Component
public class NotificationClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl;

    public NotificationClient(@Value("${notification.service.url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void sendLoanCreatedNotification(LoanCreatedEvent event) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoanCreatedEvent> request = new HttpEntity<>(event, headers);
        restTemplate.postForEntity(baseUrl + "/api/notifications/lending/loan-created", request, Void.class);
    }

    public void sendLoanOverdueNotification(LoanOverdueEvent event) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<LoanOverdueEvent> request = new HttpEntity<>(event, headers);
        restTemplate.postForEntity(baseUrl + "/api/notifications/lending/loan-overdue", request, Void.class);
    }

    public void sendDirectNotification(UUID userId, UUID referenceId, String type, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "userId", userId,
                "referenceId", referenceId,
                "type", type,
                "message", message,
                "source", "LENDING"
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(baseUrl + "/api/notifications/lending/direct", request, Void.class);
    }
}