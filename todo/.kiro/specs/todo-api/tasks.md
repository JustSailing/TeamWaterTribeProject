# Implementation Plan: todo-api

## Overview

This plan implements a Spring Boot 4.1.0 / Java 21 REST API for managing personal todo lists
with subtasks, session-based authentication, and Spring Session JDBC persistence backed by
SQLite. Tasks are ordered so each step integrates with the previous one — no orphaned code.

---

## Tasks

- [ ] 1. Configure application.properties for Spring Session JDBC
  - [ ] 1.1 Add Spring Session JDBC properties to `src/main/resources/application.properties`
    - Add `spring.session.store-type=jdbc`
    - Add `spring.session.jdbc.initialize-schema=always`
    - Add `spring.session.timeout=1800s`
    - _Requirements: 3.1, 4.1_

- [ ] 2. Implement the `User` entity and `UserRepository`
  - [ ] 2.1 Implement `entity/User.java`
    - Annotate with `@Entity`, `@Table(name = "users")`
    - Add `@Id @GeneratedValue` `Long id`, `@Column(nullable=false,unique=true) String username`, `@Column(nullable=false,unique=true) String email`, `@Column(nullable=false) String passwordHash`
    - Add `@ElementCollection` `List<String> roles` stored in `user_roles` table
    - Add `@Column(nullable=false,updatable=false) LocalDateTime createdAt` set via `@PrePersist` to `LocalDateTime.now(ZoneOffset.UTC)`
    - Implement `UserDetails`: `getAuthorities()` maps roles to `SimpleGrantedAuthority`; `getPassword()` returns `passwordHash`; all account-status methods return `true`
    - Use Lombok `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7_
  - [ ] 2.2 Implement `repository/UserRepository.java`
    - Extend `JpaRepository<User, Long>`
    - Declare `Optional<User> findByUsername(String username)`
    - Declare `Optional<User> findByEmail(String email)`
    - Declare `boolean existsByUsername(String username)`
    - Declare `boolean existsByEmail(String email)`
    - _Requirements: 1.2, 1.3, 2.2, 2.3_

- [ ] 3. Implement DTOs and exception classes
  - [ ] 3.1 Create `dto/RegisterRequest.java`
    - Record or class with `@NotBlank String username`, `@NotBlank @Email String email`, `@NotBlank @Size(min=8) String password`
    - _Requirements: 2.4, 2.5, 2.6, 2.7, 2.8_
  - [ ] 3.2 Create `dto/RegisterResponse.java`
    - Record with `Long id`, `String username`, `String email`
    - _Requirements: 2.1_
  - [ ] 3.3 Create `dto/LoginResponse.java`
    - Record with `Long id`, `String username`
    - _Requirements: 3.1_
  - [ ] 3.4 Create `dto/TodoRequest.java`
    - Record or class with `@NotBlank String task`, `String description`
    - _Requirements: 6.2, 8.2_
  - [ ] 3.5 Create `dto/TodoResponse.java`
    - Record with `Long id`, `String task`, `String description`, `boolean completed`, `LocalDateTime createdAt`, `LocalDateTime updatedAt`
    - _Requirements: 6.1, 7.2, 8.1_
  - [ ] 3.6 Create `dto/SubtaskRequest.java`
    - Record or class with `@NotBlank String title`, `String description`
    - _Requirements: 11.2, 13.2_
  - [ ] 3.7 Create `dto/SubtaskResponse.java`
    - Record with `Long id`, `String title`, `String description`, `boolean completed`, `LocalDateTime createdAt`, `LocalDateTime updatedAt`
    - _Requirements: 11.1, 12.2, 13.1_
  - [ ] 3.8 Create `dto/ErrorResponse.java`
    - Record with `int status`, `String message`, `List<String> errors`
    - Add convenience constructor `ErrorResponse(int status, String message)` that passes `null` for errors
    - _Requirements: 15.1, 15.2_
  - [ ] 3.9 Implement `exception/RegistrationFailure.java`
    - Extend `RuntimeException`; constructor accepts a `String message`
    - _Requirements: 2.2, 2.3_
  - [ ] 3.10 Create `exception/ResourceNotFoundException.java`
    - Extend `RuntimeException`; constructor accepts a `String message`
    - _Requirements: 7.3, 8.3, 9.2, 11.3, 12.3, 13.3, 14.2_
  - [ ] 3.11 Create `exception/ForbiddenException.java`
    - Extend `RuntimeException`; constructor accepts a `String message`
    - _Requirements: 7.4, 8.4, 9.3, 11.4, 12.4, 13.4, 14.3_

- [ ] 4. Implement `GlobalExceptionHandler`
  - [ ] 4.1 Create `exception/GlobalExceptionHandler.java`
    - Annotate with `@RestControllerAdvice`
    - Handle `RegistrationFailure` → `409` with `ErrorResponse`
    - Handle `ResourceNotFoundException` → `404` with `ErrorResponse`
    - Handle `ForbiddenException` → `403` with `ErrorResponse`
    - Handle `MethodArgumentNotValidException` → `400` with `ErrorResponse` that populates the `errors` list with `field: message` strings from `BindingResult`
    - Handle generic `Exception` → `500` with `ErrorResponse`
    - _Requirements: 15.1, 15.2_

- [ ] 5. Implement `Subtask` entity and `SubtaskRepository`
  - [ ] 5.1 Create `entity/Subtask.java`
    - Annotate with `@Entity`, `@Table(name = "subtasks")`
    - Add `@Id @GeneratedValue Long id`, `@Column(nullable=false) String title`, `String description`, `boolean completed`
    - Add `LocalDateTime createdAt`, `LocalDateTime updatedAt` set via `@PrePersist`; `updatedAt` updated via `@PreUpdate`
    - Add `@ManyToOne @JoinColumn(name="todo_id", nullable=false) MainTodo mainTodo`
    - Use Lombok `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7_
  - [ ] 5.2 Create `repository/SubtaskRepository.java`
    - Extend `JpaRepository<Subtask, Long>`
    - Declare `List<Subtask> findByMainTodo(MainTodo mainTodo)`
    - Declare `Optional<Subtask> findByIdAndMainTodo(Long id, MainTodo mainTodo)`
    - Declare `void deleteByMainTodo(MainTodo mainTodo)`
    - _Requirements: 9.1, 11.1, 12.1, 14.1_

