package com.cs4135.elib.catalogue.infrastructure;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Component
public class RoleChecker {

    private final JwtUtil jwtUtil;

    public RoleChecker(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public void requireRole(HttpServletRequest request, String... allowed) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = header.substring(7);
        Claims claims;
        try {
            claims = jwtUtil.getClaims(token);
        } catch (RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        String role = claims.get("role", String.class);
        if (role == null || Arrays.stream(allowed).noneMatch(role::equals)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role");
        }
    }
}
