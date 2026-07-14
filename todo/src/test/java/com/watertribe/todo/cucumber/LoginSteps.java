package com.watertribe.todo.cucumber;

import com.watertribe.todo.repository.UserRepository;
import com.watertribe.todo.service.UserService;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LoginSteps {

    @LocalServerPort
    int port;

    @Autowired UserService userService;
    @Autowired UserRepository userRepository;

    private Response lastResponse;

    @Before
    public void setUpRestAssured() {
        RestAssured.port = port;
    }

    @Given("the application is running on {string}")
    public void theApplicationIsRunning(String url) {
        // Spring Boot is already started by SpringIntegrationTest — nothing to do
    }

    @Given("a registered user with username {string} and password {string}")
    public void aRegisteredUser(String username, String password) {
        if (userRepository.findByUsername(username).isEmpty()) {
            userService.register(username, username + "@example.com", password);
        }
    }

    @When("I send a POST request to {string} with:")
    public void iSendPostWithTable(String path, io.cucumber.datatable.DataTable table) {
        Map<String, String> fields = table.asMap(String.class, String.class);
        lastResponse = given()
            .contentType(ContentType.JSON)
            .body(fields)
        .when()
            .post(path);
    }

    @When("I send a POST request to {string} with an empty body")
    public void iSendPostWithEmptyBody(String path) {
        lastResponse = given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post(path);
    }

    @Then("the response status code should be {int}")
    public void theResponseStatusCodeShouldBe(int expectedStatus) {
        assertThat(lastResponse.statusCode(), equalTo(expectedStatus));
    }

    @And("the response body should be a non-empty JWT string")
    public void theResponseBodyShouldBeJwt() {
        String body = lastResponse.getBody().asString();
        assertThat(body, not(emptyOrNullString()));
        assertThat(body.split("\\.").length, equalTo(3));
    }

    @And("the response body should contain {string}")
    public void theResponseBodyShouldContain(String expected) {
        assertThat(lastResponse.getBody().asString(), containsString(expected));
    }
}
