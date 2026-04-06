package elib.catalogue.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class BookAddedEvent {
    private UUID bookId;
    private String isbn;
    private String title;
    private int copies;
}
