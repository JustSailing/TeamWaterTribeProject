package com.watertribe.todo.cucumber;

import com.watertribe.todo.cucumber.pages.LoginPage;
import com.watertribe.todo.cucumber.pages.TodoPage;
import com.watertribe.todo.repository.MainTodoRepository;
import com.watertribe.todo.repository.SubTaskRepository;
import com.watertribe.todo.repository.UserRepository;
import com.watertribe.todo.service.UserService;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Central lifecycle hooks and shared Given steps for all Cucumber feature files.
 *
 * Keeping these here prevents them from being duplicated across SubTaskSteps and
 * MainTodoSteps (which caused them to fire twice per scenario when both classes
 * were in the same glue package).
 *
 * - @Before / @After / @AfterAll: browser + DB setup and teardown
 * - Shared @Given steps used by both maintodo.feature and subtask.feature
 */
public class CucumberHooks {

    @Autowired UserService          userService;
    @Autowired UserRepository       userRepository;
    @Autowired MainTodoRepository   mainTodoRepository;
    @Autowired SubTaskRepository    subTaskRepository;

    private WebDriver driver;
    private LoginPage loginPage;
    TodoPage          todoPage;   // package-private so step classes can share the instance if needed

    static final String USERNAME = "e2euser";
    static final String EMAIL    = "e2e@example.com";
    static final String PASSWORD = "password123";

    @Before
    public void setUp() {
        subTaskRepository.deleteAll();
        mainTodoRepository.deleteAll();
        userRepository.deleteAll();

        driver    = DriverManager.getDriver();
        loginPage = new LoginPage(driver);
        todoPage  = new TodoPage(driver);
    }

    @After
    public void tearDown() {
        String url = driver.getCurrentUrl();
        if (url != null && !url.startsWith("data:")) {
            driver.manage().deleteAllCookies();
            ((JavascriptExecutor) driver).executeScript(
                "window.localStorage.clear(); window.sessionStorage.clear();"
            );
        }
    }

    @AfterAll
    public static void closeBrowser() {
        DriverManager.closeDriver();
    }

    // ── Shared Given steps (used by both maintodo.feature and subtask.feature) ──

    @Given("the user is registered and logged in")
    public void theUserIsRegisteredAndLoggedIn() {
        userService.register(USERNAME, EMAIL, PASSWORD);
        loginPage.open();
        loginPage.login(USERNAME, PASSWORD);
        todoPage.open();
    }

    @Given("a main todo {string} exists")
    public void aMainTodoExists(String task) {
        todoPage.createMainTodo(task);
    }

    @Given("the subtask {string} exists under {string}")
    public void theSubtaskExistsUnder(String subtask, String mainTask) {
        todoPage.expandTodo(mainTask);
        todoPage.typeInSubtaskInput(subtask);
        todoPage.clickAddSubtaskButton();
        assertTrue(todoPage.isSubtaskInList(subtask),
            "Seed failed: subtask '" + subtask + "' did not appear after creation");
    }
}