- [ ] 6. Implement `UserService` and `UserController`
  - [ ] 6.1 Implement `service/UserService.java`
    - Inject `UserRepository` and `PasswordEncoder`
    - `register(RegisterRequest)`: check `existsByUsername` and `existsByEmail`, throw `RegistrationFailure` on conflict; encode the password with `PasswordEncoder`; build and save a `User` with role `ROLE_USER`; return `RegisterResponse`
    - _Requirements: 2.1, 2.2, 2.3_
  - [ ] 6.2 Implement `controller/UserController.java`
    - Annotate with `@RestController @RequestMapping("/api/users")`
    - `POST /register`: accept `@Valid @RequestBody RegisterRequest`, delegate to `UserService.register`, return `ResponseEntity.status(201).body(response)`
    - _Requirements: 2.1, 2.4, 2.5, 2.6, 2.7, 2.8_

- [ ] 7. Implement Spring Security configuration
  - [ ] 7.1 Create `config/SecurityConfig.java`
    - Annotate with `@Configuration @EnableWebSecurity`
    - Define `SecurityFilterChain` bean: disable CSRF; configure session `IF_REQUIRED`; permit `POST /api/users/register` and `POST /api/auth/login` without authentication; require authentication for `/api/todos/**`, `/api/subtasks/**`, and any other request
    - Configure `formLogin` with `loginProcessingUrl("/api/auth/login")`, custom `AuthenticationSuccessHandler` that writes `LoginResponse` JSON with `200 OK`, and custom `AuthenticationFailureHandler` that writes `ErrorResponse` JSON with `401`
    - Configure `logout` with `logoutUrl("/api/auth/logout")`, custom `LogoutSuccessHandler` that writes `200 OK`, `invalidateHttpSession(true)`, `deleteCookies("SESSION")`
    - Configure `exceptionHandling` with custom `AuthenticationEntryPoint` that writes `ErrorResponse` JSON with `401`
    - Define `PasswordEncoder` bean returning `BCryptPasswordEncoder`
    - Define `UserDetailsService` bean loading `User` from `UserRepository.findByUsername`
    - _Requirements: 3.1, 3.2, 3.3, 4.1, 4.2, 5.1, 5.2, 5.3_

