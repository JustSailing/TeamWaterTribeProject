package com.watertribe.todo.config;

import com.watertribe.todo.repository.UserRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;

@TestConfiguration
public class TestSecurityConfig {
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> {
            System.out.println("Looking for user: " + username); // DEBUG
            return userRepository.findByUsername(username)
                    .map(user -> {
                        System.out.println("Found user: " + user.getUsername());
                        return User.withUsername(user.getUsername())
                                .password(user.getPasswordHash())
                                .roles("USER")
                                .build();
                    })
                    .orElseThrow(() -> {
                        System.out.println("User not found!");
                        return new UsernameNotFoundException("User not found");
                    });
        };
    }
}
