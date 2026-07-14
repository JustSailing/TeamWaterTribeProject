package com.watertribe.todo.subtask;

import com.watertribe.todo.entity.MainTodo;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubTaskApiTest {

    @LocalServerPort
    int port;

    @Autowired UserRepository userRepository;
    @Autowired MainTodoRepository mainTodoRepository;
    @Autowired SubTaskRepository subTaskRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtility jwtUtility;

    private String token;
    private Long mainTodoId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;


        subTaskRepository.deleteAll();
        mainTodoRepository.deleteAll();
        userRepository.deleteAll();


        User user = userRepository.save(User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .build());

        token = jwtUtility.generateToken(user);

        
        MainTodo mainTodo = mainTodoRepository.save(MainTodo.builder()
                .task("Main Task")
                .description("Main task description")
                .user(user)
                .build());

        mainTodoId = mainTodo.getId();
    }


    @Test
    void createSubTask_returnsSubTaskBody() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "task": "Buy groceries",
                        "description": "Milk and eggs",
                        "completed": false
                    }
                    """)
        .when()
            .post("/api/main-todos/{mainTodoId}/subtasks", mainTodoId)
        .then()
            .statusCode(200)
            .body("task",        equalTo("Buy groceries"))
            .body("description", equalTo("Milk and eggs"))
            .body("completed",   equalTo(false))
            .body("mainTodoId",  equalTo(mainTodoId.intValue()))
            .body("id",          notNullValue());
    }

    @Test
    void createSubTask_withoutToken_returns401() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                    { "task": "No auth task" }
                    """)
        .when()
            .post("/api/main-todos/{mainTodoId}/subtasks", mainTodoId)
        .then()
            .statusCode(401);
    }

    @Test
    void createSubTask_onNonExistentMainTodo_returns500() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                    { "task": "Orphan subtask" }
                    """)
        .when()
            .post("/api/main-todos/99999/subtasks")
        .then()
            .statusCode(500);
    }


    @Test
    void getAllSubTasks_returnsEmptyList_whenNoneExist() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/main-todos/{mainTodoId}/subtasks", mainTodoId)
        .then()
            .statusCode(200)
            .body("$", hasSize(0));
    }

    @Test
    void getAllSubTasks_returnsAllSubtasksForMainTodo() {
        createSubTaskViaApi("First subtask");
        createSubTaskViaApi("Second subtask");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/main-todos/{mainTodoId}/subtasks", mainTodoId)
        .then()
            .statusCode(200)
            .body("$",    hasSize(2))
            .body("task", hasItems("First subtask", "Second subtask"));
    }


    @Test
    void getSubTaskById_returnsCorrectSubtask() {
        int subTaskId = createSubTaskViaApi("Specific task");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/main-todos/{mainTodoId}/subtasks/{id}", mainTodoId, subTaskId)
        .then()
            .statusCode(200)
            .body("id",   equalTo(subTaskId))
            .body("task", equalTo("Specific task"));
    }

    @Test
    void getSubTaskById_notFound_returns500() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/main-todos/{mainTodoId}/subtasks/99999", mainTodoId)
        .then()
            .statusCode(500);
    }


    @Test
    void updateSubTask_updatesTaskAndCompleted() {
        int subTaskId = createSubTaskViaApi("Old task name");

        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                    {
                        "task": "Updated task name",
                        "description": "Updated desc",
                        "completed": true
                    }
                    """)
        .when()
            .put("/api/main-todos/{mainTodoId}/subtasks/{id}", mainTodoId, subTaskId)
        .then()
            .statusCode(200)
            .body("task",        equalTo("Updated task name"))
            .body("description", equalTo("Updated desc"))
            .body("completed",   equalTo(true));
    }

    @Test
    void updateSubTask_withoutToken_returns401() {
        int subTaskId = createSubTaskViaApi("Some task");

        given()
            .contentType(ContentType.JSON)
            .body("""
                    { "task": "Sneaky update" }
                    """)
        .when()
            .put("/api/main-todos/{mainTodoId}/subtasks/{id}", mainTodoId, subTaskId)
        .then()
            .statusCode(401);
    }


    @Test
    void deleteSubTask_returnsSuccessMessage() {
        int subTaskId = createSubTaskViaApi("Task to delete");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/main-todos/{mainTodoId}/subtasks/{id}", mainTodoId, subTaskId)
        .then()
            .statusCode(200)
            .body(equalTo("Sub task deleted successfully"));
    }

    @Test
    void deleteSubTask_thenGetReturns500() {
        int subTaskId = createSubTaskViaApi("Task to delete then fetch");

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/main-todos/{mainTodoId}/subtasks/{id}", mainTodoId, subTaskId);


        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/main-todos/{mainTodoId}/subtasks/{id}", mainTodoId, subTaskId)
        .then()
            .statusCode(500);
    }

    @Test
    void deleteSubTask_withoutToken_returns401() {
        int subTaskId = createSubTaskViaApi("Protected task");

        given()
        .when()
            .delete("/api/main-todos/{mainTodoId}/subtasks/{id}", mainTodoId, subTaskId)
        .then()
            .statusCode(401);
    }


    private int createSubTaskViaApi(String task) {
        return given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"task\": \"" + task + "\"}")
        .when()
            .post("/api/main-todos/{mainTodoId}/subtasks", mainTodoId)
        .then()
            .statusCode(200)
            .extract()
            .path("id");
    }
}
