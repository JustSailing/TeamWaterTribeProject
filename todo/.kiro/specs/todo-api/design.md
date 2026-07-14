# Design Document — todo-api

## Overview

The Todo API is a Spring Boot 4.1.0 / Java 21 REST API that allows registered users to manage personal todo lists with subtasks. It uses:

- **Spring Web MVC** for the REST layer
- **Spring Security** (session-based) for authentication and authorization
- **Spring Session with JDBC backing** for server-side session persistence into SQLite
- **Hibernate ORM + SQLite** (via `hibernate-community-dialects`) for data persistence
- **Lombok** for boilerplate reduction
- **Gradle (Kotlin DSL)** as the build tool

The API is stateful: after a successful login the server creates a session stored in the SQLite database via Spring Session JDBC. The client receives a `SESSION` cookie and includes it on every subsequent request. All `/api/todos/**` and `/api/subtasks/**` endpoints are protected; registration and login are public.

---

## Architecture

```
HTTP Client
    │
    ▼
┌──────────────────────────────────────────────────┐
│ Spring Security Filter Chain                     │
│  ├── SessionRepositoryFilter (Spring Session)    │
│  ├── UsernamePasswordAuthenticationFilter        │
│  │     POST /api/auth/login                      │
│  ├── LogoutFilter                                │
│  │     POST /api/auth/logout                     │
│  └── AuthorizationFilter                         │
│        (guards /api/todos/**, /api/subtasks/**)  │
└──────────────────────────────────────────────────┘
    │
    ▼
┌──────────────────────────────┐
│ Controller Layer             │
│  UserController              │
│  TodoController              │
│  SubtaskController           │
└──────────────────────────────┘
    │
    ▼
┌──────────────────────────────┐
│ Service Layer                │
│  UserService                 │
│  TodoService                 │
│  SubtaskService              │
└──────────────────────────────┘
    │
    ▼
┌──────────────────────────────┐
│ Repository Layer (Spring JPA)│
│  UserRepository              │
│  MainTodoRepository          │
│  SubtaskRepository           │
└──────────────────────────────┘
    │
    ▼
┌──────────────────────────────┐
│ SQLite Database              │
│  users                       │
│  main_todos                  │
│  subtasks                    │
│  SPRING_SESSION              │
│  SPRING_SESSION_ATTRIBUTES   │
└──────────────────────────────┘
```


---

## Components and Interfaces

### Existing Components (already on disk)

| File | Status | Notes |
|------|--------|-------|
| `entity/MainTodo.java` | ✅ Exists | Complete — `@PrePersist`/`@PreUpdate` lifecycle hooks, `@ManyToOne` to `User` |
| `repository/MainTodoRepository.java` | ✅ Exists | `findByUser`, `findByIdAndUser` already defined |
| `entity/User.java` | ⚠️ Stub (empty file) | Needs full implementation |
| `repository/UserRepository.java` | ⚠️ Stub (empty file) | Needs implementation |
| `controller/UserController.java` | ⚠️ Stub (empty file) | Needs implementation |
| `service/UserService.java` | ⚠️ Stub (empty file) | Needs implementation |
| `exception/RegistrationFailure.java` | ⚠️ Stub (empty file) | Needs implementation |

### New Components (to be created)

| File | Purpose |
|------|---------|
| `entity/Subtask.java` | Subtask JPA entity |
| `repository/SubtaskRepository.java` | JPA repository for Subtask |
| `service/TodoService.java` | Business logic for MainTodo CRUD |
| `service/SubtaskService.java` | Business logic for Subtask CRUD |
| `controller/TodoController.java` | REST endpoints for MainTodo |
| `controller/SubtaskController.java` | REST endpoints for Subtask |
| `config/SecurityConfig.java` | Spring Security filter chain configuration |
| `dto/RegisterRequest.java` | Request DTO for `POST /api/users/register` |
| `dto/RegisterResponse.java` | Response DTO for registration |
| `dto/LoginResponse.java` | Response DTO returned after successful login |
| `dto/TodoRequest.java` | Request DTO for create/update MainTodo |
| `dto/TodoResponse.java` | Response DTO for MainTodo |
| `dto/SubtaskRequest.java` | Request DTO for create/update Subtask |
| `dto/SubtaskResponse.java` | Response DTO for Subtask |
| `dto/ErrorResponse.java` | Uniform error response shape |
| `exception/ResourceNotFoundException.java` | Thrown when a 404 entity is not found |
| `exception/ForbiddenException.java` | Thrown when a user accesses another user's resource |
| `exception/GlobalExceptionHandler.java` | `@RestControllerAdvice` — maps exceptions to HTTP responses |


