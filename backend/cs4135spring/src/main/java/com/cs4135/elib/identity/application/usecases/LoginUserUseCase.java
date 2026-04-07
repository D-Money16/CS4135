package com.cs4135.elib.identity.application.usecases;

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
