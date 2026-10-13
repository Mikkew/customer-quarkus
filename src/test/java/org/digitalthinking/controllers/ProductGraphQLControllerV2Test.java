package org.digitalthinking.controllers;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import org.digitalthinking.entities.Product;
import org.digitalthinking.services.ProductGraphQLServiceV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
class ProductGraphQLControllerV2Test {

    @InjectMock
    ProductGraphQLServiceV2 productService;

    private Product p1;

    @BeforeEach
    void setUp() {
        p1 = new Product(1L, null, null, "Laptop", "P-001", "Portatil");
    }

    @Test
    @DisplayName("GET /v2/product - Retorna lista")
    void testList() {
        when(productService.list()).thenReturn(Uni.createFrom().item(List.of(p1)));

        given().when().get("/v2/product")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].name", is("Laptop"))
                .body("[0].code", is("P-001"));
    }

    @Test
    @DisplayName("GET /v2/product/{id} - Retorna producto")
    void testGetById() {
        when(productService.getById(1L)).thenReturn(Uni.createFrom().item(p1));

        given().when().get("/v2/product/1")
                .then()
                .statusCode(200)
                .body("description", is("Portatil"));
    }

    @Test
    @DisplayName("GET /v2/product/{id} - 404 cuando no existe")
    void testGetByIdNotFound() {
        when(productService.getById(9L)).thenReturn(Uni.createFrom().nullItem());

        given().when().get("/v2/product/9").then().statusCode(404);
    }

    @Test
    @DisplayName("POST /v2/product - Crea producto")
    void testCreate() {
        when(productService.create(any(Product.class))).thenReturn(Uni.createFrom().item(p1));

        given().contentType(ContentType.JSON)
                .body("{\"name\":\"Laptop\",\"code\":\"P-001\",\"description\":\"Portatil\"}")
                .when().post("/v2/product")
                .then()
                .statusCode(201)
                .body("id", is(1));
    }

    @Test
    @DisplayName("PUT /v2/product/{id} - Actualiza producto")
    void testUpdate() {
        when(productService.update(eq(1L), any(Product.class))).thenReturn(Uni.createFrom().item(p1));

        given().contentType(ContentType.JSON)
                .body("{\"name\":\"Laptop\"}")
                .when().put("/v2/product/1")
                .then()
                .statusCode(202)
                .body("name", is("Laptop"));
    }

    @Test
    @DisplayName("DELETE /v2/product/{id} - Elimina producto")
    void testDelete() {
        when(productService.delete(1L)).thenReturn(Uni.createFrom().item(p1));

        given().when().delete("/v2/product/1")
                .then()
                .statusCode(202)
                .body("id", is(1));
    }
}
