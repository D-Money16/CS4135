package com.cs4135.elib.identity.infrastructure;

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
