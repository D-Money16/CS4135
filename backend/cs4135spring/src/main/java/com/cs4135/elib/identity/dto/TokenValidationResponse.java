package elib.identity.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class TokenValidationResponse {
    private UUID userId;
    private String role;
    private boolean valid;
}
