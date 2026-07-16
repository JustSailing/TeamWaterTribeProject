package com.watertribe.todo.cucumber.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By USERNAME_INPUT = By.id("username");
    private static final By PASSWORD_INPUT = By.id("password");
    private static final By SUBMIT_BUTTON = By.cssSelector("button[type='submit']");

    @Autowired
    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open() {
        driver.get("http://localhost:8080/login");
    }

    public void login(String username, String password) {
        var userField = wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_INPUT));
        userField.clear(); // Always clear before sending keys
        userField.sendKeys(username);

        var passField = driver.findElement(PASSWORD_INPUT);
        passField.clear(); // Always clear before sending keys
        passField.sendKeys(password);

        driver.findElement(SUBMIT_BUTTON).click();
        wait.until(ExpectedConditions.urlContains("/home/todos"));

    }
}
