package com.watertribe.todo.cucumber;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;

import com.watertribe.todo.cucumber.pages.LoginPage;
import com.watertribe.todo.cucumber.pages.RegistrationPage;
import com.watertribe.todo.repository.UserRepository;

import io.cucumber.java.en.Given;

public class BackgroundSteps {
    @Autowired private WebDriver driver; 
    @Autowired private LoginPage loginPage;
    @Autowired private RegistrationPage regPage;
    @Autowired UserRepository userRepository;
    
    @Given("the user is registered and logged in")
    public void theUserIsRegisteredAndLoggedIn() {
        // 1. Create a unique user so it doesn't fail if run twice
        String uniqueId = String.valueOf(System.currentTimeMillis());
        String email = "test" + uniqueId + "@example.com";
        String username = "user" + uniqueId;
        String password = "Password123!";

        System.out.println("DEBUG: Registering user: " + username + " with password: " + password);
        regPage.open();
        regPage.register(email, username, password);
        
        assertTrue(userRepository.findByUsername(username).isPresent(), "User was not saved to the database!");

        System.out.println("DEBUG: Logging in with user: " + username + " and password: " + password);
        loginPage.open();
        loginPage.login(username, password);
        
        // 4. Force a wait to ensure we are actually ON the todo page
        // This solves your "stuck on login" problem
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.urlContains("/home/todos")); 
    }
}
