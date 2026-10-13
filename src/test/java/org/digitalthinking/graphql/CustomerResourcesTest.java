package org.digitalthinking.graphql;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.exceptions.NotFoundException;
import org.digitalthinking.services.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@QuarkusTest
class CustomerResourcesTest {

    @InjectMock
    CustomerService customerService;

    private Customer c1;
    private Customer c2;

    @BeforeEach
    void setUp() {
        c1 = new Customer(1L, "CUST-001", "ACC-1001", "Juan", "Pérez", "555-0101", "Av. 1", new ArrayList<>());
        c2 = new Customer(2L, "CUST-002", "ACC-1002", "María", "Gómez", "555-0102", "Av. 2", new ArrayList<>());
    }

    private io.restassured.response.ValidatableResponse graphql(String query) {
        return given().contentType(ContentType.JSON)
                .body(Map.of("query", query))
                .when().post("/graphql")
                .then();
    }

    @Test
    @DisplayName("allCustomer - Retorna lista")
    void testAllCustomer() {
        when(customerService.list()).thenReturn(List.of(c1, c2));

        graphql("{ allCustomer { id code names } }")
                .statusCode(200)
                .body("data.allCustomer", hasSize(2))
                .body("data.allCustomer[0].code", is("CUST-001"))
                .body("data.allCustomer[1].names", is("María"));
    }

    @Test
    @DisplayName("customer - Retorna cliente por id")
    void testCustomer() {
        when(customerService.getById(1L)).thenReturn(c1);

        graphql("{ customer(customerId: 1) { id names surname } }")
                .statusCode(200)
                .body("data.customer.names", is("Juan"))
                .body("data.customer.surname", is("Pérez"));
    }

    @Test
    @DisplayName("customer - Error GraphQL si no existe")
    void testCustomer_NoExiste() {
        when(customerService.getById(99L)).thenThrow(new NotFoundException("Cliente no encontrado con ID: 99"));

        graphql("{ customer(customerId: 99) { id } }")
                .statusCode(200)
                .body("errors", hasSize(1))
                .body("data.customer", nullValue());
    }

    @Test
    @DisplayName("addCustomer - Crea cliente")
    void testAddCustomer() {
        when(customerService.add(any(Customer.class))).thenReturn(c1);

        graphql("mutation { addCustomer(customer: {code: \"CUST-001\", names: \"Juan\"}) { id code } }")
                .statusCode(200)
                .body("data.addCustomer.id", is(1))
                .body("data.addCustomer.code", is("CUST-001"));

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(Customer.class);
        verify(customerService).add(captor.capture());
        assertEquals("Juan", captor.getValue().getNames());
    }

    @Test
    @DisplayName("updateCustomer - Actualiza usando el id del input")
    void testUpdateCustomer() {
        when(customerService.update(eq(1L), any(Customer.class))).thenReturn(c1);

        graphql("mutation { updateCustomer(customer: {id: 1, names: \"Juan\"}) { id names } }")
                .statusCode(200)
                .body("data.updateCustomer.id", is(1))
                .body("data.updateCustomer.names", is("Juan"));

        verify(customerService).update(eq(1L), any(Customer.class));
    }

    @Test
    @DisplayName("deleteCustomer - Retorna el cliente eliminado")
    void testDeleteCustomer() {
        when(customerService.delete(1L)).thenReturn(c1);

        graphql("mutation { deleteCustomer(customerId: 1) { id code } }")
                .statusCode(200)
                .body("data.deleteCustomer.code", is("CUST-001"));

        verify(customerService).delete(1L);
    }

    @Test
    @DisplayName("deleteCustomer - Error GraphQL si no existe")
    void testDeleteCustomer_NoExiste() {
        when(customerService.delete(99L)).thenThrow(new NotFoundException("Cliente no encontrado con ID: 99"));

        graphql("mutation { deleteCustomer(customerId: 99) { id } }")
                .statusCode(200)
                .body("errors", hasSize(1));
    }
}