- [ ] 8. Implement `TodoService` and `TodoController`
  - [ ] 8.1 Create `service/TodoService.java`
    - Inject `MainTodoRepository`
    - `create(User principal, TodoRequest)`: build and save `MainTodo` linked to `principal`; return `TodoResponse`
    - `findAll(User principal)`: return `List<TodoResponse>` via `findByUser`
    - `findById(User principal, Long id)`: use `findById`; throw `ResourceNotFoundException` if absent; throw `ForbiddenException` if `todo.user != principal`; return `TodoResponse`
    - `update(User principal, Long id, TodoRequest)`: load and ownership-check as above; update `task`, `description`, `completed`; save; return `TodoResponse`
    - `delete(User principal, Long id)`: load and ownership-check; delete
    - _Requirements: 6.1, 6.2, 7.1, 7.2, 7.3, 7.4, 8.1, 8.2, 8.3, 8.4, 9.1, 9.2, 9.3_
  - [ ] 8.2 Create `controller/TodoController.java`
    - Annotate with `@RestController @RequestMapping("/api/todos")`
    - Inject `TodoService`; resolve `@AuthenticationPrincipal User principal` on every handler
    - `POST /`: `@Valid @RequestBody TodoRequest` → `201 Created` with `TodoResponse`
    - `GET /`: → `200 OK` with `List<TodoResponse>`
    - `GET /{id}`: → `200 OK` with `TodoResponse`
    - `PUT /{id}`: `@Valid @RequestBody TodoRequest` → `200 OK` with `TodoResponse`
    - `DELETE /{id}`: → `204 No Content`
    - _Requirements: 6.1, 6.2, 7.1, 7.2, 7.3, 7.4, 8.1, 8.2, 8.3, 8.4, 9.1, 9.2, 9.3_

- [ ] 9. Implement `SubtaskService` and `SubtaskController`
  - [ ] 9.1 Create `service/SubtaskService.java`
    - Inject `SubtaskRepository` and `MainTodoRepository`
    - Helper `resolveOwnedTodo(User principal, Long todoId)`: load `MainTodo` or throw `ResourceNotFoundException`; throw `ForbiddenException` if not owned by principal
    - `create(User, Long todoId, SubtaskRequest)`: resolve parent todo; build and save `Subtask`; return `SubtaskResponse`
    - `findAll(User, Long todoId)`: resolve parent todo; return list
    - `findById(User, Long todoId, Long id)`: resolve parent todo; find subtask or throw `ResourceNotFoundException`; return `SubtaskResponse`
    - `update(User, Long todoId, Long id, SubtaskRequest)`: resolve parent todo; find subtask; update fields; save; return `SubtaskResponse`
    - `delete(User, Long todoId, Long id)`: resolve parent todo; find subtask; delete
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 12.1, 12.2, 12.3, 12.4, 13.1, 13.2, 13.3, 13.4, 14.1, 14.2, 14.3_
  - [ ] 9.2 Create `controller/SubtaskController.java`
    - Annotate with `@RestController @RequestMapping("/api/todos/{todoId}/subtasks")`
    - Inject `SubtaskService`; resolve `@AuthenticationPrincipal User principal` on every handler
    - `POST /`: `@Valid @RequestBody SubtaskRequest` → `201 Created` with `SubtaskResponse`
    - `GET /`: → `200 OK` with `List<SubtaskResponse>`
    - `GET /{id}`: → `200 OK` with `SubtaskResponse`
    - `PUT /{id}`: `@Valid @RequestBody SubtaskRequest` → `200 OK` with `SubtaskResponse`
    - `DELETE /{id}`: → `204 No Content`
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 12.1, 12.2, 12.3, 12.4, 13.1, 13.2, 13.3, 13.4, 14.1, 14.2, 14.3_

- [ ] 10. Checkpoint — wire and smoke-test the application
  - Ensure all tests pass, ask the user if questions arise.
  - Verify the app compiles and starts without errors via `./gradlew build`.

