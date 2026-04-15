package com.cs4135.elib.identity.dto;

public class AuthResponse {
    public String token;
    public String userId;

    public AuthResponse(String token, String userId) {
        this.token = token;
        this.userId = userId;
    }
}
