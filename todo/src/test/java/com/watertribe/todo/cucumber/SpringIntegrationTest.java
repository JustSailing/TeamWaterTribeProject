package com.watertribe.todo.cucumber;

import com.watertribe.todo.TodoApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@CucumberContextConfiguration
@SpringBootTest(
    classes = TodoApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
@TestPropertySource(properties = "server.port=8080")
public class SpringIntegrationTest {
}
