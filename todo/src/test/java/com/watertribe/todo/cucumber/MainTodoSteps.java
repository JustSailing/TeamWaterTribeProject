package com.watertribe.todo.cucumber;

import com.watertribe.todo.cucumber.pages.TodoPage;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Step definitions for Main Todo UI scenarios (maintodo.feature).
 *
 * All lifecycle hooks (@Before / @After / @AfterAll), DB cleanup, user seeding,
 * and browser management live in SubTaskSteps. Cucumber discovers both classes
 * via the same glue path, so duplicating those hooks here would fire them twice
 * per scenario and crash the Spring context.
 *
 * "the user is registered and logged in" and "a main todo {string} exists" are
 * also already bound in SubTaskSteps — do NOT redefine them here.
 */
public class MainTodoSteps {

    private TodoPage todoPage() {
        return new TodoPage(DriverManager.getDriver());
    }

    // ── When / And ───────────────────────────────────────────────────────────

    @When("the user types {string} into the main todo input")
    public void theUserTypesIntoMainTodoInput(String text) {
        todoPage().clearMainTodoInputAndType(text);
    }

    @And("the user clicks the add main todo button")
    public void theUserClicksAddMainTodoButton() {
        todoPage().clickAddMainTodoButton();
    }

    @When("the user clicks the edit button for todo {string}")
    public void theUserClicksEditButtonForTodo(String task) {
        todoPage().clickEditButtonForTodo(task);
    }

    @And("the user clears the todo input and types {string}")
    public void theUserClearsTodoInputAndTypes(String text) {
        todoPage().clearMainTodoInputAndType(text);
    }

    @And("the user saves the todo edit")
    public void theUserSavesTodEdit() {
        todoPage().clickSaveTodoEdit();
    }

    @When("the user deletes the todo {string}")
    public void theUserDeletesTodo(String task) {
        todoPage().deleteTodo(task);
    }

    @When("the user checks the checkbox for todo {string}")
    public void theUserChecksCheckboxForTodo(String task) {
        todoPage().checkTodoCheckbox(task);
    }

    @And("the user cancels the edit")
    public void theUserCancelsTheEdit() {
        todoPage().clickCancelEdit();
    }

    // ── Then ─────────────────────────────────────────────────────────────────

    @Then("the empty main todo state is displayed")
    public void theEmptyMainTodoStateIsDisplayed() {
        assertTrue(todoPage().isEmptyStateDisplayed(),
            "Expected 'No todos yet.' empty-state message to be visible");
    }

    @Then("the main todo {string} appears in the list")
    public void theMainTodoAppearsInList(String task) {
        assertTrue(todoPage().isTodoInList(task),
            "Expected main todo '" + task + "' to appear in the list");
    }

    @And("the main todo {string} is no longer in the list")
    public void theMainTodoIsNoLongerInList(String task) {
        assertTrue(todoPage().isTodoAbsent(task),
            "Expected main todo '" + task + "' to be gone from the list");
    }

    @Then("the main todo {string} is shown as completed")
    public void theMainTodoIsShownAsCompleted(String task) {
        assertTrue(todoPage().isTodoCompleted(task),
            "Expected main todo '" + task + "' to have strikethrough styling");
    }

    @Then("the cancel button is no longer visible")
    public void theCancelButtonIsNoLongerVisible() {
        assertTrue(todoPage().isCancelButtonAbsent(),
            "Expected the cancel (✕) button to be absent after cancelling edit");
    }
}
