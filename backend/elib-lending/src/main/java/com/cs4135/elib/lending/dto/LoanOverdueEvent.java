package elib.lending.dto;

import lombok.Data;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
