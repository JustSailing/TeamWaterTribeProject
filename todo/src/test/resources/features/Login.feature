Feature: User Login API
  As a registered user
  I want to log in via POST /login
  So that I receive a JWT token to access protected resources

  Background:
    Given the application is running on "http://localhost:8080"

  # ─── Happy Path ─────────────────────────────────────────────────────────────

  Scenario: Successful login with valid credentials returns a JWT token
    Given a registered user with username "alice" and password "password123"
    When I send a POST request to "/login" with:
      | username | alice       |
      | password | password123 |
    Then the response status code should be 200
    And the response body should be a non-empty JWT string

  # ─── Authentication Failures ────────────────────────────────────────────────

  Scenario: Login fails with wrong password
    Given a registered user with username "alice" and password "password123"
    When I send a POST request to "/login" with:
      | username | alice       |
      | password | wrongpasswd |
    Then the response status code should be 401
    And the response body should contain "Invalid username or password"

  Scenario: Login fails with non-existent username
    When I send a POST request to "/login" with:
      | username | ghost_user  |
      | password | password123 |
    Then the response status code should be 401
    And the response body should contain "Invalid username or password"

  Scenario: Login fails with wrong username and wrong password
    When I send a POST request to "/login" with:
      | username | nobody      |
      | password | badpassword |
    Then the response status code should be 401
    And the response body should contain "Invalid username or password"

  # ─── Input Validation ───────────────────────────────────────────────────────

  Scenario: Login fails when username is missing
    When I send a POST request to "/login" with:
      | password | password123 |
    Then the response status code should be 400
    And the response body should contain "Username and password are required."

  Scenario: Login fails when password is missing
    When I send a POST request to "/login" with:
      | username | alice |
    Then the response status code should be 400
    And the response body should contain "Username and password are required."

  Scenario: Login fails when both fields are blank
    When I send a POST request to "/login" with:
      | username |  |
      | password |  |
    Then the response status code should be 400
    And the response body should contain "Username and password are required."

  Scenario: Login fails when request body is empty JSON
    When I send a POST request to "/login" with an empty body
    Then the response status code should be 400
    And the response body should contain "Username and password are required."
