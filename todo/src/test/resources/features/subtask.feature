Feature: SubTask UI

  # Both the Angular dev server (localhost:4200) and Spring Boot (localhost:8080)
  # must be running before executing these scenarios.

  Background:
    Given the user is registered and logged in

  Scenario: Expand a main todo to see the subtask panel
    Given a main todo "My First Todo" exists
    When the user expands the main todo "My First Todo"
    Then the subtask panel is visible

  Scenario: Add a subtask to a main todo
    Given a main todo "Shopping" exists
    When the user expands the main todo "Shopping"
    And the user types "Buy milk" into the subtask input
    And the user clicks the add subtask button
    Then the subtask "Buy milk" appears in the list

  Scenario: Add multiple subtasks
    Given a main todo "Work tasks" exists
    When the user expands the main todo "Work tasks"
    And the user types "Write report" into the subtask input
    And the user clicks the add subtask button
    And the user types "Send email" into the subtask input
    And the user clicks the add subtask button
    Then the subtask "Write report" appears in the list
    Then the subtask "Send email" appears in the list

  Scenario: Edit a subtask
    Given a main todo "Errands" exists
    And the subtask "Old name" exists under "Errands"
    When the user expands the main todo "Errands"
    And the user clicks the edit button for subtask "Old name"
    And the user clears the subtask edit input and types "New name"
    And the user clicks the save subtask button
    Then the subtask "New name" appears in the list

  Scenario: Delete a subtask
    Given a main todo "Chores" exists
    And the subtask "Wash dishes" exists under "Chores"
    When the user expands the main todo "Chores"
    And the user deletes the subtask "Wash dishes"
    Then the subtask "Wash dishes" is no longer in the list

  Scenario: Mark a subtask as complete
    Given a main todo "Exercise" exists
    And the subtask "Morning run" exists under "Exercise"
    When the user expands the main todo "Exercise"
    And the user checks the checkbox for subtask "Morning run"
    Then the subtask "Morning run" is shown as completed

  Scenario: Empty subtask panel shows empty state message
    Given a main todo "Empty Todo" exists
    When the user expands the main todo "Empty Todo"
    Then the empty subtask message is displayed
