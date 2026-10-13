package org.digitalthinking.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpRequest;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.ws.rs.WebApplicationException;
import org.digitalthinking.entities.Product;
import org.digitalthinking.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Pruebas de {@link CustomerService#getAllProducts()} con el WebClient simulado. */
class CustomerServiceProductsTest {

    private CustomerService customerService;
    private HttpResponse<Buffer> response;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws Exception {
        customerService = new CustomerService(mock(CustomerRepository.class), null, new ObjectMapper());

        response = mock(HttpResponse.class);
        HttpRequest<Buffer> request = mock(HttpRequest.class);
        when(request.timeout(anyLong())).thenReturn(request);
        when(request.send()).thenReturn(Uni.createFrom().item(response));
        WebClient webClient = mock(WebClient.class);
        when(webClient.get(anyString())).thenReturn(request);

        Field field = CustomerService.class.getDeclaredField("webClient");
        field.setAccessible(true);
        field.set(customerService, webClient);
    }

    @Test
    @DisplayName("getAllProducts - Deserializa la lista de productos")
    void testGetAllProducts() {
        when(response.statusCode()).thenReturn(200);
        when(response.bodyAsString()).thenReturn(
                "[{\"id\":1,\"name\":\"Cuenta\",\"description\":\"Ahorro\"},{\"id\":2,\"name\":\"Tarjeta\"}]");

        List<Product> res = customerService.getAllProducts().await().indefinitely();

        assertEquals(2, res.size());
        assertEquals(1L, res.get(0).getId());
        assertEquals("Cuenta", res.get(0).getName());
        assertEquals("Tarjeta", res.get(1).getName());
    }

    @Test
    @DisplayName("getAllProducts - Lista vacia")
    void testGetAllProducts_Vacio() {
        when(response.statusCode()).thenReturn(200);
        when(response.bodyAsString()).thenReturn("[]");

        assertTrue(customerService.getAllProducts().await().indefinitely().isEmpty());
    }

    @Test
    @DisplayName("getAllProducts - 502 si el servicio responde con error")
    void testGetAllProducts_EstadoNo200() {
        when(response.statusCode()).thenReturn(500);

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> customerService.getAllProducts().await().indefinitely());

        assertEquals(502, ex.getResponse().getStatus());
    }

    @Test
    @DisplayName("getAllProducts - 502 si el cuerpo no es JSON valido")
    void testGetAllProducts_JsonInvalido() {
        when(response.statusCode()).thenReturn(200);
        when(response.bodyAsString()).thenReturn("no es json");

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> customerService.getAllProducts().await().indefinitely());

        assertEquals(502, ex.getResponse().getStatus());
    }
}
