package org.digitalthinking.services;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.entities.Product;
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

    @Mock
    private PanacheQuery<Customer> query;

    private Customer clienteBase;

    @BeforeEach
    void setUp() {
        clienteBase = crearCliente(1L, "C001", "Carlos", "Pérez", "123", new ArrayList<>(List.of(producto(10L))));
    }

    private Customer crearCliente(Long id, String code, String names, String surname, String phone, List<Product> products) {
        return new Customer(id, code, "ACC-" + code, names, surname, phone, "Calle 1", products);
    }

    private Product producto(Long productId) {
        Product p = new Product();
        p.setProduct(productId);
        return p;
    }

    // ==========================================
    // 1. listarTodos()
    // ==========================================

    @Test
    @DisplayName("listarTodos - Éxito: Retorna todos los clientes")
    void testListarTodos_Exito() {
        Customer c2 = crearCliente(2L, "C002", "Ana", "López", "456", new ArrayList<>());
        when(customerRepository.listAll()).thenReturn(List.of(clienteBase, c2));

        List<Customer> resultado = customerService.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals("Carlos", resultado.get(0).getNames());
        assertEquals("Ana", resultado.get(1).getNames());
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
        assertEquals("Carlos", resultado.getNames());
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
        Customer input = crearCliente(null, "C001", "Carlos", "Pérez", "123", null);
        when(customerRepository.find("code", "C001")).thenReturn(query);
        when(query.firstResultOptional()).thenReturn(Optional.empty());

        Customer resultado = customerService.crearCliente(input);

        assertNotNull(resultado);
        assertEquals("Carlos", resultado.getNames());
        assertNotNull(resultado.getProducts());
        verify(customerRepository, times(1)).find("code", "C001");
        verify(customerRepository, times(1)).persist(input);
    }

    @Test
    @DisplayName("crearCliente - Error: ID no debe ser enviado (400)")
    void testCrearCliente_IdNoNulo() {
        Customer input = crearCliente(10L, "C001", "Carlos", "Pérez", "123", null);

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> customerService.crearCliente(input));

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), ex.getResponse().getStatus());
        verify(customerRepository, never()).persist(any(Customer.class));
        verify(customerRepository, never()).find(anyString(), any(Object[].class));
    }

    @Test
    @DisplayName("crearCliente - Error: Código duplicado (409)")
    void testCrearCliente_CodigoDuplicado() {
        Customer input = crearCliente(null, "C001", "Carlos", "Pérez", "123", null);
        when(customerRepository.find("code", "C001")).thenReturn(query);
        when(query.firstResultOptional()).thenReturn(Optional.of(clienteBase));

        WebApplicationException ex = assertThrows(WebApplicationException.class,
                () -> customerService.crearCliente(input));

        assertEquals(Response.Status.CONFLICT.getStatusCode(), ex.getResponse().getStatus());
        verify(customerRepository, never()).persist(any(Customer.class));
    }

    @Test
    @DisplayName("crearCliente - Éxito: Código nulo no valida duplicado")
    void testCrearCliente_CodigoNulo() {
        Customer input = crearCliente(null, null, "Carlos", "Pérez", "123", null);

        Customer resultado = customerService.crearCliente(input);

        assertNotNull(resultado);
        verify(customerRepository, never()).find(anyString(), any(Object[].class));
        verify(customerRepository, times(1)).persist(input);
    }

    // ==========================================
    // 4. actualizarCliente()
    // ==========================================

    @Test
    @DisplayName("actualizarCliente - Éxito: Actualiza datos del cliente")
    void testActualizarCliente_Exito() {
        Customer detalles = crearCliente(null, "C001", "Carlos", "Gómez", "999", null);
        when(customerRepository.findByIdOptional(1L)).thenReturn(Optional.of(clienteBase));

        Customer resultado = customerService.actualizarCliente(1L, detalles);

        assertNotNull(resultado);
        assertEquals("Gómez", resultado.getSurname());
        assertEquals("999", resultado.getPhone());
        verify(customerRepository, times(1)).findByIdOptional(1L);
    }

    @Test
    @DisplayName("actualizarCliente - Error: Cliente no existe (404)")
    void testActualizarCliente_NoEncontrado() {
        Customer detalles = crearCliente(null, "C001", "Carlos", "Gómez", "999", null);
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

        assertEquals(List.of(10L), resultado);
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

        assertEquals(2, resultado.getProducts().size());
        assertTrue(resultado.getProducts().stream().anyMatch(p -> Long.valueOf(200L).equals(p.getProduct())));
        assertTrue(resultado.getProducts().stream().allMatch(p -> p.getCustomer() == null || p.getCustomer() == resultado));
    }

    @Test
    @DisplayName("agregarProductoACliente - Éxito: No duplica producto existente")
    void testAgregarProductoACliente_YaExiste() {
        when(customerRepository.findByIdOptional(1L)).thenReturn(Optional.of(clienteBase));

        Customer resultado = customerService.agregarProductoACliente(1L, 10L);

        assertEquals(1, resultado.getProducts().size());
        assertEquals(10L, resultado.getProducts().get(0).getProduct());
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

        assertTrue(resultado.getProducts().isEmpty());
    }

    @Test
    @DisplayName("removerProductoDeCliente - Éxito: No falla si el producto no estaba")
    void testRemoverProductoDeCliente_NoEstaba() {
        when(customerRepository.findByIdOptional(1L)).thenReturn(Optional.of(clienteBase));

        Customer resultado = customerService.removerProductoDeCliente(1L, 999L);

        assertEquals(1, resultado.getProducts().size());
        assertEquals(10L, resultado.getProducts().get(0).getProduct());
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