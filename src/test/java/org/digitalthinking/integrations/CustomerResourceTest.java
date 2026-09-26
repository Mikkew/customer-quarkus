package org.digitalthinking.integrations;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.services.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@QuarkusTest
@DisplayName("CustomerController - Pruebas de integración")
class CustomerControllerTest {

    @InjectMock
    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        reset(customerService);
    }

    // Helper para no repetir la construcción del cliente
    private Customer buildCustomer(Long id) {
        Customer c = new Customer();
        c.setId(id);
        return c;
    }

    // ==================================================================
    // GET /customer  ->  listarTodos()
    // ==================================================================
    @Nested
    @DisplayName("listarTodos() - GET /customer")
    class ListarTodosTest {

        @Test
        @DisplayName("Retorna 200 con la lista de clientes")
        void listarTodos_retornaLista() {
            when(customerService.listarTodos())
                    .thenReturn(Arrays.asList(buildCustomer(1L), buildCustomer(2L)));

            given()
                    .accept(ContentType.JSON)
                    .when()
                    .get("/customer")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("size()", is(2))
                    .body("[0].id", is(1))
                    .body("[1].id", is(2));

            verify(customerService, times(1)).listarTodos();
        }

        @Test
        @DisplayName("Retorna 200 con lista vacía cuando no hay clientes")
        void listarTodos_retornaVacio() {
            when(customerService.listarTodos()).thenReturn(Collections.emptyList());

            given()
                    .accept(ContentType.JSON)
                    .when()
                    .get("/customer")
                    .then()
                    .statusCode(200)
                    .body("size()", is(0));

            verify(customerService).listarTodos();
        }
    }

    // ==================================================================
    // GET /customer/{id}  ->  obtenerPorId()
    // ==================================================================
    @Nested
    @DisplayName("obtenerPorId() - GET /customer/{id}")
    class ObtenerPorIdTest {

        @Test
        @DisplayName("Retorna 200 con el cliente solicitado")
        void obtenerPorId_encontrado() {
            when(customerService.obtenerPorId(5L)).thenReturn(buildCustomer(5L));

            given()
                    .accept(ContentType.JSON)
                    .when()
                    .get("/customer/5")
                    .then()
                    .statusCode(200)
                    .body("id", is(5));

            verify(customerService).obtenerPorId(5L);
        }

        @Test
        @DisplayName("Retorna 204 cuando el cliente no existe")
        void obtenerPorId_noEncontrado() {
            when(customerService.obtenerPorId(99L)).thenReturn(null);

            given()
                    .accept(ContentType.JSON)
                    .when()
                    .get("/customer/99")
                    .then()
                    .statusCode(204);

            verify(customerService).obtenerPorId(99L);
        }
    }

    // ==================================================================
    // POST /customer  ->  crear()
    // ==================================================================
    @Nested
    @DisplayName("crear() - POST /customer")
    class CrearTest {

        @Test
        @DisplayName("Retorna 201 con el cliente creado")
        void crear_exitoso() {
            when(customerService.crearCliente(any(Customer.class)))
                    .thenReturn(buildCustomer(10L));

            String body = "{\"id\":0}";

            given()
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body(body)
                    .when()
                    .post("/customer")
                    .then()
                    .statusCode(201)
                    .body("id", is(10));

            verify(customerService).crearCliente(any(Customer.class));
        }
    }

    // ==================================================================
    // PUT /customer/{id}  ->  actualizar()
    // ==================================================================
    @Nested
    @DisplayName("actualizar() - PUT /customer/{id}")
    class ActualizarTest {

        @Test
        @DisplayName("Retorna 200 con el cliente actualizado")
        void actualizar_exitoso() {
            when(customerService.actualizarCliente(eq(1L), any(Customer.class)))
                    .thenReturn(buildCustomer(1L));

            given()
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body("{\"id\":1}")
                    .when()
                    .put("/customer/1")
                    .then()
                    .statusCode(200)
                    .body("id", is(1));

            verify(customerService).actualizarCliente(eq(1L), any(Customer.class));
        }

        @Test
        @DisplayName("Retorna 204 cuando el cliente no existe")
        void actualizar_noEncontrado() {
            when(customerService.actualizarCliente(eq(99L), any(Customer.class)))
                    .thenReturn(null);

            given()
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .body("{\"id\":99}")
                    .when()
                    .put("/customer/99")
                    .then()
                    .statusCode(204);

            verify(customerService).actualizarCliente(eq(99L), any(Customer.class));
        }
    }

    // ==================================================================
    // DELETE /customer/{id}  ->  eliminar()
    // ==================================================================
    @Nested
    @DisplayName("eliminar() - DELETE /customer/{id}")
    class EliminarTest {

        @Test
        @DisplayName("Retorna 204 al eliminar correctamente")
        void eliminar_exitoso() {
            doNothing().when(customerService).eliminarCliente(1L);

            given()
                    .accept(ContentType.JSON)
                    .when()
                    .delete("/customer/1")
                    .then()
                    .statusCode(204);

            verify(customerService).eliminarCliente(1L);
        }
    }

    // ==================================================================
    // GET /customer/{id}/products  ->  obtenerProductosDeCliente()
    // ==================================================================
    @Nested
    @DisplayName("obtenerProductosDeCliente() - GET /customer/{id}/products")
    class ObtenerProductosTest {

        @Test
        @DisplayName("Retorna 200 con la lista de ids de productos")
        void obtenerProductos_ok() {
            when(customerService.obtenerProductIdsPorCliente(1L))
                    .thenReturn(Arrays.asList(100L, 200L, 300L));

            given()
                    .accept(ContentType.JSON)
                    .when()
                    .get("/customer/1/products")
                    .then()
                    .statusCode(200)
                    .body("size()", is(3))
                    .body("[0]", is(100))
                    .body("[1]", is(200))
                    .body("[2]", is(300));

            verify(customerService).obtenerProductIdsPorCliente(1L);
        }

        @Test
        @DisplayName("Retorna 200 con lista vacía si el cliente no tiene productos")
        void obtenerProductos_vacio() {
            when(customerService.obtenerProductIdsPorCliente(2L))
                    .thenReturn(Collections.emptyList());

            given()
                    .accept(ContentType.JSON)
                    .when()
                    .get("/customer/2/products")
                    .then()
                    .statusCode(200)
                    .body("size()", is(0));

            verify(customerService).obtenerProductIdsPorCliente(2L);
        }
    }

    // ==================================================================
    // POST /customer/{id}/products/{productId}  ->  agregarProducto()
    // ==================================================================
    @Nested
    @DisplayName("agregarProducto() - POST /customer/{id}/products/{productId}")
    class AgregarProductoTest {

        @Test
        @DisplayName("Retorna 200 con el cliente actualizado")
        void agregarProducto_ok() {
            when(customerService.agregarProductoACliente(1L, 50L))
                    .thenReturn(buildCustomer(1L));

            given()
                    .contentType(ContentType.JSON)
                    .accept(ContentType.JSON)
                    .when()
                    .post("/customer/1/products/50")
                    .then()
                    .statusCode(200)
                    .body("id", is(1));

            verify(customerService).agregarProductoACliente(1L, 50L);
        }
    }

    // ==================================================================
    // DELETE /customer/{id}/products/{productId}  ->  removerProducto()
    // ==================================================================
    @Nested
    @DisplayName("removerProducto() - DELETE /customer/{id}/products/{productId}")
    class RemoverProductoTest {

        @Test
        @DisplayName("Retorna 200 con el cliente actualizado")
        void removerProducto_ok() {
            when(customerService.removerProductoDeCliente(1L, 50L))
                    .thenReturn(buildCustomer(1L));

            given()
                    .accept(ContentType.JSON)
                    .when()
                    .delete("/customer/1/products/50")
                    .then()
                    .statusCode(200)
                    .body("id", is(1));

            verify(customerService).removerProductoDeCliente(1L, 50L);
        }
    }
}