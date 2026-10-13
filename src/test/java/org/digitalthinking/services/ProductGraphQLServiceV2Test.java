package org.digitalthinking.services;

import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.mutiny.Uni;
import org.digitalthinking.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductGraphQLServiceV2Test {

    private DynamicGraphQLClient client;
    private Response response;
    private ProductGraphQLServiceV2 service;
    private Product product;

    @BeforeEach
    void setUp() throws Exception {
        client = mock(DynamicGraphQLClient.class);
        response = mock(Response.class);
        service = new ProductGraphQLServiceV2(client);
        product = new Product(1L, null, null, "Laptop", "P-001", "Portatil");

        when(client.executeAsync(anyString())).thenReturn(Uni.createFrom().item(response));
        when(client.executeAsync(anyString(), org.mockito.ArgumentMatchers.<Map<String, Object>>any()))
                .thenReturn(Uni.createFrom().item(response));
    }

    @Test
    void listConsultaAllProducts() throws Exception {
        when(response.getList(Product.class, "allProducts")).thenReturn(List.of(product));

        List<Product> result = service.list().await().indefinitely();

        assertEquals(List.of(product), result);
        ArgumentCaptor<String> query = ArgumentCaptor.forClass(String.class);
        verify(client).executeAsync(query.capture());
        assertTrue(query.getValue().contains("allProducts"));
    }

    @Test
    void getByIdEnviaId() throws Exception {
        when(response.getObject(Product.class, "productById")).thenReturn(product);

        Product result = service.getById(1L).await().indefinitely();

        assertEquals(product, result);
        verify(client).executeAsync(org.mockito.ArgumentMatchers.contains("productById"), org.mockito.ArgumentMatchers.eq(Map.of("id", 1L)));
    }

    @Test
    @SuppressWarnings("unchecked")
    void createEnviaInputSinIdNiCustomer() throws Exception {
        when(response.getObject(Product.class, "createProduct")).thenReturn(product);

        Product result = service.create(product).await().indefinitely();

        assertEquals(product, result);
        ArgumentCaptor<Map<String, Object>> vars = ArgumentCaptor.forClass(Map.class);
        verify(client).executeAsync(org.mockito.ArgumentMatchers.contains("createProduct"), vars.capture());
        Map<String, Object> input = (Map<String, Object>) vars.getValue().get("p");
        assertFalse(input.containsKey("id"));
        assertFalse(input.containsKey("customer"));
        assertEquals("Laptop", input.get("name"));
        assertEquals("P-001", input.get("code"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateUsaElIdDeLaRuta() throws Exception {
        when(response.getObject(Product.class, "updateProduct")).thenReturn(product);

        service.update(7L, product).await().indefinitely();

        ArgumentCaptor<Map<String, Object>> vars = ArgumentCaptor.forClass(Map.class);
        verify(client).executeAsync(org.mockito.ArgumentMatchers.contains("updateProduct"), vars.capture());
        assertEquals(7L, ((Map<String, Object>) vars.getValue().get("p")).get("id"));
    }

    @Test
    void deleteUsaMutationDeletProduct() throws Exception {
        when(response.getObject(Product.class, "deletProduct")).thenReturn(product);

        assertEquals(product, service.delete(1L).await().indefinitely());
        verify(client).executeAsync(org.mockito.ArgumentMatchers.contains("deletProduct"), org.mockito.ArgumentMatchers.eq(Map.of("id", 1L)));
    }
}
