package org.digitalthinking.services;

import org.digitalthinking.entities.Customer;
import org.digitalthinking.entities.Product;
import org.digitalthinking.exceptions.NotFoundException;
import org.digitalthinking.repositories.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer base;

    @BeforeEach
    void setUp() {
        Product p = new Product(1L, null, 101L, "Prod", null, null);
        base = new Customer(1L, "CUST-001", "ACC-1001", "Juan", "Pérez", "555-0101", "Av. 1",
                new ArrayList<>(List.of(p)));
    }

    @Test
    @DisplayName("list - Retorna todos los clientes")
    void testList() {
        Customer c2 = new Customer(2L, "C2", "A2", "Ana", "López", "456", "Calle", new ArrayList<>());
        when(customerRepository.listAll()).thenReturn(List.of(base, c2));

        List<Customer> res = customerService.list();

        assertEquals(2, res.size());
        assertEquals("Juan", res.get(0).getNames());
        assertEquals("Ana", res.get(1).getNames());
    }

    @Test
    @DisplayName("list - Retorna lista vacía")
    void testList_Vacio() {
        when(customerRepository.listAll()).thenReturn(Collections.emptyList());

        assertTrue(customerService.list().isEmpty());
    }

    @Test
    @DisplayName("getById - Retorna cliente existente")
    void testGetById() {
        when(customerRepository.findByIdOptional(1L)).thenReturn(Optional.of(base));

        assertSame(base, customerService.getById(1L));
    }

    @Test
    @DisplayName("getById - Acepta productos null")
    void testGetById_ProductosNull() {
        base.setProducts(null);
        when(customerRepository.findByIdOptional(1L)).thenReturn(Optional.of(base));

        assertDoesNotThrow(() -> customerService.getById(1L));
    }

    @Test
    @DisplayName("getById - 404 si no existe")
    void testGetById_NoExiste() {
        when(customerRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> customerService.getById(99L));

        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    @DisplayName("add - Persiste y enlaza productos con el cliente")
    void testAdd() {
        Customer res = customerService.add(base);

        assertSame(base, res);
        assertSame(base, base.getProducts().get(0).getCustomer());
        verify(customerRepository).persist(base);
    }

    @Test
    @DisplayName("add - Ignora ids enviados por el cliente")
    void testAdd_IgnoraIds() {
        Customer res = customerService.add(base);

        assertNull(res.getId());
        assertNull(res.getProducts().get(0).getId());
        verify(customerRepository).persist(base);
    }

    @Test
    @DisplayName("add - Acepta productos null")
    void testAdd_ProductosNull() {
        base.setProducts(null);

        assertDoesNotThrow(() -> customerService.add(base));
        verify(customerRepository).persist(base);
    }

    @Test
    @DisplayName("update - Actualiza todos los campos")
    void testUpdate() {
        when(customerRepository.findByIdOptional(1L)).thenReturn(Optional.of(base));
        Product nuevo = new Product(null, null, 202L, null, null, null);
        Customer detalles = new Customer(null, "NEW", "ACC-NEW", "Pedro", "Ruiz", "999", "Nueva",
                new ArrayList<>(List.of(nuevo)));

        Customer res = customerService.update(1L, detalles);

        assertEquals("NEW", res.getCode());
        assertEquals("ACC-NEW", res.getAccountNumber());
        assertEquals("Pedro", res.getNames());
        assertEquals("Ruiz", res.getSurname());
        assertEquals("999", res.getPhone());
        assertEquals("Nueva", res.getAddress());
        assertEquals(1, res.getProducts().size());
        assertEquals(1L, res.getId());
        verify(customerRepository).persist(base);
    }

    @Test
    @DisplayName("update - 404 si no existe")
    void testUpdate_NoExiste() {
        when(customerRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> customerService.update(99L, base));

        assertTrue(ex.getMessage().contains("99"));
        verify(customerRepository, never()).persist(any(Customer.class));
    }

    @Test
    @DisplayName("delete - Elimina cliente existente")
    void testDelete() {
        when(customerRepository.deleteById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> customerService.delete(1L));
    }

    @Test
    @DisplayName("delete - 404 si no existe")
    void testDelete_NoExiste() {
        when(customerRepository.deleteById(99L)).thenReturn(false);

        NotFoundException ex = assertThrows(NotFoundException.class, () -> customerService.delete(99L));

        assertTrue(ex.getMessage().contains("99"));
    }
}
