package com.cs4135.elib.identity.infrastructure;

import com.cs4135.elib.identity.application.usecases.LoginUserUseCase;
import com.cs4135.elib.identity.application.usecases.RegisterUserUseCases;
import com.cs4135.elib.identity.domain.User;
import com.cs4135.elib.identity.dto.AuthResponse;
import com.cs4135.elib.identity.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/auth")
public class AuthController {

    @Autowired
    private RegisterUserUseCases registerUseCase;

    @Autowired
    private LoginUserUseCase loginUseCase;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return registerUseCase.execute(user);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return loginUseCase.execute(request);
    }
}
