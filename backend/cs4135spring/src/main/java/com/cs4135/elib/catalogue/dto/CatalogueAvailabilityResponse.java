package elib.catalogue.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class CatalogueAvailabilityResponse {
    private UUID copyId;
    private String status;
    private String bookTitle;
}
