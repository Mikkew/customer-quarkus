package org.digitalthinking.repositories;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.digitalthinking.entities.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CustomerRepositoryTest {

    @Inject
    private CustomerRepository customerRepository;

    private Customer nuevoCliente(String code, String names, String surname) {
        return new Customer(null, code, "ACC-" + code, names, surname, "123", "Calle 1", null);
    }

    @Test
    @TestTransaction
    @DisplayName("findByCode - Éxito: retorna cliente cuando el código existe")
    void testFindByCode_Existe() {
        Customer c = nuevoCliente("C001", "Carlos", "Pérez");
        customerRepository.persist(c);

        Optional<Customer> resultado = customerRepository.findByCode("C001");

        assertTrue(resultado.isPresent());
        assertEquals("Carlos", resultado.get().getNames());
    }

    @Test
    @TestTransaction
    @DisplayName("findByCode - Éxito: retorna vacío cuando el código no existe")
    void testFindByCode_NoExiste() {
        Optional<Customer> resultado = customerRepository.findByCode("NOEXISTE");

        assertTrue(resultado.isEmpty());
    }

    @Test
    @TestTransaction
    @DisplayName("findByNames - Éxito: retorna coincidencias parciales")
    void testFindByNombre_ConCoincidencias() {
        Customer c1 = nuevoCliente("C001", "Zqcarlos", "Pérez");
        Customer c2 = nuevoCliente("C002", "Zqcarla", "Gómez");
        Customer c3 = nuevoCliente("C003", "Ana", "López");
        customerRepository.persist(c1);
        customerRepository.persist(c2);
        customerRepository.persist(c3);

        List<Customer> resultado = customerRepository.findByNames("Zqcarl");

        assertEquals(2, resultado.size());
    }

    @Test
    @TestTransaction
    @DisplayName("findByNames - Éxito: case-insensitive")
    void testFindByNombre_CaseInsensitive() {
        Customer c = nuevoCliente("C001", "Zqcarlos", "Pérez");
        customerRepository.persist(c);

        List<Customer> resultado = customerRepository.findByNames("ZQCARLOS");

        assertEquals(1, resultado.size());
    }

    @Test
    @TestTransaction
    @DisplayName("findByNames - Éxito: retorna vacío cuando no hay coincidencias")
    void testFindByNombre_SinCoincidencias() {
        Customer c = nuevoCliente("C001", "Carlos", "Pérez");
        customerRepository.persist(c);

        List<Customer> resultado = customerRepository.findByNames("Zzz");

        assertTrue(resultado.isEmpty());
    }
}