- [ ] 11. Write integration tests for user registration and authentication
  - [ ] 11.1 Create `UserControllerTest.java` with `@SpringBootTest` + `@AutoConfigureMockMvc`
    - Use an in-memory H2 (or SQLite test DB) and `@Transactional` to isolate each test
    - **Test — Property 1 (password not stored in plain text):** POST a registration request; load the saved `User` from the repository; assert `passwordHash` does not equal the plain-text password; assert `BCryptPasswordEncoder.matches(plainText, passwordHash)` returns `true`
    - **Test — Property 2 (registration round-trip, valid input):** POST valid `{username, email, password}`; assert `201 Created`; assert response body contains the same `username` and `email`
    - **Test — Property 3a (blank username rejected):** POST `{username:"", email, password}`; assert `400` with non-empty `errors` array
    - **Test — Property 3b (blank email rejected):** POST `{username, email:"", password}`; assert `400` with non-empty `errors` array
    - **Test — Property 3c (blank password rejected):** POST `{username, email, password:""}`; assert `400` with non-empty `errors` array
    - **Test — Property 3d (short password rejected):** POST with `password` of 7 characters; assert `400` with non-empty `errors` array
    - **Test — Property 3e (malformed email rejected):** POST with `email:"not-an-email"`; assert `400` with non-empty `errors` array
    - **Test — Property 3f (duplicate username rejected):** Register a user; POST again with the same username but different email; assert `409` with message identifying duplicate field
    - **Test — Property 3g (duplicate email rejected):** Register a user; POST again with different username but same email; assert `409` with message identifying duplicate field
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8_
  - [ ] 11.2 Create `AuthControllerTest.java` with `@SpringBootTest` + `@AutoConfigureMockMvc`
    - **Test — Property 4a (login success):** Register a user; POST `POST /api/auth/login` with correct credentials; assert `200 OK`, response body contains `id` and `username`, response includes `SESSION` cookie
    - **Test — Property 4b (wrong password rejected):** Register a user; POST login with wrong password; assert `401`
    - **Test — Property 4c (unknown username rejected):** POST login for a username not in the DB; assert `401`
    - **Test — Property 4d (blank username or password rejected):** POST login with blank fields; assert `400`
    - **Test — Property 5a (unauthenticated GET /api/todos returns 401):** Make request without session; assert `401`
    - **Test — Property 5b (unauthenticated GET /api/todos/{id} returns 401):** assert `401`
    - **Test — Property 5c (unauthenticated POST /api/todos returns 401):** assert `401`
    - **Test — Property 5d (authenticated request proceeds past auth filter):** Login, then GET `/api/todos`; assert not `401`
    - **Test — logout (Property 4 coverage):** Login; POST `/api/auth/logout`; assert `200`; re-attempt authenticated request; assert `401`
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 4.1, 4.2, 5.1, 5.2, 5.3_

