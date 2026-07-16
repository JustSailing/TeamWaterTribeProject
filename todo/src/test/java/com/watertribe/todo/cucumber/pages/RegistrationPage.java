package com.watertribe.todo.cucumber.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RegistrationPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By EMAIL_INPUT = By.cssSelector("input[formControlName='email']");
    private static final By USERNAME_INPUT = By.cssSelector("input[formControlName='username']");
    private static final By PASSWORD_INPUT = By.cssSelector("input[formControlName='password']");
    private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit']");

    @Autowired
    public RegistrationPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open() {
        driver.get("http://localhost:8080/register");
        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/register"));
        System.out.println("CURRENT URL: " + driver.getCurrentUrl());
    }

    public void register(String email, String username, String password) {
        var emailField = wait.until(ExpectedConditions.elementToBeClickable(EMAIL_INPUT));
        emailField.clear();
        emailField.sendKeys(email);

        var userField = wait.until(ExpectedConditions.elementToBeClickable(USERNAME_INPUT));
        userField.clear();
        userField.sendKeys(username);

        var passField = wait.until(ExpectedConditions.elementToBeClickable(PASSWORD_INPUT));
        passField.clear();
        passField.sendKeys(password);

        wait.until(ExpectedConditions.elementToBeClickable(SUBMIT_BUTTON)).click();

        // Ensure registration actually succeeded
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Registration successful"));
    }
}
