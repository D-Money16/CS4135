package elib.identity.dto;

import lombok.Data;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
public class UserRegisteredEvent {
    private UUID userId;
    private String email;
    private String fullName;
    private LocalDateTime timestamp;
}
