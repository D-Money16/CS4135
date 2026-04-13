package com.cs4135.elib.identity.application.usecases;

import com.cs4135.elib.identity.domain.User;
import com.cs4135.elib.identity.dto.AuthResponse;
import com.cs4135.elib.identity.dto.LoginRequest;
import com.cs4135.elib.identity.infrastructure.JwtUtil;
import com.cs4135.elib.identity.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginUserUseCase {

    @Autowired
    private UserRepository repo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder encoder;

    public AuthResponse execute(LoginRequest request) {
        User user = repo.findByUsername(request.username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!encoder.matches(request.password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new AuthResponse(token);
    }
}
