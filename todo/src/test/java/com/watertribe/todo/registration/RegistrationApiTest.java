package com.watertribe.todo.registration;

import com.watertribe.todo.config.TestSecurityConfig;
import com.watertribe.todo.repository.MainTodoRepository;
import com.watertribe.todo.repository.SubTaskRepository;
import com.watertribe.todo.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import java.util.UUID;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import(TestSecurityConfig.class)
class RegistrationApiTest {

    @LocalServerPort
    int port;

    @Autowired SubTaskRepository subTaskRepository;
    @Autowired MainTodoRepository mainTodoRepository;
    @Autowired UserRepository userRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        // Delete in FK order: subtasks → main todos → users
        subTaskRepository.deleteAll();
        mainTodoRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String uniqueUsername() {
        return "user_" + UUID.randomUUID().toString().substring(0, 6);
    }

    private String uniqueEmail(String username) {
        return username + "@example.com";
    }

    @Test
    void registerWithValidData_returns201() {
        String username = uniqueUsername();

        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": "%s",
                        "email":    "%s",
                        "password": "password123"
                    }
                    """.formatted(username, uniqueEmail(username)))
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(201)
            .body("message",equalTo("Registration successful!"));
    }

    @Test
    void registerWithDuplicateUsername_returns409() {
        String username = uniqueUsername();
        String email    = uniqueEmail(username);

        // First registration — must succeed
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": "%s",
                        "email":    "%s",
                        "password": "password123"
                    }
                    """.formatted(username, email))
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(201);

        // Second registration — same username, different email
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": "%s",
                        "email":    "other_%s",
                        "password": "password123"
                    }
                    """.formatted(username, email))
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(409)
            .body(equalTo("Username already exists"));
    }

    @Test
    void registerWithDuplicateEmail_returns409() {
        String username = uniqueUsername();
        String email    = uniqueEmail(username);

        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": "%s",
                        "email":    "%s",
                        "password": "password123"
                    }
                    """.formatted(username, email))
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": "other_%s",
                        "email":    "%s",
                        "password": "password123"
                    }
                    """.formatted(username, email))
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(409)
            .body(equalTo("Email already exists"));
    }

    @Test
    void registerWithMissingUsername_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "email":    "test@example.com",
                        "password": "password123"
                    }
                    """)
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400)
            .body("message",equalTo("Username, email and password are required."));
    }

    @Test
    void registerWithMissingEmail_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": "%s",
                        "password": "password123"
                    }
                    """.formatted(uniqueUsername()))
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400)
            .body("message",equalTo("Username, email and password are required."));
    }

    @Test
    void registerWithMissingPassword_returns400() {
        String username = uniqueUsername();
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": "%s",
                        "email":    "%s"
                    }
                    """.formatted(username, uniqueEmail(username)))
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400)
            .body("message",equalTo("Username, email and password are required."));
    }

    @Test
    void registerWithAllBlankFields_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": " ",
                        "email":    " ",
                        "password": " "
                    }
                    """)
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400)
            .body("message",equalTo("Username, email and password are required."));
    }

    @Test
    void registerWithEmptyBody_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400)
            .body("message",equalTo("Username, email and password are required."));
    }

    @Test
    void registerWithShortPassword_returns400() {
        String username = uniqueUsername();
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": "%s",
                        "email":    "%s",
                        "password": "short"
                    }
                    """.formatted(username, uniqueEmail(username)))
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400)
            .body("message", equalTo("Password must be at least 8 characters."));
    }

    @Test
    void registerWithSevenCharPassword_returns400() {
        String username = uniqueUsername();
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": "%s",
                        "email":    "%s",
                        "password": "1234567"
                    }
                    """.formatted(username, uniqueEmail(username)))
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(400)
            .body("message", equalTo("Password must be at least 8 characters."));
    }

    @Test
    void registerWithEightCharPassword_returns201() {
        String username = uniqueUsername();
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": "%s",
                        "email":    "%s",
                        "password": "12345678"
                    }
                    """.formatted(username, uniqueEmail(username)))
        .when()
            .post("/api/auth/register")
        .then()
            .statusCode(201)
            .body("message",equalTo("Registration successful!"));
    }
}