---

## Data Models

### Entity: `User`

Implements `UserDetails` so Spring Security can use it directly as the authentication principal.

```java
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private List<String> roles;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void beforeCreate() {
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    // UserDetails methods: getAuthorities() maps roles to SimpleGrantedAuthority,
    // getPassword() returns passwordHash, getUsername() returns username.
    // isAccountNonExpired, isAccountNonLocked, isCredentialsNonExpired,
    // isEnabled all return true.
}
```

**DB table:** `users`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | INTEGER | PK, auto-increment |
| `username` | TEXT | NOT NULL, UNIQUE |
| `email` | TEXT | NOT NULL, UNIQUE |
| `password_hash` | TEXT | NOT NULL |
| `created_at` | DATETIME | NOT NULL |

**DB table:** `user_roles`

| Column | Type | Constraints |
|--------|------|-------------|
| `user_id` | INTEGER | FK → users.id |
| `role` | TEXT | NOT NULL |

---

### Entity: `MainTodo` (already exists)

```java
@Entity
@Table(name = "main_todos")
public class MainTodo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String task;

    private String description;

    private boolean completed;           // default false via @PrePersist

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

**DB table:** `main_todos`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | INTEGER | PK, auto-increment |
| `task` | TEXT | NOT NULL |
| `description` | TEXT | nullable |
| `completed` | BOOLEAN | NOT NULL, default 0 |
| `created_at` | DATETIME | NOT NULL |
| `updated_at` | DATETIME | NOT NULL |
| `user_id` | INTEGER | FK → users.id, NOT NULL |

---

### Entity: `Subtask` (new)

```java
@Entity
@Table(name = "subtasks")
public class Subtask {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private boolean completed;           // default false via @PrePersist

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "todo_id", nullable = false)
    private MainTodo mainTodo;

    @PrePersist
    public void beforeCreate() {
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
        completed = false;
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
```

**DB table:** `subtasks`

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | INTEGER | PK, auto-increment |
| `title` | TEXT | NOT NULL |
| `description` | TEXT | nullable |
| `completed` | BOOLEAN | NOT NULL, default 0 |
| `created_at` | DATETIME | NOT NULL |
| `updated_at` | DATETIME | NOT NULL |
| `todo_id` | INTEGER | FK → main_todos.id, NOT NULL |


---

## API Endpoint Specifications

### Authentication Endpoints

#### `POST /api/auth/login`

Handled by Spring Security's `UsernamePasswordAuthenticationFilter`. The filter is reconfigured to read JSON rather than form data.

**Request body:**
```json
{ "username": "alice", "password": "secret123" }
```

**Responses:**

| Status | Condition | Body |
|--------|-----------|------|
| 200 OK | Credentials valid | `{ "id": 1, "username": "alice" }` |
| 400 Bad Request | Blank/missing username or password | Error response with `errors` array |
| 401 Unauthorized | Wrong password or unknown username | `{ "status": 401, "message": "Bad credentials" }` |

A `Set-Cookie: SESSION=<token>; HttpOnly; Path=/` header is added to the 200 response by Spring Session.

---

#### `POST /api/auth/logout`

Handled by Spring Security's `LogoutFilter`.

**Request:** No body. Must include the `SESSION` cookie.

**Responses:**

| Status | Condition |
|--------|-----------|
| 200 OK | Session was valid and has been invalidated |
| 401 Unauthorized | No valid session present |

---

### User Endpoints

#### `POST /api/users/register`

**Request body:**
```json
{ "username": "alice", "email": "alice@example.com", "password": "secret123" }
```

**Validation (`@Valid` on `RegisterRequest`):**
- `username`: `@NotBlank`
- `email`: `@NotBlank`, `@Email`
- `password`: `@NotBlank`, `@Size(min = 8)`

**Responses:**

| Status | Condition | Body |
|--------|-----------|------|
| 201 Created | Registration successful | `{ "id": 1, "username": "alice", "email": "alice@example.com" }` |
| 400 Bad Request | Validation failure | Error response with `errors` array |
| 409 Conflict | Duplicate username or email | `{ "status": 409, "message": "Username already exists" }` |

---

### MainTodo Endpoints

All require a valid `SESSION` cookie. The authenticated `User` is resolved from the session as the `Principal`.

#### `POST /api/todos`

**Request body:**
```json
{ "task": "Buy groceries", "description": "Milk and eggs" }
```

**Responses:**

| Status | Condition | Body |
|--------|-----------|------|
| 201 Created | Todo created | Full `TodoResponse` |
| 400 Bad Request | Blank/missing `task` | Error response |
| 401 Unauthorized | No valid session | Error response |

**`TodoResponse` shape:**
```json
{
  "id": 1,
  "task": "Buy groceries",
  "description": "Milk and eggs",
  "completed": false,
  "createdAt": "2025-01-01T10:00:00",
  "updatedAt": "2025-01-01T10:00:00"
}
```

---

#### `GET /api/todos`

**Responses:**

| Status | Condition | Body |
|--------|-----------|------|
| 200 OK | Always (for authenticated user) | Array of `TodoResponse` (may be empty) |
| 401 Unauthorized | No valid session | Error response |

---

#### `GET /api/todos/{id}`

**Responses:**

| Status | Condition | Body |
|--------|-----------|------|
| 200 OK | Todo exists and belongs to principal | `TodoResponse` |
| 401 Unauthorized | No valid session | Error response |
| 403 Forbidden | Todo belongs to another user | Error response |
| 404 Not Found | No todo with this id | Error response |

---

#### `PUT /api/todos/{id}`

**Request body:**
```json
{ "task": "Buy groceries", "description": "Milk, eggs, and bread", "completed": true }
```

**Responses:**

| Status | Condition | Body |
|--------|-----------|------|
| 200 OK | Updated successfully | Updated `TodoResponse` |
| 400 Bad Request | Blank/missing `task` | Error response |
| 401 Unauthorized | No valid session | Error response |
| 403 Forbidden | Todo belongs to another user | Error response |
| 404 Not Found | No todo with this id | Error response |

---

#### `DELETE /api/todos/{id}`

**Responses:**

| Status | Condition | Body |
|--------|-----------|------|
| 204 No Content | Deleted successfully | (empty) |
| 401 Unauthorized | No valid session | Error response |
| 403 Forbidden | Todo belongs to another user | Error response |
| 404 Not Found | No todo with this id | Error response |


---

### Subtask Endpoints

All require a valid `SESSION` cookie. The `todoId` path variable identifies the parent `MainTodo`; the service verifies ownership before proceeding.

**`SubtaskResponse` shape:**
```json
{
  "id": 5,
  "title": "Get milk",
  "description": null,
  "completed": false,
  "createdAt": "2025-01-01T10:05:00",
  "updatedAt": "2025-01-01T10:05:00"
}
```

#### `POST /api/todos/{todoId}/subtasks`

**Request body:** `{ "title": "Get milk", "description": null }`

| Status | Condition |
|--------|-----------|
| 201 Created | Subtask created |
| 400 Bad Request | Blank/missing `title` |
| 401 Unauthorized | No session |
| 403 Forbidden | Parent todo belongs to another user |
| 404 Not Found | No parent todo with `todoId` |

---

#### `GET /api/todos/{todoId}/subtasks`

| Status | Condition |
|--------|-----------|
| 200 OK | Array of `SubtaskResponse` (may be empty) |
| 401 Unauthorized | No session |
| 403 Forbidden | Parent todo belongs to another user |
| 404 Not Found | No parent todo with `todoId` |

---

#### `GET /api/todos/{todoId}/subtasks/{id}`

| Status | Condition |
|--------|-----------|
| 200 OK | Subtask exists, parent owned by principal |
| 401 Unauthorized | No session |
| 403 Forbidden | Parent todo belongs to another user |
| 404 Not Found | No subtask with `id` under `todoId` |

---

#### `PUT /api/todos/{todoId}/subtasks/{id}`

**Request body:** `{ "title": "Get milk", "description": "Low-fat", "completed": true }`

| Status | Condition |
|--------|-----------|
| 200 OK | Subtask updated |
| 400 Bad Request | Blank/missing `title` |
| 401 Unauthorized | No session |
| 403 Forbidden | Parent todo belongs to another user |
| 404 Not Found | No subtask with `id` under `todoId` |

---

#### `DELETE /api/todos/{todoId}/subtasks/{id}`

| Status | Condition |
|--------|-----------|
| 204 No Content | Subtask deleted |
| 401 Unauthorized | No session |
| 403 Forbidden | Parent todo belongs to another user |
| 404 Not Found | No subtask with `id` under `todoId` |


---

## Spring Security Configuration

`SecurityConfig.java` extends nothing — it defines a `@Bean SecurityFilterChain`.

### Filter Chain Design

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())          // REST API — no CSRF needed
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.POST, "/api/users/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers("/api/todos/**", "/api/subtasks/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .successHandler(loginSuccessHandler())    // returns JSON {id, username}
                .failureHandler(loginFailureHandler())    // returns JSON {status, message}
                .usernameParameter("username")
                .passwordParameter("password")
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(logoutSuccessHandler())  // returns 200 OK
                .invalidateHttpSession(true)
                .deleteCookies("SESSION")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(unauthorizedEntryPoint())  // 401 for protected routes
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository repo) {
        return username -> repo.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
```

### Key Decisions

- **CSRF disabled** — pure REST API with no browser form submissions.
- **Session cookie name** — Spring Session JDBC defaults to `SESSION`; the cookie is `HttpOnly`.
- **Login/logout as JSON** — custom `AuthenticationSuccessHandler`, `AuthenticationFailureHandler`, and `LogoutSuccessHandler` write JSON to the response instead of redirecting. This is required because the default Spring Security behavior is a redirect (HTML) response.
- **401 on missing session** — a custom `AuthenticationEntryPoint` returns `401 Unauthorized` with an `ErrorResponse` JSON body instead of redirecting to a login page.
- **Content-Type negotiation** — all custom handlers write `application/json` responses explicitly.

---

## Spring Session JDBC Setup

Spring Session JDBC is already on the classpath via `spring-boot-starter-session-jdbc`. Two configuration steps are needed:

### `application.properties` additions

```properties
# Activate Spring Session JDBC
spring.session.store-type=jdbc
spring.session.jdbc.initialize-schema=always

# Session timeout (default 30 minutes)
spring.session.timeout=1800s
```

### Schema

Spring Session auto-creates two tables when `spring.session.jdbc.initialize-schema=always`:

- `SPRING_SESSION` — stores session id, principal name, creation/last-accessed times.
- `SPRING_SESSION_ATTRIBUTES` — stores serialized session attributes keyed by attribute name.

**Important:** With `spring.jpa.hibernate.ddl-auto=create-drop` currently set, the Spring Session tables will be dropped on app shutdown. This is acceptable for development. For production, change `ddl-auto` to `validate` or `none` and use a separate migration tool.


---

## Error Handling

### Exception Hierarchy

```
RuntimeException
 ├── RegistrationFailure        (409 Conflict — duplicate username/email)
 ├── ResourceNotFoundException  (404 Not Found)
 └── ForbiddenException         (403 Forbidden)
```

`@Valid` constraint violations are automatically handled by Spring and produce `MethodArgumentNotValidException`, which the global handler intercepts.

### `GlobalExceptionHandler`

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegistrationFailure.class)
    public ResponseEntity<ErrorResponse> handleRegistrationFailure(RegistrationFailure ex) {
        return ResponseEntity.status(409).body(new ErrorResponse(409, ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(new ErrorResponse(404, ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        return ResponseEntity.status(403).body(new ErrorResponse(403, ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .toList();
        ErrorResponse body = new ErrorResponse(400, "Validation failed", errors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(500).body(new ErrorResponse(500, "Internal server error"));
    }
}
```

### `ErrorResponse` DTO

```java
public record ErrorResponse(int status, String message, List<String> errors) {
    // Convenience constructor for non-validation errors (no errors list)
    public ErrorResponse(int status, String message) {
        this(status, message, null);
    }
}
```

**Serialized examples:**

Error without field details (404, 409, 401, 403, 500):
```json
{ "status": 404, "message": "Todo not found with id: 42" }
```

Validation error (400):
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    "username: must not be blank",
    "password: size must be between 8 and 2147483647"
  ]
}
```


---

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system — essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

**Property Reflection (redundancy elimination):**

After reviewing all PROPERTY-classified criteria, the following consolidations were made:

- 2.4, 2.5, 2.6, 2.7, 2.8 all concern registration input validation. They can be addressed by a single property over the space of invalid inputs rather than five separate properties.
- 7.3, 8.3, 9.2, 11.3, 12.3, 13.3, 14.2 all follow the same "non-existent id returns 404" pattern. Consolidated into two properties: one for todos, one for subtasks.
- 7.4, 8.4, 9.3, 11.4, 12.4, 13.4, 14.3 all follow the same "cross-user access returns 403" pattern. Consolidated into two properties: one for todos, one for subtasks.
- 5.1 and 5.2 are two sides of the same authorization property — combined into one.
- 8.1 and 7.2 together constitute a round-trip property for todos — combined.
- 13.1 and 12.2 together constitute a round-trip property for subtasks — combined.
- 6.2, 8.2, 11.2, 13.2 are all "blank title/task returns 400" patterns, consolidated into one validation rejection property.
- 1.4 (password hashing) is retained as a standalone property.
- 2.1 and 6.1 are retained as standalone creation round-trip properties.
- 7.1 and 12.1 (data isolation per user) are retained as standalone properties.
- 9.1 (cascade delete) is retained as a standalone property.
- 15.1 and 15.2 (error response shape) are retained as two distinct properties.

---

### Property 1: Password is never stored in plain text

*For any* plain-text password string, when a `User` is registered with that password, the value stored in `passwordHash` SHALL NOT equal the original plain-text string, and `BCryptPasswordEncoder.matches(plainText, storedHash)` SHALL return `true`.

**Validates: Requirements 1.4**

---

### Property 2: Registration is a successful round-trip for valid input

*For any* combination of a valid unique username, valid email address, and password of at least 8 characters, submitting a `POST /api/users/register` request SHALL return `201 Created`, and the response body SHALL contain the same `username` and `email` that were submitted.

**Validates: Requirements 2.1**

---

### Property 3: Registration rejects invalid input

*For any* registration request where at least one field is blank, the email does not match the standard format, or the password is shorter than 8 characters, the API SHALL return `400 Bad Request` with a non-empty `errors` array.

*For any* registration request where the username or email already belongs to an existing user, the API SHALL return `409 Conflict` with an error message identifying the duplicate field.

**Validates: Requirements 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8**

---

### Property 4: Login succeeds for any valid registered user and fails for invalid credentials

*For any* registered user, submitting a `POST /api/auth/login` request with the correct username and password SHALL return `200 OK` with the user's `id` and `username`, and the response SHALL include a `SESSION` cookie.

*For any* registered user, submitting a login request with an incorrect password SHALL return `401 Unauthorized`.

*For any* username not present in the database, submitting a login request SHALL return `401 Unauthorized`.

**Validates: Requirements 3.1, 3.2, 3.3**

---

### Property 5: Protected endpoints enforce authentication

*For any* request to any path under `/api/todos/**` or `/api/subtasks/**` made without a valid session cookie, the API SHALL return `401 Unauthorized`. *For any* equivalent request made with a valid session, the API SHALL not return `401` at the filter layer.

**Validates: Requirements 5.1, 5.2**

---

### Property 6: Todo creation round-trip

*For any* authenticated user and *any* non-blank task string, submitting `POST /api/todos` SHALL return `201 Created`. The returned `TodoResponse` SHALL have the submitted `task` value, `completed` equal to `false`, and non-null `createdAt` and `updatedAt` timestamps.

**Validates: Requirements 6.1**

---

### Property 7: Todo list is scoped to the authenticated user

*For any* set of users each owning some number of todos, `GET /api/todos` for a given user SHALL return exactly the todos owned by that user and no todos owned by other users.

**Validates: Requirements 7.1**

---

### Property 8: Todo CRUD is a consistent round-trip

*For any* authenticated user, any owned `MainTodo`, and any valid update payload, the sequence `POST /api/todos` → `GET /api/todos/{id}` → `PUT /api/todos/{id}` SHALL each return the correct current state of the todo, with `updatedAt` on a PUT response being greater than or equal to `createdAt`.

**Validates: Requirements 7.2, 8.1**

---

### Property 9: Todo operations on non-owned or non-existent todos return the correct error

*For any* authenticated user making a `GET`, `PUT`, or `DELETE` request to `/api/todos/{id}` where that `id` does not exist, the API SHALL return `404 Not Found`.

*For any* two distinct users where user B does not own todo `id`, user B's `GET`, `PUT`, or `DELETE` request to `/api/todos/{id}` SHALL return `403 Forbidden`.

**Validates: Requirements 7.3, 7.4, 8.3, 8.4, 9.2, 9.3**

---

### Property 10: Delete cascades to subtasks

*For any* `MainTodo` owned by the authenticated user, regardless of how many `Subtask` records it has, a successful `DELETE /api/todos/{id}` SHALL return `204 No Content` and all associated `Subtask` records SHALL no longer be retrievable.

**Validates: Requirements 9.1**

---

### Property 11: Subtask creation round-trip

*For any* authenticated user, any owned `MainTodo`, and any non-blank title string, submitting `POST /api/todos/{todoId}/subtasks` SHALL return `201 Created`. The returned `SubtaskResponse` SHALL have the submitted `title` value, `completed` equal to `false`, and non-null `createdAt` and `updatedAt` timestamps.

**Validates: Requirements 11.1**

---

### Property 12: Subtask list is scoped to the parent todo

*For any* authenticated user with multiple todos each owning some number of subtasks, `GET /api/todos/{todoId}/subtasks` SHALL return exactly the subtasks linked to `todoId` and no subtasks linked to other todos.

**Validates: Requirements 12.1**

---

### Property 13: Subtask CRUD is a consistent round-trip

*For any* authenticated user, owned parent todo, and any valid update payload, the sequence `POST /api/todos/{todoId}/subtasks` → `GET /api/todos/{todoId}/subtasks/{id}` → `PUT /api/todos/{todoId}/subtasks/{id}` SHALL each return the correct current state, with `updatedAt` on a PUT response being greater than or equal to `createdAt`.

**Validates: Requirements 12.2, 13.1**

---

### Property 14: Subtask operations on non-owned or non-existent resources return the correct error

*For any* request to `/api/todos/{todoId}/subtasks/{id}` where the subtask or parent todo does not exist, the API SHALL return `404 Not Found`.

*For any* request where the parent todo belongs to a different user, the API SHALL return `403 Forbidden`.

**Validates: Requirements 11.3, 11.4, 12.3, 12.4, 13.3, 13.4, 14.2, 14.3**

---

### Property 15: All error responses follow a consistent JSON shape

*For any* request that causes the API to return a 4xx or 5xx response, the response body SHALL be valid JSON containing at minimum an integer `status` field and a string `message` field.

**Validates: Requirements 15.1**

---

### Property 16: Validation error responses include a field-level errors array

*For any* request that causes a `400 Bad Request` due to constraint violations, the response body SHALL include a non-empty `errors` array where each element identifies the offending field and a human-readable message.

**Validates: Requirements 15.2**


---

## Testing Strategy

### Overview

This feature uses a **dual testing approach**: example-based unit/integration tests for specific scenarios, and property-based tests for the universal properties defined above. The two are complementary — unit tests catch concrete regressions, property tests verify general correctness across a large input space.

### Property-Based Testing

**Library:** [jqwik](https://jqwik.net/) — the most mature PBT library for Java, integrates with JUnit 5 (already on the classpath via Spring Boot Test), and generates arbitrary values for primitives, strings, collections, and custom types via `@Provide` annotators.

**Add to `build.gradle.kts`:**
```kotlin
testImplementation("net.jqwik:jqwik:1.9.1")
```

**Configuration:** Each `@Property` test runs a minimum of **100 tries** (jqwik default). Tests that require database interaction should use `@SpringBootTest` with a separate in-memory H2 or a fresh SQLite file per run; alternatively, mock the repository/service layers with Mockito for pure logic properties.

**Tagging convention:** Each property test is annotated with a comment:
```
// Feature: todo-api, Property N: <property title>
```

**Scope per property:**

| Property | Test class | What jqwik generates |
|----------|------------|----------------------|
| P1: Password hashing | `UserServicePropertyTest` | Arbitrary `String` (plain-text passwords) |
| P2: Registration round-trip | `UserControllerPropertyTest` | Valid username + email + password combos |
| P3: Registration input validation | `UserControllerPropertyTest` | Blank strings, short passwords, malformed emails, duplicate users |
| P4: Login success/failure | `AuthPropertyTest` | Registered users + correct/incorrect passwords |
| P5: Auth enforcement | `SecurityPropertyTest` | HTTP methods × endpoint paths, with/without session |
| P6: Todo creation | `TodoControllerPropertyTest` | Arbitrary non-blank task strings |
| P7: Todo list isolation | `TodoControllerPropertyTest` | Multiple users × multiple todos |
| P8: Todo CRUD round-trip | `TodoControllerPropertyTest` | Valid todo + valid update payloads |
| P9: Todo 404/403 | `TodoControllerPropertyTest` | Non-existent ids, cross-user access |
| P10: Cascade delete | `TodoControllerPropertyTest` | Todos with 0..N subtasks |
| P11: Subtask creation | `SubtaskControllerPropertyTest` | Arbitrary non-blank title strings |
| P12: Subtask list isolation | `SubtaskControllerPropertyTest` | Multiple todos × multiple subtasks |
| P13: Subtask CRUD round-trip | `SubtaskControllerPropertyTest` | Valid subtask + valid update payloads |
| P14: Subtask 404/403 | `SubtaskControllerPropertyTest` | Non-existent ids, cross-user access |
| P15: Error shape | `ErrorResponsePropertyTest` | Requests designed to trigger 4xx/5xx |
| P16: Validation error shape | `ErrorResponsePropertyTest` | Invalid payloads to all validated endpoints |

### Unit and Integration Tests

- **`UserServiceTest`** — example-based: BCrypt hash presence, `UsernameNotFoundException` on unknown user, `RegistrationFailure` on duplicate username/email.
- **`SecurityConfigTest`** — smoke: verify `/api/users/register` and `/api/auth/login` return non-401 when unauthenticated; verify `/api/todos/1` returns 401 without session.
- **`TodoServiceTest`** — example-based: create, update, delete with explicit entity assertions.
- **`SubtaskServiceTest`** — example-based: create, update, delete.
- **Integration tests** (`@SpringBootTest + MockMvc`) — one happy-path integration test per endpoint to verify the full stack from HTTP through to SQLite.

### Test Coverage Goals

- All 16 correctness properties have a corresponding `@Property` test.
- Every endpoint has at least one integration test for the happy path.
- Every exception type (`RegistrationFailure`, `ResourceNotFoundException`, `ForbiddenException`, `MethodArgumentNotValidException`) is covered by at least one unit test.
