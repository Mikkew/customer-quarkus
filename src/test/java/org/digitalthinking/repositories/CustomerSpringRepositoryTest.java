package org.digitalthinking.repositories;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.digitalthinking.entities.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CustomerSpringRepositoryTest {

    @Inject
    CustomerSpringRepository repository;

    private Customer build(String code, String names, String surname) {
        return new Customer(null, code, "ACC-" + code, names, surname, "123", "Calle 1", new ArrayList<>());
    }

    @Test
    @TestTransaction
    @DisplayName("save y findById - Persiste y recupera")
    void testSaveAndFindById() {
        Customer c = repository.save(build("S-1", "Carlos", "Pérez"));

        assertNotNull(c.getId());
        assertEquals("S-1", repository.findById(c.getId()).orElseThrow().getCode());
    }

    @Test
    @TestTransaction
    @DisplayName("findByCode - Encuentra por código o vacío")
    void testFindByCode() {
        repository.save(build("S-2", "Ana", "López"));

        assertTrue(repository.findByCode("S-2").isPresent());
        assertTrue(repository.findByCode("NOPE").isEmpty());
    }

    @Test
    @TestTransaction
    @DisplayName("findBySurnameIgnoreCase - Ignora mayúsculas")
    void testFindBySurnameIgnoreCase() {
        repository.save(build("S-3", "Luis", "Zzapellido"));

        assertEquals(1, repository.findBySurnameIgnoreCase("zZAPELLIDO").size());
    }

    @Test
    @TestTransaction
    @DisplayName("existsByAccountNumber - Verifica existencia")
    void testExistsByAccountNumber() {
        repository.save(build("S-4", "Eva", "Ruiz"));

        assertTrue(repository.existsByAccountNumber("ACC-S-4"));
        assertFalse(repository.existsByAccountNumber("ACC-NOPE"));
    }

    @Test
    @TestTransaction
    @DisplayName("searchByName - Búsqueda parcial con @Query")
    void testSearchByName() {
        repository.save(build("S-5", "Xylofonista", "A"));

        var res = repository.searchByName("YLOFON");

        assertEquals(1, res.size());
        assertEquals("S-5", res.get(0).getCode());
    }

    @Test
    @TestTransaction
    @DisplayName("deleteById - Elimina el cliente")
    void testDeleteById() {
        Customer c = repository.save(build("S-6", "Tmp", "Tmp"));

        repository.deleteById(c.getId());
        repository.flush();

        assertTrue(repository.findById(c.getId()).isEmpty());
    }
}
