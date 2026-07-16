package com.watertribe.todo.service;

import com.watertribe.todo.entity.User;
import com.watertribe.todo.exception.LoginFailure;
import com.watertribe.todo.exception.RegistrationFailure;
import com.watertribe.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

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

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    return org.springframework.security.core.userdetails.User
        .withUsername(user.getUsername())
        .password(user.getPasswordHash()) // Ensure your Entity stores the hashed password
        .authorities("USER") // Or pull roles from your entity
        .build();
  }
}
