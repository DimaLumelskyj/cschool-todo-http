package pl.connectis.todo;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import pl.connectis.todo.domain.Task;

import java.io.IOException;

import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.*;

class TodoAppTest {

    private static final String TASK_1 = "{\"name\":\"Zrobi\u0107 kaw\u0119\"," +
            "\"priority\":3,\"assigned\":\"Robert\"," +
            "                              \"description\":\"U\u017cyj tego nowego ekspresu\"}";
    private static final String TASK_2 = "{\"name\":\"Zrobi\u0107 kaw\u0119\"," +
            "\"priority\":3,\"assigned\":\"Marek\"," +
            "                              \"description\":\"La La la\"}";
    private static final int APP_PORT = 8080;
    private static final String INVALID_BOOK = "{\"name\":\"Zrobi\u0107 kaw\u0119\"," +
            "\"priority\":\"3fff\",\"assigned\":\"Robert\"," +
            "\"description\":\"U\u017cyj tego nowego ekspresu\"}";
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
    void afterEach() throws InterruptedException {
        todoApp.stop();
    }

    @Test
    void testToDoTaskSuccessfulAdd() {
        with()
                .body(TASK_1)
                .when()
                .post("/todos")
                .then()
                .statusCode(200)
                .body(containsString("id"));
    }

    @Test
    void testToDoTaskInvalidAdd() {
        with()
                .body(INVALID_BOOK)
                .when()
                .post("/todos")
                .then()
                .statusCode(400)
                .body(equalTo("Bad request"));
    }

    private long addTaskAndGetId(String json) throws IOException {
        Task task = with().body(json)
                .when().post("/todos")
                .then()
                .statusCode(200)
                // .body(startsWith("{"))
                .extract()
                .as(Task.class);
        return task.getId();
    }

    @ParameterizedTest
    @ValueSource(strings = {TASK_1, TASK_2})
    void getMethod_correctIDTaskParam_shouldReturnStatus200(String json) throws IOException {
        long taskID = addTaskAndGetId(json);
        with().param("id", taskID)
                .when()
                .get("/todos/getSingle")
                .then().statusCode(200)
                .body("id", equalTo(taskID));
    }

    @Test
    void getMethod_noIDTaskParam_shouldReturnStatus400() throws IOException {
        RestAssured
                .get("/todos/getSingle")
                .then().statusCode(400);
    }

    @Test
    void getMethod_wrongID_shouldReturnStatus404() throws IOException {
        with().param("id", -1)
                .when()
                .get("/todos/getSingle")
                .then().statusCode(404)
                .body(equalTo("Task doesn't exist"));
    }

    @Test
    void getAllMethod_correctZeroTasks_shouldReturnStatus200() throws IOException {
        with()
                .get("/todos/getAll")
                .then().statusCode(200)
                .body("", hasSize(0));
    }

    @Test
    void getAllMethod_correctOneTask_shouldReturnStatus200() throws IOException {
        long taskID1 = addTaskAndGetId(TASK_1);
        with()
                .get("/todos/getAll")
                .then().statusCode(200)
                .body("", hasSize(1))
                .body("id", hasItems(taskID1));
    }

    @Test
    void getAllMethod_correctTwoTasks_shouldReturnStatus200() throws IOException {
        long taskID1 = addTaskAndGetId(TASK_1);
        long taskID2 = addTaskAndGetId(TASK_2);
        with()
                .get("/todos/getAll")
                .then().statusCode(200)
                .body("", hasSize(2))
                .body("id", hasItems(taskID1, taskID2));
    }

}