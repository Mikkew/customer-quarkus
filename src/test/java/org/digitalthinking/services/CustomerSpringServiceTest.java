package org.digitalthinking.services;

import org.digitalthinking.entities.Customer;
import org.digitalthinking.exceptions.NotFoundException;
import org.digitalthinking.repositories.CustomerSpringRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerSpringServiceTest {

    @Mock
    private CustomerSpringRepository repository;

    @InjectMocks
    private CustomerSpringService service;

    private final Customer c = new Customer(1L, "C1", "A1", "Juan", "Pérez", "1", "Av", new ArrayList<>());

    @Test
    @DisplayName("getByCode - Retorna cliente existente")
    void testGetByCode() {
        when(repository.findByCode("C1")).thenReturn(Optional.of(c));

        assertSame(c, service.getByCode("C1"));
    }

    @Test
    @DisplayName("getByCode - Lanza NotFoundException si no existe")
    void testGetByCode_NoExiste() {
        when(repository.findByCode("X")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getByCode("X"));
    }

    @Test
    @DisplayName("findBySurname - Delega en el repositorio")
    void testFindBySurname() {
        when(repository.findBySurnameIgnoreCase("pérez")).thenReturn(List.of(c));

        assertEquals(List.of(c), service.findBySurname("pérez"));
    }

    @Test
    @DisplayName("searchByName - Delega en el repositorio")
    void testSearchByName() {
        when(repository.searchByName("jua")).thenReturn(List.of(c));

        assertEquals(List.of(c), service.searchByName("jua"));
    }

    @Test
    @DisplayName("existsByAccountNumber - Delega en el repositorio")
    void testExistsByAccountNumber() {
        when(repository.existsByAccountNumber("A1")).thenReturn(true);

        assertTrue(service.existsByAccountNumber("A1"));
        assertFalse(service.existsByAccountNumber("B"));
    }
}
