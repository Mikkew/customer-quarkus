package org.digitalthinking.services;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.digitalthinking.entities.Customer;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private Customer clienteBase;

    @BeforeEach
    void setUp() {
        clienteBase = new Customer(1L, "Carlos", "Pérez", "carlos@test.com", "123", new ArrayList<>(List.of(10L)));
    }

    // ==========================================
    // 1. listarTodos()
    // ==========================================

    @Test
    @DisplayName("listarTodos - Éxito: Retorna todos los clientes")
    void testListarTodos_Exito() {
        Customer c2 = new Customer(2L, "Ana", "López", "ana@test.com", "456", new ArrayList<>());
        when(customerRepository.listAll()).thenReturn(List.of(clienteBase, c2));

        List<Customer> resultado = customerService.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals("Carlos", resultado.get(0).getNombre());
        assertEquals("Ana", resultado.get(1).getNombre());
        verify(customerRepository, times(1)).listAll();
    }

    @Test
    @DisplayName("listarTodos - Éxito: Retorna lista vacía")
    void testListarTodos_Vacio() {
        when(customerRepository.listAll()).thenReturn(Collections.emptyList());

        List<Customer> resultado = customerService.listarTodos();

        assertTrue(resultado.isEmpty());
        verify(customerRepository, times(1)).listAll();
    }

    // ==========================================
    // 2. obtenerPorId()
    // ==========================================

    @Test
    @DisplayName("obtenerPorId - Éxito: Cliente encontrado")
    void testObtenerPorId_Exito() {
        when(customerRepository.findByIdOptional(1L)).thenReturn(Optional.of(clienteBase));

        Customer resultado = customerService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Carlos", resultado.getNombre());
        verify(customerRepository, times(1)).findByIdOptional(1L);
    }

    @Test
    @DisplayName("obtenerPorId - Error: Cliente no encontrado (404)")
    void testObtenerPorId_NoEncontrado() {
        when(customerRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> customerService.obtenerPorId(99L));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("99"));
        verify(customerRepository, times(1)).findByIdOptional(99L);
    }

    // ==========================================
    // 3. crearCliente()
    // ==========================================

    @Test
    @DisplayName("crearCliente - Éxito: Persiste cliente nuevo")
    void testCrearCliente_Exito() {
        Customer input = new Customer(null, "Carlos", "Pérez", "carlos@test.com", "123", null);
        when(customerRepository.findByEmail("carlos@test.com")).thenReturn(Optional.empty());

        Customer resultado = customerService.crearCliente(input);

        assertNotNull(resultado);
        assertEquals("Carlos", resultado.getNombre());
        verify(customerRepository, times(1)).findByEmail("carlos@test.com");
        verify(customerRepository, times(1)).persist(input);
    }

    @Test
    @DisplayName("crearCliente - Error: ID no debe ser enviado (400)")
    void testCrearCliente_IdNoNulo() {
        Customer input = new Customer(10L, "Carlos", "Pérez", "carlos@test.com", "123", null);

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> customerService.crearCliente(input));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
        verify(customerRepository, never()).persist(any(Customer.class));
        verify(customerRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("crearCliente - Error: Email duplicado (409)")
    void testCrearCliente_EmailDuplicado() {
        Customer input = new Customer(null, "Carlos", "Pérez", "carlos@test.com", "123", null);
        when(customerRepository.findByEmail("carlos@test.com")).thenReturn(Optional.of(clienteBase));

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> customerService.crearCliente(input));

        assertEquals(Response.Status.CONFLICT.getStatusCode(), ex.getResponse().getStatus());
        verify(customerRepository, never()).persist(any(Customer.class));
    }

    @Test
    @DisplayName("crearCliente - Éxito: Email nulo no valida duplicado")
    void testCrearCliente_EmailNulo() {
        Customer input = new Customer(null, "Carlos", "Pérez", null, "123", null);

        Customer resultado = customerService.crearCliente(input);

        assertNotNull(resultado);
        verify(customerRepository, never()).findByEmail(anyString());
        verify(customerRepository, times(1)).persist(input);
    }

    // ==========================================
    // 4. actualizarCliente()
    // ==========================================

    @Test
    @DisplayName("actualizarCliente - Éxito: Actualiza datos del cliente")
    void testActualizarCliente_Exito() {
        Customer detalles = new Customer(null, "Carlos", "Gómez", "carlos@test.com", "999", null);
        when(customerRepository.findByIdOptional(1L)).thenReturn(Optional.of(clienteBase));

        Customer resultado = customerService.actualizarCliente(1L, detalles);

        assertNotNull(resultado);
        assertEquals("Gómez", resultado.getApellido());
        assertEquals("999", resultado.getTelefono());
        verify(customerRepository, times(1)).findByIdOptional(1L);
    }

    @Test
    @DisplayName("actualizarCliente - Error: Cliente no existe (404)")
    void testActualizarCliente_NoEncontrado() {
        Customer detalles = new Customer(null, "Carlos", "Gómez", "carlos@test.com", "999", null);
        when(customerRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> customerService.actualizarCliente(99L, detalles));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
    }

    // ==========================================
    // 5. eliminarCliente()
    // ==========================================

    @Test
    @DisplayName("eliminarCliente - Éxito: Elimina cliente existente")
    void testEliminarCliente_Exito() {
        when(customerRepository.deleteById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> customerService.eliminarCliente(1L));

        verify(customerRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("eliminarCliente - Error: Cliente no existe (404)")
    void testEliminarCliente_NoEncontrado() {
        when(customerRepository.deleteById(99L)).thenReturn(false);

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> customerService.eliminarCliente(99L));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
        assertTrue(ex.getMessage().contains("99"));
    }

    // ==========================================
    // 6. obtenerProductIdsPorCliente()
    // ==========================================

    @Test
    @DisplayName("obtenerProductIdsPorCliente - Éxito: Retorna IDs de productos")
    void testObtenerProductIdsPorCliente_Exito() {
        when(customerRepository.findByIdOptional(1L)).thenReturn(Optional.of(clienteBase));

        List<Long> resultado = customerService.obtenerProductIdsPorCliente(1L);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0));
        verify(customerRepository, times(1)).findByIdOptional(1L);
    }

    @Test
    @DisplayName("obtenerProductIdsPorCliente - Error: Cliente no existe (404)")
    void testObtenerProductIdsPorCliente_NoEncontrado() {
        when(customerRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> customerService.obtenerProductIdsPorCliente(99L));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
    }

    // ==========================================
    // 7. agregarProductoACliente()
    // ==========================================

    @Test
    @DisplayName("agregarProductoACliente - Éxito: Agrega producto nuevo")
    void testAgregarProductoACliente_Exito() {
        when(customerRepository.findByIdOptional(1L)).thenReturn(Optional.of(clienteBase));

        Customer resultado = customerService.agregarProductoACliente(1L, 200L);

        assertTrue(resultado.getProductIds().contains(200L));
        assertEquals(2, resultado.getProductIds().size());
    }

    @Test
    @DisplayName("agregarProductoACliente - Éxito: No duplica producto existente")
    void testAgregarProductoACliente_YaExiste() {
        when(customerRepository.findByIdOptional(1L)).thenReturn(Optional.of(clienteBase));

        Customer resultado = customerService.agregarProductoACliente(1L, 10L);

        assertEquals(1, resultado.getProductIds().size());
        assertTrue(resultado.getProductIds().contains(10L));
    }

    @Test
    @DisplayName("agregarProductoACliente - Error: Cliente no existe (404)")
    void testAgregarProductoACliente_NoEncontrado() {
        when(customerRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> customerService.agregarProductoACliente(99L, 200L));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
    }

    // ==========================================
    // 8. removerProductoDeCliente()
    // ==========================================

    @Test
    @DisplayName("removerProductoDeCliente - Éxito: Remueve producto existente")
    void testRemoverProductoDeCliente_Exito() {
        when(customerRepository.findByIdOptional(1L)).thenReturn(Optional.of(clienteBase));

        Customer resultado = customerService.removerProductoDeCliente(1L, 10L);

        assertFalse(resultado.getProductIds().contains(10L));
        assertTrue(resultado.getProductIds().isEmpty());
    }

    @Test
    @DisplayName("removerProductoDeCliente - Éxito: No falla si el producto no estaba")
    void testRemoverProductoDeCliente_NoEstaba() {
        when(customerRepository.findByIdOptional(1L)).thenReturn(Optional.of(clienteBase));

        Customer resultado = customerService.removerProductoDeCliente(1L, 999L);

        assertEquals(1, resultado.getProductIds().size());
        assertTrue(resultado.getProductIds().contains(10L));
    }

    @Test
    @DisplayName("removerProductoDeCliente - Error: Cliente no existe (404)")
    void testRemoverProductoDeCliente_NoEncontrado() {
        when(customerRepository.findByIdOptional(99L)).thenReturn(Optional.empty());

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> customerService.removerProductoDeCliente(99L, 200L));

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), ex.getResponse().getStatus());
    }
}