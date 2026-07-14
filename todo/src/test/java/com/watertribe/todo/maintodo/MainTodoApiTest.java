package com.watertribe.todo.maintodo;

import com.watertribe.todo.entity.User;
import com.watertribe.todo.repository.MainTodoRepository;
import com.watertribe.todo.repository.UserRepository;
import com.watertribe.todo.utility.JwtUtility;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MainTodoApiTest {

    @LocalServerPort
    int port;

    @Autowired UserRepository userRepository;
    @Autowired MainTodoRepository mainTodoRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtility jwtUtility;

    private String token;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        mainTodoRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .build());

        token = jwtUtility.generateToken(user);
    }

    @Test
    void testCreateMainTodoSuccessfully() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                {
                  "task": "Study REST Assured",
                  "description": "Write API tests"
                }
            """)
        .when()
            .post("/api/main-todos")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)))
            .body("id",          notNullValue())
            .body("task",        equalTo("Study REST Assured"))
            .body("description", equalTo("Write API tests"));
    }

    @Test
    void testGetAllMainTodos() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/main-todos")
        .then()
            .statusCode(200)
            .body("$", notNullValue());
    }

    @Test
    void testGetMainTodoById() {
        int todoId = createTodoForTest("Temporary Todo", "Temporary description");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/main-todos/" + todoId)
        .then()
            .statusCode(200)
            .body("id",   equalTo(todoId))
            .body("task", equalTo("Temporary Todo"));
    }

    @Test
    void testUpdateMainTodoSuccessfully() {
        int todoId = createTodoForTest("Original Todo", "Original description");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                {
                  "task": "Updated Todo",
                  "description": "Updated description"
                }
            """)
        .when()
            .put("/api/main-todos/" + todoId)
        .then()
            .statusCode(200)
            .body("id",          equalTo(todoId))
            .body("task",        equalTo("Updated Todo"))
            .body("description", equalTo("Updated description"));
    }

    @Test
    void testDeleteMainTodoSuccessfully() {
        int todoId = createTodoForTest("Todo to delete", "Delete me");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/main-todos/" + todoId)
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(204)));

        // After delete, fetching it should return an error (500 since service throws RuntimeException)
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/main-todos/" + todoId)
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(500)));
    }

    @Test
    void testCreateMainTodoWithoutTokenShouldFail() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "task": "Unauthorized Todo",
                  "description": "Should not be created"
                }
            """)
        .when()
            .post("/api/main-todos")
        .then()
            .statusCode(401);
    }

    @Test
    void testGetNonExistingMainTodoShouldReturn500() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/main-todos/999999")
        .then()
            .statusCode(anyOf(equalTo(404), equalTo(500)));
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private int createTodoForTest(String task, String description) {
        return given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(String.format("""
                {
                  "task": "%s",
                  "description": "%s"
                }
            """, task, description))
        .when()
            .post("/api/main-todos")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(201)))
            .extract()
            .path("id");
    }
}
