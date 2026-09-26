package org.digitalthinking.controllers;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.services.CustomerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;

@QuarkusTest
class CustomerControllerTest {

    @InjectMock
    private CustomerService customerService;

    // ==========================================
    // 1. GET /customer (listarTodos)
    // ==========================================

    @Test
    @DisplayName("GET /customer - Éxito: Retorna lista con elementos")
    public void testListarTodos_Exito() {
        Customer c1 = new Customer(1L, "Carlos", "Pérez", "carlos@test.com", "123", List.of(10L));
        Customer c2 = new Customer(2L, "Ana", "López", "ana@test.com", "456", List.of());

        when(customerService.listarTodos()).thenReturn(List.of(c1, c2));

        given()
                .when()
                .get("/customer")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("$", hasSize(2))
                .body("[0].nombre", is("Carlos"))
                .body("[1].nombre", is("Ana"));

        verify(customerService, times(1)).listarTodos();
    }

    @Test
    @DisplayName("GET /customer - Éxito: Retorna lista vacía")
    public void testListarTodos_Vacio() {
        when(customerService.listarTodos()).thenReturn(Collections.emptyList());

        given()
                .when()
                .get("/customer")
                .then()
                .statusCode(200)
                .body("$", empty());

        verify(customerService, times(1)).listarTodos();
    }

    // ==========================================
    // 2. GET /customer/{id} (obtenerPorId)
    // ==========================================

    @Test
    @DisplayName("GET /customer/{id} - Éxito: Retorna cliente encontrado")
    public void testObtenerPorId_Exito() {
        Customer c = new Customer(1L, "Carlos", "Pérez", "carlos@test.com", "123", List.of());
        when(customerService.obtenerPorId(1L)).thenReturn(c);

        given()
                .pathParam("id", 1L)
                .when()
                .get("/customer/{id}")
                .then()
                .statusCode(200)
                .body("id", is(1))
                .body("nombre", is("Carlos"));

        verify(customerService, times(1)).obtenerPorId(1L);
    }

    @Test
    @DisplayName("GET /customer/{id} - Error: Cliente no encontrado (404)")
    public void testObtenerPorId_NoEncontrado() {
        when(customerService.obtenerPorId(99L))
                .thenThrow(new WebApplicationException("Cliente no encontrado", Response.Status.NOT_FOUND));

        given()
                .pathParam("id", 99L)
                .when()
                .get("/customer/{id}")
                .then()
                .statusCode(404);

        verify(customerService, times(1)).obtenerPorId(99L);
    }

    // ==========================================
    // 3. POST /customer (crear)
    // ==========================================

    @Test
    @DisplayName("POST /customer - Éxito: Crea nuevo cliente (201 Created)")
    public void testCrear_Exito() {
        Customer input = new Customer(null, "Carlos", "Pérez", "carlos@test.com", "123", null);
        Customer output = new Customer(1L, "Carlos", "Pérez", "carlos@test.com", "123", List.of());

        when(customerService.crearCliente(any(Customer.class))).thenReturn(output);

        given()
                .contentType(ContentType.JSON)
                .body(input)
                .when()
                .post("/customer")
                .then()
                .statusCode(201)
                .body("id", is(1))
                .body("nombre", is("Carlos"));

        verify(customerService, times(1)).crearCliente(any(Customer.class));
    }

    @Test
    @DisplayName("POST /customer - Error: Solicitud inválida por el servicio (400 Bad Request)")
    public void testCrear_BadRequest() {
        Customer input = new Customer(10L, "Carlos", "Pérez", "carlos@test.com", "123", null);

        when(customerService.crearCliente(any(Customer.class)))
                .thenThrow(new WebApplicationException("El ID debe ser nulo", Response.Status.BAD_REQUEST));

        given()
                .contentType(ContentType.JSON)
                .body(input)
                .when()
                .post("/customer")
                .then()
                .statusCode(400);

        verify(customerService, times(1)).crearCliente(any(Customer.class));
    }

    // ==========================================
    // 4. PUT /customer/{id} (actualizar)
    // ==========================================

    @Test
    @DisplayName("PUT /customer/{id} - Éxito: Actualiza cliente existente")
    public void testActualizar_Exito() {
        Customer updateDetails = new Customer(null, "Carlos", "Gómez", "carlos@test.com", "999", null);
        Customer updatedResult = new Customer(1L, "Carlos", "Gómez", "carlos@test.com", "999", List.of());

        when(customerService.actualizarCliente(eq(1L), any(Customer.class))).thenReturn(updatedResult);

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", 1L)
                .body(updateDetails)
                .when()
                .put("/customer/{id}")
                .then()
                .statusCode(200)
                .body("apellido", is("Gómez"))
                .body("telefono", is("999"));

        verify(customerService, times(1)).actualizarCliente(eq(1L), any(Customer.class));
    }

    @Test
    @DisplayName("PUT /customer/{id} - Error: Intenta actualizar cliente inexistente (404)")
    public void testActualizar_NoEncontrado() {
        Customer updateDetails = new Customer(null, "Carlos", "Gómez", "carlos@test.com", "999", null);

        when(customerService.actualizarCliente(eq(99L), any(Customer.class)))
                .thenThrow(new WebApplicationException("Cliente no encontrado", Response.Status.NOT_FOUND));

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", 99L)
                .body(updateDetails)
                .when()
                .put("/customer/{id}")
                .then()
                .statusCode(404);

        verify(customerService, times(1)).actualizarCliente(eq(99L), any(Customer.class));
    }

