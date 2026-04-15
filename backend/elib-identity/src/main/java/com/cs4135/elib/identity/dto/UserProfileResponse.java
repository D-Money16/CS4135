package com.cs4135.elib.identity.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class UserProfileResponse {
    private UUID userId;
    private String fullName;
    private String role;
}
