package org.digitalthinking.repositories;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.digitalthinking.entities.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CustomerRepositoryTest {

    @Inject
    private CustomerRepository customerRepository;

    @Test
    @TestTransaction
    @DisplayName("findByEmail - Éxito: retorna cliente cuando el email existe")
    void testFindByEmail_Existe() {
        Customer c = new Customer(null, "Carlos", "Pérez", "carlos@test.com", "123", new ArrayList<>());
        customerRepository.persist(c);

        Optional<Customer> resultado = customerRepository.findByEmail("carlos@test.com");

        assertTrue(resultado.isPresent());
        assertEquals("Carlos", resultado.get().getNombre());
    }

    @Test
    @TestTransaction
    @DisplayName("findByEmail - Éxito: retorna vacío cuando el email no existe")
    void testFindByEmail_NoExiste() {
        Optional<Customer> resultado = customerRepository.findByEmail("noexiste@test.com");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @TestTransaction
    @DisplayName("findByNombre - Éxito: retorna coincidencias parciales")
    void testFindByNombre_ConCoincidencias() {
        Customer c1 = new Customer(null, "Carlos", "Pérez", "carlos@test.com", "123", new ArrayList<>());
        Customer c2 = new Customer(null, "Carla", "Gómez", "carla@test.com", "456", new ArrayList<>());
        Customer c3 = new Customer(null, "Ana", "López", "ana@test.com", "789", new ArrayList<>());
        customerRepository.persist(c1);
        customerRepository.persist(c2);
        customerRepository.persist(c3);

        List<Customer> resultado = customerRepository.findByNombre("Carl");

        assertEquals(2, resultado.size());
    }

    @Test
    @TestTransaction
    @DisplayName("findByNombre - Éxito: case-insensitive")
    void testFindByNombre_CaseInsensitive() {
        Customer c = new Customer(null, "Carlos", "Pérez", "carlos@test.com", "123", new ArrayList<>());
        customerRepository.persist(c);

        List<Customer> resultado = customerRepository.findByNombre("CARLOS");

        assertEquals(1, resultado.size());
    }

    @Test
    @TestTransaction
    @DisplayName("findByNombre - Éxito: retorna vacío cuando no hay coincidencias")
    void testFindByNombre_SinCoincidencias() {
        Customer c = new Customer(null, "Carlos", "Pérez", "carlos@test.com", "123", new ArrayList<>());
        customerRepository.persist(c);

        List<Customer> resultado = customerRepository.findByNombre("Zzz");

        assertTrue(resultado.isEmpty());
    }
}