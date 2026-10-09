package org.digitalthinking.controllers;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.entities.Product;
import org.digitalthinking.exceptions.NotFoundException;
import org.digitalthinking.services.CustomerService;
import org.digitalthinking.services.CustomerSpringService;
import org.digitalthinking.services.CustomerViewService;
import org.digitalthinking.views.CustomerView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@QuarkusTest
class CustomerControllerTest {

    @InjectMock
    CustomerService customerService;

    @InjectMock
    CustomerViewService customerViewService;

    @InjectMock
    CustomerSpringService customerSpringService;

    private Customer c1;
    private Customer c2;

    @BeforeEach
    void setUp() {
        c1 = new Customer(1L, "CUST-001", "ACC-1001", "Juan", "Pérez", "555-0101", "Av. 1",
                new ArrayList<>(List.of(new Product(1L, null, 101L, null, null, null))));
        c2 = new Customer(2L, "CUST-002", "ACC-1002", "María", "Gómez", "555-0102", "Av. 2", new ArrayList<>());
    }

    @Test
    @DisplayName("GET /customer - Retorna lista")
    void testList() {
        when(customerService.list()).thenReturn(List.of(c1, c2));

        given().when().get("/customer")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(2))
                .body("[0].names", is("Juan"))
                .body("[0].products", hasSize(1))
                .body("[0].products[0].product", is(101))
                .body("[1].names", is("María"));

        verify(customerService).list();
    }

    @Test
    @DisplayName("GET /customer - Lista vacía")
    void testList_Vacio() {
        when(customerService.list()).thenReturn(Collections.emptyList());

        given().when().get("/customer")
                .then()
                .statusCode(200)
                .body("$", hasSize(0));
    }

    @Test
    @DisplayName("GET /customer/{id} - Retorna cliente")
    void testGetById() {
        when(customerService.getById(1L)).thenReturn(c1);

        given().when().get("/customer/1")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("code", is("CUST-001"))
                .body("accountNumber", is("ACC-1001"))
                .body("surname", is("Pérez"))
                .body("phone", is("555-0101"))
                .body("address", is("Av. 1"));
    }

    @Test
    @DisplayName("GET /customer/{id} - 404")
    void testGetById_NoExiste() {
        when(customerService.getById(99L))
                .thenThrow(new WebApplicationException("no", Response.Status.NOT_FOUND));

        given().when().get("/customer/99").then().statusCode(404);
    }

    @Test
    @DisplayName("GET /customer/{id} - id inválido retorna 404")
    void testGetById_IdInvalido() {
        given().when().get("/customer/abc").then().statusCode(404);
    }

    @Test
    @DisplayName("POST /customer - 201 con el cliente creado")
    void testAdd() {
        when(customerService.add(any(Customer.class))).thenReturn(c1);

        given()
                .contentType(ContentType.JSON)
                .body("{\"code\":\"CUST-001\",\"names\":\"Juan\",\"products\":[{\"product\":101}]}")
                .when().post("/customer")
                .then()
                .statusCode(201)
                .body("id", is(1))
                .body("names", is("Juan"));

        verify(customerService).add(any(Customer.class));
    }

    @Test
    @DisplayName("POST /customer - Error del servicio se propaga")
    void testAdd_Error() {
        when(customerService.add(any(Customer.class)))
                .thenThrow(new WebApplicationException("bad", Response.Status.BAD_REQUEST));

        given()
                .contentType(ContentType.JSON)
                .body("{\"code\":\"X\"}")
                .when().post("/customer")
                .then()
                .statusCode(400);
    }

    @Test
    @DisplayName("PUT /customer/{id} - Actualiza cliente")
    void testUpdate() {
        when(customerService.update(eq(1L), any(Customer.class))).thenReturn(c1);

        given()
                .contentType(ContentType.JSON)
                .body("{\"code\":\"CUST-001\",\"names\":\"Juan\"}")
                .when().put("/customer/1")
                .then()
                .statusCode(200)
                .body("names", is("Juan"));

        verify(customerService).update(eq(1L), any(Customer.class));
    }

    @Test
    @DisplayName("PUT /customer/{id} - 404")
    void testUpdate_NoExiste() {
        when(customerService.update(eq(99L), any(Customer.class)))
                .thenThrow(new WebApplicationException("no", Response.Status.NOT_FOUND));

        given()
                .contentType(ContentType.JSON)
                .body("{\"code\":\"X\"}")
                .when().put("/customer/99")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("DELETE /customer/{id} - 204")
    void testDelete() {
        doNothing().when(customerService).delete(1L);

        given().when().delete("/customer/1").then().statusCode(204);

        verify(customerService).delete(1L);
    }

    @Test
    @DisplayName("DELETE /customer/{id} - 404")
    void testDelete_NoExiste() {
        doThrow(new WebApplicationException("no", Response.Status.NOT_FOUND)).when(customerService).delete(99L);

        given().when().delete("/customer/99").then().statusCode(404);
    }

    private static CustomerView stubView(String code, String names) {
        return new CustomerView() {
            public Long getId() { return 1L; }
            public String getCode() { return code; }
            public String getAccountNumber() { return null; }
            public String getNames() { return names; }
            public String getSurname() { return null; }
            public String getPhone() { return null; }
            public String getAddress() { return null; }
            public java.util.Set<org.digitalthinking.views.ProductView> getProducts() { return java.util.Set.of(); }
        };
    }

    @Test
    @DisplayName("GET /customer/view - Retorna lista de views")
    void testListViews() {
        CustomerView v = stubView("CUST-001", "Juan");
        when(customerViewService.list()).thenReturn(List.of(v));

        given().when().get("/customer/view")
                .then()
                .statusCode(200)
                .body("$", hasSize(1))
                .body("[0].names", is("Juan"));
    }

    @Test
    @DisplayName("GET /customer/view/{id} - Retorna la view")
    void testGetViewById() {
        CustomerView v = stubView("CUST-001", "Juan");
        when(customerViewService.getById(1L)).thenReturn(v);

        given().when().get("/customer/view/1")
                .then()
                .statusCode(200)
                .body("code", is("CUST-001"));
    }

    @Test
    @DisplayName("GET /customer/view/{id} - 404 si no existe")
    void testGetViewById_NoExiste() {
        when(customerViewService.getById(99L)).thenThrow(new NotFoundException("no"));

        given().when().get("/customer/view/99")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET /customer/code/{code} - Retorna cliente")
    void testGetByCode() {
        when(customerSpringService.getByCode("CUST-001")).thenReturn(c1);

        given().when().get("/customer/code/CUST-001")
                .then()
                .statusCode(200)
                .body("names", is("Juan"));
    }

    @Test
    @DisplayName("GET /customer/code/{code} - 404 si no existe")
    void testGetByCode_NoExiste() {
        when(customerSpringService.getByCode("X")).thenThrow(new NotFoundException("no"));

        given().when().get("/customer/code/X")
                .then()
                .statusCode(404);
    }

    @Test
    @DisplayName("GET /customer/search?name= - Busca por nombre")
    void testSearchByName() {
        when(customerSpringService.searchByName("jua")).thenReturn(List.of(c1));

        given().queryParam("name", "jua").when().get("/customer/search")
                .then()
                .statusCode(200)
                .body("$", hasSize(1));
    }

    @Test
    @DisplayName("GET /customer/search?surname= - Busca por apellido")
    void testSearchBySurname() {
        when(customerSpringService.findBySurname("Gómez")).thenReturn(List.of(c2));

        given().queryParam("surname", "Gómez").when().get("/customer/search")
                .then()
                .statusCode(200)
                .body("[0].surname", is("Gómez"));
    }
}
