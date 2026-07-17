package com.watertribe.todo.cucumber;

import com.watertribe.todo.cucumber.pages.TodoPage;
import com.watertribe.todo.repository.MainTodoRepository;
import com.watertribe.todo.repository.SubTaskRepository;
import com.watertribe.todo.repository.UserRepository;
import com.watertribe.todo.service.UserService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Central lifecycle hooks and shared Given steps for all Cucumber feature
 * files.
 *
 * Keeping these here prevents them from being duplicated across SubTaskSteps
 * and
 * MainTodoSteps (which caused them to fire twice per scenario when both classes
 * were in the same glue package).
 *
 * - @Before / @After / @AfterAll: browser + DB setup and teardown
 * - Shared @Given steps used by both maintodo.feature and subtask.feature
 */
public class CucumberHooks {

  @Autowired
  UserService userService;
  @Autowired
  UserRepository userRepository;
  @Autowired
  MainTodoRepository mainTodoRepository;
  @Autowired
  SubTaskRepository subTaskRepository;

  @Autowired
  private WebDriver driver;
  @Autowired
  TodoPage todoPage; // package-private so step classes can share the instance if needed

  static final String USERNAME = "e2euser";
  static final String EMAIL = "e2e@example.com";
  static final String PASSWORD = "password123";

  @Before
  public void setUp() {
    subTaskRepository.deleteAll();
    mainTodoRepository.deleteAll();
    userRepository.deleteAll();
  }

  @After
  public void tearDown() {
    if (driver != null) {
      try {
        System.out.println("Test finished. Current URL: " + driver.getCurrentUrl());
      } catch (Exception e) {
        System.out.println("Browser already closed.");
      }
    }
  }

  // ── Shared Given steps (used by both maintodo.feature and subtask.feature) ──

  @Given("a main todo {string} exists")
  public void aMainTodoExists(String task) {
    todoPage.createMainTodo(task);
  }

  @Given("a subtask {string} exists under {string}")
  public void aSubtaskExistsUnder(String subtask, String mainTask) {

    // Open panel
    todoPage.expandTodo(mainTask);

    // Add subtask
    todoPage.typeInSubtaskInput(subtask);
    todoPage.clickAddSubtaskButton(subtask);

    // Angular rerender closes the panel.
    // Re-open it so the next step starts with a visible panel.
    todoPage.expandTodo(mainTask);

    assertTrue(
        todoPage.isSubtaskInList(subtask),
        "Subtask was not created in UI");
  }
}
