package org.digitalthinking.services;

import org.digitalthinking.exceptions.NotFoundException;
import org.digitalthinking.repositories.CustomerViewRepository;
import org.digitalthinking.views.CustomerView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerViewServiceTest {

    @Mock
    private CustomerViewRepository customerViewRepository;

    @InjectMocks
    private CustomerViewService service;

    @Test
    @DisplayName("list - Delega en el repositorio de views")
    void testList() {
        CustomerView v = mock(CustomerView.class);
        when(customerViewRepository.findAll()).thenReturn(List.of(v));

        assertEquals(List.of(v), service.list());
    }

    @Test
    @DisplayName("getById - Retorna la view existente")
    void testGetById() {
        CustomerView v = mock(CustomerView.class);
        when(customerViewRepository.findById(1L)).thenReturn(Optional.of(v));

        assertSame(v, service.getById(1L));
    }

    @Test
    @DisplayName("getById - Lanza NotFoundException si no existe")
    void testGetById_NoExiste() {
        when(customerViewRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(99L));
    }
}
