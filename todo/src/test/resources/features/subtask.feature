Feature: SubTask UI Management

  Background:
    Given the user is registered and logged in

  Scenario: Expand a main todo to view subtasks
    Given a main todo "My First Todo" exists
    When the user expands the main todo "My First Todo"
    Then the subtask panel should be visible

  Scenario: Add a subtask
    Given a main todo "Shopping" exists
    When the user expands the main todo "Shopping"
    And the user adds a subtask "Buy milk"
    Then the subtask "Buy milk" should appear in the list

  Scenario: Add multiple subtasks
    Given a main todo "Work tasks" exists
    When the user expands the main todo "Work tasks"
    And the user adds a subtask "Write report"
    And the user adds a subtask "Send email"
    Then the subtask "Write report" should appear in the list
    And the subtask "Send email" should appear in the list

  Scenario: Edit a subtask
    Given a main todo "Errands" exists
    And a subtask "Old name" exists under "Errands"
    When the user expands the main todo "Errands"
    And the user edits the subtask "Old name" to "New name"
    Then the subtask "New name" should appear in the list
    And the subtask "Old name" should no longer be in the list

  Scenario: Delete a subtask
    Given a main todo "Chores" exists
    And a subtask "Wash dishes" exists under "Chores"
    When the user expands the main todo "Chores"
    And the user deletes the subtask "Wash dishes"
    Then the subtask "Wash dishes" should no longer be in the list

  Scenario: Mark a subtask as complete
    Given a main todo "Exercise" exists
    And a subtask "Morning run" exists under "Exercise"
    When the user expands the main todo "Exercise"
    And the user completes the subtask "Morning run"
    Then the subtask "Morning run" should be shown as completed