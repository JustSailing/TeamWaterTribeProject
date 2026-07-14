package com.watertribe.todo.cucumber;

import com.watertribe.todo.cucumber.pages.TodoPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for subtask UI scenarios (subtask.feature).
 *
 * Lifecycle hooks (@Before / @After / @AfterAll), DB cleanup, user seeding,
 * browser management, and shared Given steps all live in CucumberHooks.
 */
public class SubTaskSteps {

    private TodoPage todoPage() {
        return new TodoPage(DriverManager.getDriver());
    }

    @When("the user expands the main todo {string}")
    public void theUserExpandsMainTodo(String task) {
        todoPage().expandTodo(task);
    }

    @And("the user types {string} into the subtask input")
    public void theUserTypesIntoSubtaskInput(String text) {
        todoPage().typeInSubtaskInput(text);
    }

    @And("the user clicks the add subtask button")
    public void theUserClicksAddSubtaskButton() {
        todoPage().clickAddSubtaskButton();
    }

    @And("the user clicks the edit button for subtask {string}")
    public void theUserClicksEditButtonForSubtask(String task) {
        todoPage().clickEditButtonForSubtask(task);
    }

    @And("the user clears the subtask edit input and types {string}")
    public void theUserClearsEditInputAndTypes(String text) {
        todoPage().clearEditInputAndType(text);
    }

    @And("the user clicks the save subtask button")
    public void theUserClicksSaveSubtaskButton() {
        todoPage().clickSaveSubtaskButton();
    }

    @And("the user deletes the subtask {string}")
    public void theUserDeletesSubtask(String task) {
        todoPage().deleteSubtask(task);
    }

    @And("the user checks the checkbox for subtask {string}")
    public void theUserChecksCheckboxForSubtask(String task) {
        todoPage().checkSubtaskCheckbox(task);
    }

    @Then("the subtask panel is visible")
    public void theSubtaskPanelIsVisible() {
        assertTrue(todoPage().isSubtaskPanelVisible(),
            "Expected subtask panel to be visible");
    }

    @Then("the subtask {string} appears in the list")
    public void theSubtaskAppearsInList(String task) {
        assertTrue(todoPage().isSubtaskInList(task),
            "Expected subtask '" + task + "' to appear in the list");
    }

    @Then("the subtask {string} is no longer in the list")
    public void theSubtaskIsNoLongerInList(String task) {
        assertTrue(todoPage().isSubtaskAbsent(task),
            "Expected subtask '" + task + "' to be gone from the list");
    }

    @Then("the subtask {string} is shown as completed")
    public void theSubtaskIsShownAsCompleted(String task) {
        assertTrue(todoPage().isSubtaskCompleted(task),
            "Expected subtask '" + task + "' to have strikethrough styling");
    }

    @Then("the empty subtask message is displayed")
    public void theEmptySubtaskMessageIsDisplayed() {
        assertTrue(todoPage().isEmptySubtaskMessageDisplayed(),
            "Expected 'No subtasks yet.' message to be visible");
    }
}
