package com.cs4135.elib.identity.infrastructure;

import com.cs4135.elib.identity.application.usecases.LoginUserUseCase;
import com.cs4135.elib.identity.application.usecases.RegisterUserUseCases;
import com.cs4135.elib.identity.domain.User;
import com.cs4135.elib.identity.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/auth")
public class AuthController {

    @Autowired
    private RegisterUserUseCases registerUseCase;

    @Autowired
    private LoginUserUseCase loginUseCase;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(registerUseCase.execute(user));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(loginUseCase.execute(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
