package org.digitalthinking.services;

import io.smallrye.graphql.client.GraphQLError;
import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.WebApplicationException;
import org.digitalthinking.entities.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GraphQLClientServiceTest {

    private static final String QUERY = "{ allCustomer { id names } }";

    private DynamicGraphQLClient client;
    private GraphQLClientService service;

    @BeforeEach
    void setUp() {
        client = mock(DynamicGraphQLClient.class);
        service = new GraphQLClientService(client);
    }

    @Test
    void queryListDeserializaElCampoRaiz() throws Exception {
        Customer customer = new Customer();
        customer.setNames("Ana");
        Response response = mock(Response.class);
        when(response.hasError()).thenReturn(false);
        when(response.getList(Customer.class, "allCustomer")).thenReturn(List.of(customer));
        when(client.executeAsync(eq(QUERY), any(Map.class))).thenReturn(Uni.createFrom().item(response));

        List<Customer> result = service.queryList(QUERY, null, "allCustomer", Customer.class)
                .await().indefinitely();

        assertEquals(1, result.size());
        assertEquals("Ana", result.get(0).getNames());
    }

    @Test
    void lanzaBadGatewayCuandoRespondeConErrores() throws Exception {
        Response response = mock(Response.class);
        when(response.hasError()).thenReturn(true);
        when(response.getErrors()).thenReturn(List.of(mock(GraphQLError.class)));
        when(client.executeAsync(eq(QUERY), any(Map.class))).thenReturn(Uni.createFrom().item(response));

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> service.execute(QUERY, Map.of()).await().indefinitely());

        assertEquals(502, ex.getResponse().getStatus());
    }

    @Test
    void lanzaBadGatewayCuandoFallaLaConexion() throws Exception {
        when(client.executeAsync(eq(QUERY), any(Map.class)))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("conexion rechazada")));

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> service.execute(QUERY, Map.of()).await().indefinitely());

        assertEquals(502, ex.getResponse().getStatus());
    }
}
