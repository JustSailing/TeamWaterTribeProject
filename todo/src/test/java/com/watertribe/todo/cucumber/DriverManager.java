package com.watertribe.todo.cucumber;

import org.junit.jupiter.api.AfterEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.watertribe.todo.cucumber.pages.LoginPage;
import com.watertribe.todo.cucumber.pages.RegistrationPage;
import com.watertribe.todo.cucumber.pages.TodoPage;

import io.cucumber.spring.ScenarioScope;

@Configuration
public class DriverManager {
    @Bean
    @ScenarioScope
    public WebDriver getDriver() {
        ChromeOptions options = new ChromeOptions();
        //options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1280,800"); 
        return new ChromeDriver(options);
    }

    @Bean
    public LoginPage loginPage(WebDriver driver) { return new LoginPage(driver); }

    @Bean
    public RegistrationPage registrationPage(WebDriver driver) { return new RegistrationPage(driver); }

    @Bean
    public TodoPage todoPage(WebDriver driver) { return new TodoPage(driver); }
}
