package org.digitalthinking.integrations;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@DisplayName("CustomerResource - Pruebas de integración (sin mocks)")
class CustomerResourceTest {

    private static final String BODY = "{\"code\":\"CUST-X\",\"accountNumber\":\"ACC-X\",\"names\":\"Luis\","
            + "\"surname\":\"Ruiz\",\"phone\":\"111\",\"address\":\"Calle\",\"products\":[{\"product\":101}]}";

    private int create() {
        return given().contentType(ContentType.JSON).body(BODY)
                .when().post("/customer")
                .then().statusCode(201)
                .body("id", notNullValue())
                .body("names", is("Luis"))
                .extract().path("id");
    }

    @Test
    @DisplayName("POST y GET por id - Persiste cliente con productos")
    void testCreateAndGet() {
        int id = create();

        given().when().get("/customer/" + id)
                .then()
                .statusCode(200)
                .body("code", is("CUST-X"))
                .body("products", hasSize(1))
                .body("products[0].product", is(101));
    }

    @Test
    @DisplayName("POST con id en el body - Ignora el id y crea")
    void testCreateWithId() {
        given().contentType(ContentType.JSON)
                .body("{\"id\":1,\"code\":\"C\",\"products\":[{\"id\":1,\"product\":101}]}")
                .when().post("/customer")
                .then().statusCode(201)
                .body("id", notNullValue());
    }

    @Test
    @DisplayName("GET /customer - Incluye cliente creado")
    void testList() {
        create();

        given().when().get("/customer")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("PUT - Actualiza cliente")
    void testUpdate() {
        int id = create();

        given().contentType(ContentType.JSON)
                .body("{\"code\":\"NEW\",\"names\":\"Pedro\",\"surname\":\"Sanz\",\"products\":[]}")
                .when().put("/customer/" + id)
                .then()
                .statusCode(200)
                .body("code", is("NEW"))
                .body("names", is("Pedro"));
    }

    @Test
    @DisplayName("DELETE - Elimina cliente y luego GET retorna 404")
    void testDelete() {
        int id = create();

        given().when().delete("/customer/" + id).then().statusCode(204);
        given().when().get("/customer/" + id).then().statusCode(404);
    }

    @Test
    @DisplayName("Error 404 - Retorna JSON con el mensaje")
    void testMensajeError() {
        given().when().get("/customer/999999")
                .then()
                .statusCode(404)
                .contentType(ContentType.JSON)
                .body("status", is(404))
                .body("message", containsString("999999"));
    }

    @Test
    @DisplayName("GET/PUT/DELETE inexistente - 404")
    void testNoExiste() {
        given().when().get("/customer/999999").then().statusCode(404);
        given().contentType(ContentType.JSON).body("{\"code\":\"X\",\"products\":[]}")
                .when().put("/customer/999999").then().statusCode(404);
        given().when().delete("/customer/999999").then().statusCode(404);
    }

    @Test
    @DisplayName("GET /customer/view/{id} - Proyecta cliente con productos")
    void testGetView() {
        int id = create();

        given().when().get("/customer/view/" + id)
                .then()
                .statusCode(200)
                .body("code", is("CUST-X"))
                .body("names", is("Luis"))
                .body("products", hasSize(1))
                .body("products[0].product", is(101));
    }

    @Test
    @DisplayName("GET /customer/view - Incluye cliente creado")
    void testListViews() {
        create();

        given().when().get("/customer/view")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @DisplayName("GET /customer/view/{id} - 404 si no existe")
    void testGetView_NoExiste() {
        given().when().get("/customer/view/999999")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET /customer/code/{code} y /search - Consultas vía Spring Data")
    void testSpringDataQueries() {
        String code = "CUST-" + java.util.UUID.randomUUID();
        given().contentType(ContentType.JSON).body(BODY.replace("CUST-X", code))
                .when().post("/customer").then().statusCode(201);

        given().when().get("/customer/code/" + code)
                .then().statusCode(200).body("names", is("Luis"));
        given().queryParam("name", "lui").when().get("/customer/search")
                .then().statusCode(200).body("size()", greaterThanOrEqualTo(1));
        given().queryParam("surname", "ruiz").when().get("/customer/search")
                .then().statusCode(200).body("size()", greaterThanOrEqualTo(1));
        given().when().get("/customer/code/NOPE")
                .then().statusCode(404);
    }
}
