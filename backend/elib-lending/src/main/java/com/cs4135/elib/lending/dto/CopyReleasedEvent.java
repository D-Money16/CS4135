package elib.lending.dto;

import lombok.Data;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
public class CopyReleasedEvent {
    private UUID copyId;
    private UUID loanId;
    private LocalDateTime returnedAt;
}
