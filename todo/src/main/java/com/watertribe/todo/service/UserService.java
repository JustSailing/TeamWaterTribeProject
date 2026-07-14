package com.watertribe.todo.service;

import com.watertribe.todo.entity.User;
import com.watertribe.todo.exception.LoginFailure;
import com.watertribe.todo.exception.RegistrationFailure;
import com.watertribe.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user and returns the saved entity.
     */
    public User register(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RegistrationFailure("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RegistrationFailure("Email already exists");
        }

        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .build();

        return userRepository.save(user);
    }

    /**
     * Validates credentials. Throws LoginFailure if the username is not found
     * or the password does not match.
     */
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new LoginFailure("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new LoginFailure("Invalid username or password");
        }

        return user;
    }
}
