package org.digitalthinking.services;

import io.smallrye.mutiny.Uni;
import org.digitalthinking.entities.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductGraphQLServiceV1Test {

    private GraphQLClientService graphQLClient;
    private ProductGraphQLServiceV1 service;
    private Product product;

    @BeforeEach
    void setUp() {
        graphQLClient = mock(GraphQLClientService.class);
        service = new ProductGraphQLServiceV1(graphQLClient);
        product = new Product(1L, null, null, "Laptop", "P-001", "Portatil");
    }

    @Test
    void listConsultaAllProducts() {
        when(graphQLClient.queryList(anyString(), any(), eq("allProducts"), eq(Product.class)))
                .thenReturn(Uni.createFrom().item(List.of(product)));

        List<Product> result = service.list().await().indefinitely();

        assertEquals(List.of(product), result);
    }

    @Test
    void getByIdEnviaProductId() {
        when(graphQLClient.queryObject(anyString(), any(), eq("productById"), eq(Product.class)))
                .thenReturn(Uni.createFrom().item(product));

        Product result = service.getById(1L).await().indefinitely();

        assertEquals(product, result);
        verify(graphQLClient).queryObject(anyString(), eq(Map.of("productId", 1L)), eq("productById"),
                eq(Product.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void createEnviaInputSinIdNiCustomer() {
        when(graphQLClient.queryObject(anyString(), any(), eq("createProduct"), eq(Product.class)))
                .thenReturn(Uni.createFrom().item(product));

        service.create(product).await().indefinitely();

        ArgumentCaptor<Map<String, Object>> vars = ArgumentCaptor.forClass(Map.class);
        verify(graphQLClient).queryObject(anyString(), vars.capture(), eq("createProduct"), eq(Product.class));
        Map<String, Object> input = (Map<String, Object>) vars.getValue().get("product");
        assertFalse(input.containsKey("id"));
        assertFalse(input.containsKey("customer"));
        assertEquals("Laptop", input.get("name"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void updateUsaElIdDeLaRuta() {
        when(graphQLClient.queryObject(anyString(), any(), eq("updateProduct"), eq(Product.class)))
                .thenReturn(Uni.createFrom().item(product));

        service.update(7L, product).await().indefinitely();

        ArgumentCaptor<Map<String, Object>> vars = ArgumentCaptor.forClass(Map.class);
        verify(graphQLClient).queryObject(anyString(), vars.capture(), eq("updateProduct"), eq(Product.class));
        assertEquals(7L, ((Map<String, Object>) vars.getValue().get("product")).get("id"));
    }

    @Test
    void deleteUsaMutationDeletProduct() {
        when(graphQLClient.queryObject(anyString(), any(), eq("deletProduct"), eq(Product.class)))
                .thenReturn(Uni.createFrom().item(product));

        assertEquals(product, service.delete(1L).await().indefinitely());
    }
}
