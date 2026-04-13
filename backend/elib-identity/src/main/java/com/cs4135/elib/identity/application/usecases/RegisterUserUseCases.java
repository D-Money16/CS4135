package com.cs4135.elib.identity.application.usecases;

import com.cs4135.elib.identity.domain.Role;
import com.cs4135.elib.identity.domain.User;
import com.cs4135.elib.identity.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterUserUseCases {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    public User execute(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole(Role.STUDENT);
        return repo.save(user);
    }
}
