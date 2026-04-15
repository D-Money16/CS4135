package com.cs4135.elib.notification.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LoanOverdueEvent {
    private UUID loanId;
    private UUID userId;
    private UUID copyId;
    private String bookTitle;
    private LocalDate dueDate;
    private int daysOverdue;
    private LocalDateTime timestamp;
}