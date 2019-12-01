package pl.connectis.todo;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class TodoAppTest {

    private static final String TASK_1 = "{\"name\":\"Zrobi\u0107 kaw\u0119\",\"priority\":3,\"assigned\":\"Robert\",\"description\":\"U\u017cyj tego nowego ekspresu\"}";
    private static final int APP_PORT = 8080;
    private static TodoApp todoApp;

    @BeforeAll
    public static void beforeAll() {
        RestAssured.port = APP_PORT;
    }

    @BeforeEach
    void beforeEach() throws IOException {
        todoApp = new TodoApp(APP_PORT);
        todoApp.start(5000, false);
    }

    @AfterEach
    void afterEach() {
        todoApp.stop();
    }

    @Test
    void testToDoTaskSuccessfulAdd() {
        RestAssured.with()
                .body(TASK_1)
                .when()
                .post("/todos")
                .then()
                .statusCode(200)
                .body(containsString("id"));
    }
}