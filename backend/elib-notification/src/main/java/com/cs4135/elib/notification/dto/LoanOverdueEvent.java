package com.cs4135.elib.notification.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class LoanOverdueEvent {
    private UUID loanId;
    private UUID userId;
    private UUID copyId;
    private String bookTitle;
    private LocalDate dueDate;
    private int daysOverdue;
    private LocalDateTime timestamp;

    public UUID getLoanId() {
        return loanId;
    }

    public void setLoanId(UUID loanId) {
        this.loanId = loanId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getCopyId() {
        return copyId;
    }

    public void setCopyId(UUID copyId) {
        this.copyId = copyId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getDaysOverdue() {
        return daysOverdue;
    }

    public void setDaysOverdue(int daysOverdue) {
        this.daysOverdue = daysOverdue;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}