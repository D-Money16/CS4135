package com.cs4135.elib.identity.application.usecases;

@Service
public class RegisterUserUseCases {
    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEnconder encoder;

    public User execute(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole(Role.STUDENT);
        return repo.save(user);
    }
}
