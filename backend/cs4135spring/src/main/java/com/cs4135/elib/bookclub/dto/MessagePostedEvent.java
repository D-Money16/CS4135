package elib.bookclub.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class MessagePostedEvent {
    private UUID messageId;
    private UUID clubId;
    private UUID authorId;
}