    // ==========================================
    // 5. DELETE /customer/{id} (eliminar)
    // ==========================================

    @Test
    @DisplayName("DELETE /customer/{id} - Éxito: Elimina cliente (204 No Content)")
    public void testEliminar_Exito() {
        doNothing().when(customerService).eliminarCliente(1L);

        given()
                .pathParam("id", 1L)
                .when()
                .delete("/customer/{id}")
                .then()
                .statusCode(204);

        verify(customerService, times(1)).eliminarCliente(1L);
    }

    @Test
    @DisplayName("DELETE /customer/{id} - Error: Intenta eliminar cliente inexistente (404)")
    public void testEliminar_NoEncontrado() {
        doThrow(new WebApplicationException("Cliente no encontrado", Response.Status.NOT_FOUND))
                .when(customerService).eliminarCliente(99L);

        given()
                .pathParam("id", 99L)
                .when()
                .delete("/customer/{id}")
                .then()
                .statusCode(404);

        verify(customerService, times(1)).eliminarCliente(99L);
    }

    // ==========================================
    // 6. GET /customer/{id}/products (obtenerProductosDeCliente)
    // ==========================================

    @Test
    @DisplayName("GET /customer/{id}/products - Éxito: Retorna lista de IDs de productos")
    public void testObtenerProductosDeCliente_Exito() {
        when(customerService.obtenerProductIdsPorCliente(1L)).thenReturn(List.of(101L, 102L));

        given()
                .pathParam("id", 1L)
                .when()
                .get("/customer/{id}/products")
                .then()
                .statusCode(200)
                .body("$", hasSize(2))
                .body("[0]", is(101))
                .body("[1]", is(102));

        verify(customerService, times(1)).obtenerProductIdsPorCliente(1L);
    }

    @Test
    @DisplayName("GET /customer/{id}/products - Error: Cliente no encontrado (404)")
    public void testObtenerProductosDeCliente_NoEncontrado() {
        when(customerService.obtenerProductIdsPorCliente(99L))
                .thenThrow(new WebApplicationException("Cliente no encontrado", Response.Status.NOT_FOUND));

        given()
                .pathParam("id", 99L)
                .when()
                .get("/customer/{id}/products")
                .then()
                .statusCode(404);

        verify(customerService, times(1)).obtenerProductIdsPorCliente(99L);
    }

    // ==========================================
    // 7. POST /customer/{id}/products/{productId} (agregarProducto)
    // ==========================================

    @Test
    @DisplayName("POST /customer/{id}/products/{productId} - Éxito: Asocia producto a cliente")
    public void testAgregarProducto_Exito() {
        Customer updatedCustomer = new Customer(1L, "Carlos", "Pérez", "carlos@test.com", "123", List.of(200L));

        when(customerService.agregarProductoACliente(1L, 200L)).thenReturn(updatedCustomer);

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", 1L)
                .pathParam("productId", 200L)
                .when()
                .post("/customer/{id}/products/{productId}")
                .then()
                .statusCode(200)
                .body("productIds[0]", is(200));

        verify(customerService, times(1)).agregarProductoACliente(1L, 200L);
    }

    @Test
    @DisplayName("POST /customer/{id}/products/{productId} - Error: Cliente no existe (404)")
    public void testAgregarProducto_NoEncontrado() {
        when(customerService.agregarProductoACliente(99L, 200L))
                .thenThrow(new WebApplicationException("Cliente no encontrado", Response.Status.NOT_FOUND));

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", 99L)
                .pathParam("productId", 200L)
                .when()
                .post("/customer/{id}/products/{productId}")
                .then()
                .statusCode(404);

        verify(customerService, times(1)).agregarProductoACliente(99L, 200L);
    }

    // ==========================================
    // 8. DELETE /customer/{id}/products/{productId} (removerProducto)
    // ==========================================

    @Test
    @DisplayName("DELETE /customer/{id}/products/{productId} - Éxito: Desasocia producto")
    public void testRemoverProducto_Exito() {
        Customer updatedCustomer = new Customer(1L, "Carlos", "Pérez", "carlos@test.com", "123", List.of());

        when(customerService.removerProductoDeCliente(1L, 200L)).thenReturn(updatedCustomer);

        given()
                .pathParam("id", 1L)
                .pathParam("productId", 200L)
                .when()
                .delete("/customer/{id}/products/{productId}")
                .then()
                .statusCode(200)
                .body("productIds", empty());

        verify(customerService, times(1)).removerProductoDeCliente(1L, 200L);
    }

    @Test
    @DisplayName("DELETE /customer/{id}/products/{productId} - Error: Cliente no existe (404)")
    public void testRemoverProducto_NoEncontrado() {
        when(customerService.removerProductoDeCliente(99L, 200L))
                .thenThrow(new WebApplicationException("Cliente no encontrado", Response.Status.NOT_FOUND));

        given()
                .pathParam("id", 99L)
                .pathParam("productId", 200L)
                .when()
                .delete("/customer/{id}/products/{productId}")
                .then()
                .statusCode(404);

        verify(customerService, times(1)).removerProductoDeCliente(99L, 200L);
    }
}