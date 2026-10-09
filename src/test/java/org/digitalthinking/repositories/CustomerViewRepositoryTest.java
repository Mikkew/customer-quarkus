package org.digitalthinking.repositories;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.entities.Product;
import org.digitalthinking.views.CustomerView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CustomerViewRepositoryTest {

    @Inject
    CustomerRepository customerRepository;

    @Inject
    CustomerViewRepository viewRepository;

    private Customer persist(String code, Long... productIds) {
        Customer c = new Customer(null, code, "ACC-" + code, "Carlos", "Pérez", "123", "Calle 1", new ArrayList<>());
        for (Long pid : productIds) {
            Product p = new Product();
            p.setProduct(pid);
            p.setName("P" + pid);
            p.setCustomer(c);
            c.getProducts().add(p);
        }
        customerRepository.persist(c);
        customerRepository.flush();
        return c;
    }

    @Test
    @TestTransaction
    @DisplayName("findById - Proyecta campos y productos")
    void testFindById() {
        Customer c = persist("V-1", 101L, 102L);

        CustomerView view = viewRepository.findById(c.getId()).orElseThrow();

        assertEquals("V-1", view.getCode());
        assertEquals("Carlos", view.getNames());
        assertEquals(2, view.getProducts().size());
        assertTrue(view.getProducts().stream().anyMatch(p -> "P101".equals(p.getName())));
    }

    @Test
    @TestTransaction
    @DisplayName("findById - Vacío si no existe")
    void testFindById_NoExiste() {
        assertTrue(viewRepository.findById(-1L).isEmpty());
    }

    @Test
    @TestTransaction
    @DisplayName("findAll - Incluye clientes persistidos, ordenados por id")
    void testFindAll() {
        int antes = viewRepository.findAll().size();
        persist("V-2");
        persist("V-3");

        List<CustomerView> todos = viewRepository.findAll();

        assertEquals(antes + 2, todos.size());
        for (int i = 1; i < todos.size(); i++) {
            assertTrue(todos.get(i - 1).getId() < todos.get(i).getId());
        }
    }
}
