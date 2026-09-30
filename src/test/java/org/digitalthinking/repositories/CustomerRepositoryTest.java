package org.digitalthinking.repositories;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.entities.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CustomerRepositoryTest {

    @Inject
    CustomerRepository customerRepository;

    private Customer build(String code) {
        return new Customer(null, code, "ACC-" + code, "Carlos", "Pérez", "123", "Calle 1", new ArrayList<>());
    }

    @Test
    @TestTransaction
    @DisplayName("persist - Asigna id y se puede recuperar")
    void testPersist() {
        Customer c = build("T-1");
        customerRepository.persist(c);

        assertNotNull(c.getId());
        Customer encontrado = customerRepository.findById(c.getId());
        assertEquals("Carlos", encontrado.getNames());
    }

    @Test
    @TestTransaction
    @DisplayName("persist - Cascada guarda productos")
    void testPersist_ConProductos() {
        Customer c = build("T-2");
        Product p = new Product();
        p.setProduct(101L);
        p.setCustomer(c);
        c.getProducts().add(p);

        customerRepository.persist(c);
        customerRepository.flush();

        assertNotNull(p.getId());
    }

    @Test
    @TestTransaction
    @DisplayName("listAll - Incluye clientes persistidos")
    void testListAll() {
        int antes = customerRepository.listAll().size();
        customerRepository.persist(build("T-3"));
        customerRepository.persist(build("T-4"));

        List<Customer> todos = customerRepository.listAll();

        assertEquals(antes + 2, todos.size());
    }

    @Test
    @TestTransaction
    @DisplayName("deleteById - Elimina cliente existente y retorna false si no existe")
    void testDeleteById() {
        Customer c = build("T-5");
        customerRepository.persist(c);

        assertTrue(customerRepository.deleteById(c.getId()));
        assertFalse(customerRepository.deleteById(-1L));
    }
}
