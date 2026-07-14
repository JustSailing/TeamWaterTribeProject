package com.watertribe.todo.cucumber.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class TodoPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private static final By MAIN_TODO_INPUT     = By.cssSelector("input#task");
    private static final By MAIN_TODO_SUBMIT    = By.cssSelector("button[type='submit']");
    private static final By MAIN_TODO_EMPTY     = By.cssSelector("p.empty-state");
    private static final By MAIN_TODO_CANCEL    = By.cssSelector("button.btn-cancel");
    private static final By EXPAND_BTN          = By.cssSelector(".btn-expand");
    private static final By SUBTASK_PANEL       = By.cssSelector(".subtodo-panel");
    private static final By SUBTASK_INPUT       = By.cssSelector(".subtodo-input");
    private static final By SUBTASK_ADD_BTN     = By.cssSelector(".btn-subtodo-add");
    private static final By SUBTASK_EMPTY       = By.cssSelector(".subtodo-empty");
    private static final By SUBTASK_EDIT_INPUT  = By.cssSelector(".subtodo-edit-input");
    private static final By SUBTASK_SAVE_BTN    = By.cssSelector(".btn-subtodo-save");

    public TodoPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public void open() {
        driver.get("http://localhost:4200/home/todos");
    }

    public void createMainTodo(String task) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(MAIN_TODO_INPUT));
        input.clear();
        input.sendKeys(task);
        driver.findElement(MAIN_TODO_SUBMIT).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//span[contains(@class,'todo-task') and text()='" + task + "']")
        ));
    }

    // ── Main todo interaction methods ─────────────────────────────────────────

    /** Clear input#task and type new text (used for both add and edit flows). */
    public void clearMainTodoInputAndType(String text) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(MAIN_TODO_INPUT));
        input.clear();
        input.sendKeys(text);
    }

    /** Click the submit button to add a new todo (shows '+' when not editing). */
    public void clickAddMainTodoButton() {
        driver.findElement(MAIN_TODO_SUBMIT).click();
    }

    /**
     * Click the edit (✏️) button for the todo with the given task text.
     * Waits until input#task contains the todo's current text (edit mode active).
     */
    public void clickEditButtonForTodo(String task) {
        WebElement todoRow = getTodoRowByTask(task);
        todoRow.findElement(By.cssSelector(".btn-edit")).click();
        wait.until(ExpectedConditions.attributeContains(MAIN_TODO_INPUT, "value", task));
    }

    /** Click the submit button to save a todo edit (shows '✓' during editing). */
    public void clickSaveTodoEdit() {
        wait.until(ExpectedConditions.elementToBeClickable(MAIN_TODO_SUBMIT)).click();
    }

    /** Click the delete (🗑️) button for the todo with the given task text. */
    public void deleteTodo(String task) {
        WebElement todoRow = getTodoRowByTask(task);
        todoRow.findElement(By.cssSelector(".btn-delete")).click();
    }

    /** Click the checkbox for the todo with the given task text to toggle completion. */
    public void checkTodoCheckbox(String task) {
        WebElement todoRow = getTodoRowByTask(task);
        todoRow.findElement(By.cssSelector(".todo-checkbox")).click();
    }

    /** Click the cancel (✕) button to abort an in-progress edit. */
    public void clickCancelEdit() {
        wait.until(ExpectedConditions.elementToBeClickable(MAIN_TODO_CANCEL)).click();
    }

    /** Returns true when a span.todo-task with exactly the given text is present. */
    public boolean isTodoInList(String task) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//span[contains(@class,'todo-task') and text()='" + task + "']")
            ));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Returns true when no span.todo-task with the given text is visible. */
    public boolean isTodoAbsent(String task) {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//span[contains(@class,'todo-task') and text()='" + task + "']")
            ));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Returns true when the matching span.todo-task has the 'strikethrough' class. */
    public boolean isTodoCompleted(String task) {
        try {
            // Wait explicitly for the strikethrough class to appear after the async API call + re-render
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//span[contains(@class,'todo-task') and contains(@class,'strikethrough') and text()='" + task + "']")
            ));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Returns true when p.empty-state is visible. */
    public boolean isEmptyStateDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(MAIN_TODO_EMPTY)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    /** Returns true when button.btn-cancel is not present in the DOM. */
    public boolean isCancelButtonAbsent() {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(MAIN_TODO_CANCEL));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    // ── Subtask interaction methods ───────────────────────────────────────────

    public void expandTodo(String task) {
        WebElement todoRow = getTodoRowByTask(task);
        // Only click expand if the panel is not already open
        boolean alreadyExpanded = !driver.findElements(By.cssSelector(".subtodo-panel")).isEmpty();
        if (!alreadyExpanded) {
            todoRow.findElement(EXPAND_BTN).click();
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(SUBTASK_PANEL));
    }

    public void typeInSubtaskInput(String text) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(SUBTASK_INPUT));
        input.clear();
        input.sendKeys(text);
    }

    public void clickAddSubtaskButton() {
        driver.findElement(SUBTASK_ADD_BTN).click();
    }

    public void clickEditButtonForSubtask(String task) {
        WebElement item = getSubtaskItemByText(task);
        item.findElement(By.cssSelector(".btn-edit")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(SUBTASK_EDIT_INPUT));
    }

    public void clearEditInputAndType(String text) {
        WebElement editInput = wait.until(ExpectedConditions.elementToBeClickable(SUBTASK_EDIT_INPUT));
        editInput.clear();
        editInput.sendKeys(text);
    }

    public void clickSaveSubtaskButton() {
        driver.findElement(SUBTASK_SAVE_BTN).click();
    }

    public void deleteSubtask(String task) {
        WebElement item = getSubtaskItemByText(task);
        item.findElement(By.cssSelector(".btn-delete")).click();
    }

    public void checkSubtaskCheckbox(String task) {
        WebElement item = getSubtaskItemByText(task);
        item.findElement(By.cssSelector(".subtodo-checkbox")).click();
    }

    public boolean isSubtaskPanelVisible() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(SUBTASK_PANEL)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isSubtaskInList(String task) {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//span[contains(@class,'subtodo-task') and text()='" + task + "']")
            ));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isSubtaskAbsent(String task) {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//span[contains(@class,'subtodo-task') and text()='" + task + "']")
            ));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isSubtaskCompleted(String task) {
        try {
            // Wait explicitly for the strikethrough class to appear after the async API call + re-render
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//span[contains(@class,'subtodo-task') and contains(@class,'strikethrough') and text()='" + task + "']")
            ));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isEmptySubtaskMessageDisplayed() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(SUBTASK_EMPTY)).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    private WebElement getTodoRowByTask(String task) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(@class,'todo-item') and .//span[contains(@class,'todo-task') and text()='" + task + "']]")
        ));
    }

    private WebElement getSubtaskItemByText(String task) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(
            By.xpath("//div[contains(@class,'subtodo-item') and .//span[contains(@class,'subtodo-task') and text()='" + task + "']]")
        ));
    }
}