- [ ] 12. Write integration tests for MainTodo CRUD
  - [ ] 12.1 Create `TodoControllerTest.java` with `@SpringBootTest` + `@AutoConfigureMockMvc`
    - Helper method: register + login a user and extract the `SESSION` cookie for subsequent requests
    - **Test — Property 6 (todo creation round-trip):** POST `POST /api/todos` with valid `task`; assert `201 Created`; assert `task` matches, `completed` is `false`, `createdAt` and `updatedAt` are non-null
    - **Test — Property 6 (blank task rejected):** POST with blank `task`; assert `400` with non-empty `errors` array
    - **Test — Property 7 (list scoped to authenticated user):** Register two users; each creates two todos; GET `/api/todos` for user A; assert exactly user A's todos are returned and none of user B's
    - **Test — Property 8a (GET /api/todos/{id} round-trip):** Create a todo; GET by id; assert same `task` value
    - **Test — Property 8b (PUT updates reflected in GET):** Create a todo; PUT with updated fields; GET by id; assert `task` equals updated value and `updatedAt >= createdAt`
    - **Test — Property 9a (GET non-existent todo returns 404):** GET `/api/todos/99999`; assert `404`
    - **Test — Property 9b (PUT non-existent todo returns 404):** PUT `/api/todos/99999`; assert `404`
    - **Test — Property 9c (DELETE non-existent todo returns 404):** DELETE `/api/todos/99999`; assert `404`
    - **Test — Property 9d (GET another user's todo returns 403):** User A creates a todo; user B authenticates and GET's user A's todo id; assert `403`
    - **Test — Property 9e (PUT another user's todo returns 403):** User A creates a todo; user B attempts PUT; assert `403`
    - **Test — Property 9f (DELETE another user's todo returns 403):** User A creates a todo; user B attempts DELETE; assert `403`
    - **Test — Property 10 (delete cascades to subtasks):** Create a todo; add two subtasks; DELETE the todo; assert `204`; attempt GET on each subtask; assert `404`
    - _Requirements: 6.1, 6.2, 7.1, 7.2, 7.3, 7.4, 8.1, 8.2, 8.3, 8.4, 9.1, 9.2, 9.3_

- [ ] 13. Write integration tests for Subtask CRUD
  - [ ] 13.1 Create `SubtaskControllerTest.java` with `@SpringBootTest` + `@AutoConfigureMockMvc`
    - Helper method: register + login a user, create a parent todo, extract `SESSION` cookie
    - **Test — Property 11 (subtask creation round-trip):** POST `POST /api/todos/{todoId}/subtasks` with valid `title`; assert `201 Created`; assert `title` matches, `completed` is `false`, `createdAt` and `updatedAt` are non-null
    - **Test — Property 11 (blank title rejected):** POST with blank `title`; assert `400` with non-empty `errors` array
    - **Test — Property 12 (subtask list scoped to parent todo):** Create two todos each with two subtasks; GET `/api/todos/{todo1Id}/subtasks`; assert only todo1's subtasks returned
    - **Test — Property 13a (GET subtask round-trip):** Create a subtask; GET by id; assert `title` matches
    - **Test — Property 13b (PUT subtask updates reflected in GET):** Create a subtask; PUT with updated fields; GET; assert `title` equals updated value and `updatedAt >= createdAt`
    - **Test — Property 14a (GET non-existent subtask returns 404):** GET `/api/todos/{todoId}/subtasks/99999`; assert `404`
    - **Test — Property 14b (PUT non-existent subtask returns 404):** PUT `/api/todos/{todoId}/subtasks/99999`; assert `404`
    - **Test — Property 14c (DELETE non-existent subtask returns 404):** DELETE `/api/todos/{todoId}/subtasks/99999`; assert `404`
    - **Test — Property 14d (subtask ops on another user's parent todo return 403):** User A creates a todo with a subtask; user B authenticates; assert GET, PUT, DELETE on that subtask all return `403`
    - **Test — Property 11 (create subtask on non-existent todo returns 404):** POST subtask to todoId `99999`; assert `404`
    - **Test — Property 11 (create subtask on another user's todo returns 403):** User A creates a todo; user B attempts to POST a subtask to it; assert `403`
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 12.1, 12.2, 12.3, 12.4, 13.1, 13.2, 13.3, 13.4, 14.1, 14.2, 14.3_

- [ ] 14. Write integration tests for error response shape
  - [ ] 14.1 Create `ErrorResponseShapeTest.java` with `@SpringBootTest` + `@AutoConfigureMockMvc`
    - **Test — Property 15 (4xx response has status and message fields):** Trigger each of the following and assert the JSON response body contains integer `status` and string `message`:
      - `401` from unauthenticated protected request
      - `403` from cross-user todo access
      - `404` from GET on non-existent todo
      - `409` from duplicate-username registration
    - **Test — Property 16 (400 validation response includes errors array):** POST registration with blank `username`; assert `400`; assert JSON body contains `errors` array with at least one element where each element includes the offending field name
    - **Test — Property 16 (todo 400 includes errors array):** POST `/api/todos` with blank `task` while authenticated; assert `400`; assert JSON body contains non-empty `errors` array
    - _Requirements: 15.1, 15.2_

- [ ] 15. Checkpoint — ensure all tests pass
  - Run `./gradlew test` and confirm all tests pass.
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Tests use JUnit 5 `@Test` methods with MockMvc to verify all 16 correctness properties from the design document using concrete example-based scenarios
- Unit tests validate specific examples and edge cases; integration tests verify end-to-end HTTP behavior

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1.1"] },
    { "id": 1, "tasks": ["2.1", "2.2"] },
    { "id": 2, "tasks": ["3.1", "3.2", "3.3", "3.4", "3.5", "3.6", "3.7", "3.8", "3.9", "3.10", "3.11"] },
    { "id": 3, "tasks": ["4.1", "5.1"] },
    { "id": 4, "tasks": ["5.2"] },
    { "id": 5, "tasks": ["6.1", "7.1"] },
    { "id": 6, "tasks": ["6.2", "8.1"] },
    { "id": 7, "tasks": ["8.2", "9.1"] },
    { "id": 8, "tasks": ["9.2"] },
    { "id": 9, "tasks": ["11.1", "11.2"] },
    { "id": 10, "tasks": ["12.1"] },
    { "id": 11, "tasks": ["13.1"] },
    { "id": 12, "tasks": ["14.1"] }
  ]
}
```
