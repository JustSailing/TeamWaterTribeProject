Feature: Main Todo UI

  # Both the Angular dev server (localhost:4200) and Spring Boot (localhost:8080)
  # must be running before executing these scenarios.

  Background:
    Given the user is registered and logged in

  Scenario: Empty state message is shown when no todos exist
    Then the empty main todo state is displayed

  Scenario: Create a main todo
    When the user types "Buy groceries" into the main todo input
    And the user clicks the add main todo button
    Then the main todo "Buy groceries" appears in the list

  Scenario: Create multiple main todos
    When the user types "Task Alpha" into the main todo input
    And the user clicks the add main todo button
    And the user types "Task Beta" into the main todo input
    And the user clicks the add main todo button
    Then the main todo "Task Alpha" appears in the list
    And the main todo "Task Beta" appears in the list

  Scenario: Edit a main todo
    Given a main todo "Original Task" exists
    When the user clicks the edit button for todo "Original Task"
    And the user clears the todo input and types "Updated Task"
    And the user saves the todo edit
    Then the main todo "Updated Task" appears in the list
    And the main todo "Original Task" is no longer in the list

  Scenario: Delete a main todo
    Given a main todo "Task To Delete" exists
    When the user deletes the todo "Task To Delete"
    Then the main todo "Task To Delete" is no longer in the list

  Scenario: Mark a main todo as complete
    Given a main todo "Morning Run" exists
    When the user checks the checkbox for todo "Morning Run"
    Then the main todo "Morning Run" is shown as completed

  Scenario: Cancel editing a main todo
    Given a main todo "Stable Task" exists
    When the user clicks the edit button for todo "Stable Task"
    And the user clears the todo input and types "Temporary Edit"
    And the user cancels the edit
    Then the main todo "Stable Task" appears in the list
    And the cancel button is no longer visible
