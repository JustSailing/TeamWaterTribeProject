package com.watertribe.todo.login;

import com.watertribe.todo.config.TestSecurityConfig;
import com.watertribe.todo.entity.User;
import com.watertribe.todo.repository.MainTodoRepository;
import com.watertribe.todo.repository.SubTaskRepository;
import com.watertribe.todo.repository.UserRepository;
import com.watertribe.todo.utility.JwtUtility;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import(TestSecurityConfig.class)
class LoginApiTest {

    @LocalServerPort
    int port;

    @Autowired SubTaskRepository subTaskRepository;
    @Autowired MainTodoRepository mainTodoRepository;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtility jwtUtility;

    private static final String VALID_USERNAME = "alice";
    private static final String VALID_EMAIL    = "alice@example.com";
    private static final String VALID_PASSWORD = "password123";

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        subTaskRepository.deleteAll();
        mainTodoRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(User.builder()
                .username(VALID_USERNAME)
                .email(VALID_EMAIL)
                .passwordHash(passwordEncoder.encode(VALID_PASSWORD))
                .build());
    }

    @Test
    void loginWithValidCredentials_returns200AndJwt() {
        String token =
            given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "alice",
                            "password": "password123"
                        }
                        """)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(200)
                .extract().asString();

        assertNotNull(token, "Token must not be null");
        assertFalse(token.isBlank(), "Token must not be blank");
        assertEquals(3, token.split("\\.").length,
                "Token should have three dot-separated segments (header.payload.signature)");
    }

    @Test
    void loginToken_hasValidJwtHeader() {
        String token =
            given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "alice",
                            "password": "password123"
                        }
                        """)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(200)
                .extract().asString();

        String headerB64 = token.split("\\.")[0];
        String padded = headerB64 + "=".repeat((4 - headerB64.length() % 4) % 4);
        String header = new String(java.util.Base64.getUrlDecoder().decode(padded));

        assertTrue(header.contains("\"alg\""), "Header should contain 'alg' claim");
    }

    @Test
    void loginToken_payloadContainsUsername() {
        String token =
            given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "username": "alice",
                            "password": "password123"
                        }
                        """)
            .when()
                .post("/api/auth/login")
            .then()
                .log().all()
                .statusCode(200)
                .extract().asString();

        String payloadB64 = token.split("\\.")[1];
        String padded = payloadB64 + "=".repeat((4 - payloadB64.length() % 4) % 4);
        String payload = new String(java.util.Base64.getUrlDecoder().decode(padded));

        assertTrue(payload.contains("\"username\""), "Payload should contain 'username' claim");
        assertTrue(payload.contains(VALID_USERNAME), "Payload should contain the logged-in username value");
    }


    @Test
    void loginWithWrongPassword_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": "alice",
                        "password": "wrongpasswd"
                    }
                    """)
        .when()
            .post("/api/auth/login")
        .then()
            .log().all()
            .statusCode(401)
            .body(equalTo("Invalid username or password"));
    }

    @Test
    void loginWithUnknownUsername_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": "ghost_user",
                        "password": "password123"
                    }
                    """)
        .when()
            .post("/api/auth/login")
        .then()
            .log().all()
            .statusCode(401)
            .body(equalTo("Invalid username or password"));
    }

    @Test
    void loginWithBothFieldsWrong_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": "nobody",
                        "password": "badpassword"
                    }
                    """)
        .when()
            .post("/api/auth/login")
        .then()
            .log().all()
            .statusCode(401)
            .body(equalTo("Invalid username or password"));
    }

    @Test
    void loginWithMissingUsername_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "password": "password123"
                    }
                    """)
        .when()
            .post("/api/auth/login")
        .then()
            .log().all()
            .statusCode(400)
            .body(equalTo("Username and password are required."));
    }

    @Test
    void loginWithMissingPassword_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": "alice"
                    }
                    """)
        .when()
            .post("/api/auth/login")
        .then()
            .log().all()
            .statusCode(400)
            .body(equalTo("Username and password are required."));
    }

    @Test
    void loginWithBlankFields_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "username": " ",
                        "password": " "
                    }
                    """)
        .when()
            .post("/api/auth/login")
        .then()
            .log().all()
            .statusCode(400)
            .body(equalTo("Username and password are required."));
    }

    @Test
    void loginWithEmptyBody_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/auth/login")
        .then()
            .log().all()
            .statusCode(400)
            .body(equalTo("Username and password are required."));
    }
}
