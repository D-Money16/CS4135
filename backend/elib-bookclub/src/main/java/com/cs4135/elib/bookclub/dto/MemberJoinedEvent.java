package elib.bookclub.dto;

import lombok.Data;
import java.util.UUID;
import java.time.LocalDateTime;

@Data
public class MemberJoinedEvent {
    private UUID clubId;
    private String clubName;
    private UUID userId;
    private LocalDateTime joinedAt;
}
