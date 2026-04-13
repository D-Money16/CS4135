package elib.bookclub.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class BookClubCreatedEvent {
    private UUID clubId;
    private String name;
    private UUID ownerId;
}
