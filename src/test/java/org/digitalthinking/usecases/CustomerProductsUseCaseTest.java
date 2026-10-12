package org.digitalthinking.usecases;

import io.smallrye.mutiny.Uni;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.entities.Product;
import org.digitalthinking.exceptions.NotFoundException;
import org.digitalthinking.services.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerProductsUseCaseTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerProductsUseCase useCase;

    private Customer customer;

    @BeforeEach
    void setUp() {
        Product p1 = new Product(1L, null, 101L, null, null, null);
        Product p2 = new Product(2L, null, 102L, null, null, null);
        customer = new Customer(1L, "CUST-001", "ACC-1001", "Juan", "Pérez", "555-0101", "Av. 1",
                new ArrayList<>(List.of(p1, p2)));
    }

    @Test
    @DisplayName("getByIdWithProducts - Completa nombre y descripcion por id")
    void testGetByIdWithProducts() {
        Product d1 = new Product();
        d1.setId(1L);
        d1.setName("Cuenta");
        d1.setDescription("Cuenta de ahorro");
        Product d2 = new Product();
        d2.setId(2L);
        d2.setName("Tarjeta");
        d2.setDescription("Tarjeta de credito");
        when(customerService.getCustomerReactive(1L)).thenReturn(Uni.createFrom().item(customer));
        when(customerService.getAllProducts()).thenReturn(Uni.createFrom().item(List.of(d1, d2)));

        Customer res = useCase.getByIdWithProducts(1L).await().indefinitely();

        assertEquals("Cuenta", res.getProducts().get(0).getName());
        assertEquals("Cuenta de ahorro", res.getProducts().get(0).getDescription());
        assertEquals("Tarjeta", res.getProducts().get(1).getName());
        assertEquals("Tarjeta de credito", res.getProducts().get(1).getDescription());
    }

    @Test
    @DisplayName("getByIdWithProducts - Deja intactos los productos sin coincidencia")
    void testGetByIdWithProducts_SinCoincidencia() {
        Product otro = new Product();
        otro.setId(999L);
        otro.setName("Otro");
        when(customerService.getCustomerReactive(1L)).thenReturn(Uni.createFrom().item(customer));
        when(customerService.getAllProducts()).thenReturn(Uni.createFrom().item(List.of(otro)));

        Customer res = useCase.getByIdWithProducts(1L).await().indefinitely();

        assertNull(res.getProducts().get(0).getName());
        assertNull(res.getProducts().get(1).getName());
    }

    @Test
    @DisplayName("getByIdWithProducts - Catalogo vacio")
    void testGetByIdWithProducts_CatalogoVacio() {
        when(customerService.getCustomerReactive(1L)).thenReturn(Uni.createFrom().item(customer));
        when(customerService.getAllProducts()).thenReturn(Uni.createFrom().item(List.of()));

        Customer res = useCase.getByIdWithProducts(1L).await().indefinitely();

        assertSame(customer, res);
        assertNull(res.getProducts().get(0).getName());
    }

    @Test
    @DisplayName("getByIdWithProducts - Propaga el fallo del servicio de productos")
    void testGetByIdWithProducts_FalloProductos() {
        when(customerService.getCustomerReactive(1L)).thenReturn(Uni.createFrom().item(customer));
        when(customerService.getAllProducts()).thenReturn(Uni.createFrom().failure(new IllegalStateException("caido")));

        assertThrows(IllegalStateException.class, () -> useCase.getByIdWithProducts(1L).await().indefinitely());
    }

    @Test
    @DisplayName("getByIdWithProducts - Propaga el fallo del cliente")
    void testGetByIdWithProducts_FalloCliente() {
        when(customerService.getCustomerReactive(99L))
                .thenReturn(Uni.createFrom().failure(new NotFoundException("no existe")));
        when(customerService.getAllProducts()).thenReturn(Uni.createFrom().item(List.of()));

        assertThrows(NotFoundException.class, () -> useCase.getByIdWithProducts(99L).await().indefinitely());
    }
}
