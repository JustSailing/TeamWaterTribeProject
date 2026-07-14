package com.watertribe.todo.controller;

import com.watertribe.todo.entity.User;
import com.watertribe.todo.service.UserService;
import com.watertribe.todo.utility.JwtUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtility jwtUtility;

    /**
     * POST /register
     * Body: { "username": "alice", "email": "alice@example.com", "password": "secret123" }
     * Throws RegistrationFailure (409) if username/email already exists — handled by GlobalExceptionHandler.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String email    = body.get("email");
        String password = body.get("password");

        if (username == null || username.isBlank() ||
            email    == null || email.isBlank()    ||
            password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("Username, email and password are required.");
        }
        if (password.length() < 8) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters.");
        }

        userService.register(username, email, password);
        return ResponseEntity.status(HttpStatus.CREATED).body("Registration successful!");
    }

    /**
     * POST /login
     * Body: { "username": "alice", "password": "secret123" }
     * Throws LoginFailure (401) on bad credentials — handled by GlobalExceptionHandler.
     */
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || username.isBlank() ||
            password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("Username and password are required.");
        }

        User user = userService.login(username, password);
        String token = jwtUtility.generateToken(user);
        return ResponseEntity.ok(token);
    }
}
