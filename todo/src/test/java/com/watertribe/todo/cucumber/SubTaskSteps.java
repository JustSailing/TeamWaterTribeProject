package com.watertribe.todo.cucumber;

import com.watertribe.todo.cucumber.pages.TodoPage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SubTaskSteps {

    @Autowired
    private TodoPage todoPage;

    @When("the user expands the main todo {string}")
    public void theUserExpandsTheMainTodo(String task) {
        todoPage.expandTodo(task);
    }

    @Then("the subtask panel should be visible")
    public void theSubtaskPanelShouldBeVisible() {
        assertTrue(todoPage.isSubtaskPanelVisible(), "Subtask panel was not visible");
    }

    @When("the user adds a subtask {string}")
    public void theUserAddsASubtask(String subtask) {
        todoPage.typeInSubtaskInput(subtask);
        todoPage.clickAddSubtaskButton(subtask);
    }

    @Then("the subtask {string} should appear in the list")
    public void theSubtaskShouldAppearInTheList(String subtask) {
        assertTrue(todoPage.isSubtaskInList(subtask), "Subtask '" + subtask + "' was not found");
    }

    @When("the user edits the subtask {string} to {string}")
    public void theUserEditsTheSubtask(String oldName, String newName) {
        todoPage.clickEditButtonForSubtask(oldName);
        todoPage.clearAndTypeSubtaskEdit(newName);
        todoPage.clickSaveSubtaskButton();
    }

    @Then("the subtask {string} should no longer be in the list")
    public void theSubtaskShouldNoLongerBeInTheList(String subtask) {
        assertFalse(todoPage.isSubtaskInList(subtask), "Subtask '" + subtask + "' still exists");
    }

    @When("the user deletes the subtask {string}")
    public void theUserDeletesTheSubtask(String subtask) {
        todoPage.deleteSubtask(subtask);
    }

    @When("the user completes the subtask {string}")
    public void theUserCompletesTheSubtask(String subtask) {
        todoPage.checkSubtaskCheckbox(subtask);
    }

    @Then("the subtask {string} should be shown as completed")
    public void theSubtaskShouldBeShownAsCompleted(String subtask) {
        assertTrue(todoPage.isSubtaskCompleted(subtask), "Subtask '" + subtask + "' was not marked completed");
    }
    
    @Then("the empty subtask message is displayed")
    public void theEmptySubtaskMessageIsDisplayed() {
        assertTrue(todoPage.isEmptySubtaskMessageDisplayed(), "Empty subtask message was not displayed");
    }
}