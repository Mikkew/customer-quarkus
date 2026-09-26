package org.digitalthinking.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    // ==========================================
    // 1. Constructor sin argumentos
    // ==========================================

    @Test
    @DisplayName("Constructor vacío - Crea instancia con productIds no nulo y vacío")
    void testConstructorVacio() {
        Customer c = new Customer();

        assertNotNull(c);
        assertNull(c.getId());
        assertNull(c.getNombre());
        assertNull(c.getApellido());
        assertNull(c.getEmail());
        assertNull(c.getTelefono());
        assertNotNull(c.getProductIds(), "productIds debe inicializarse por defecto");
        assertTrue(c.getProductIds().isEmpty());
    }

    // ==========================================
    // 2. Constructor con todos los argumentos
    // ==========================================

    @Test
    @DisplayName("Constructor completo - Asigna todos los campos")
    void testConstructorCompleto() {
        List<Long> productos = new ArrayList<>(List.of(10L, 20L));

        Customer c = new Customer(1L, "Carlos", "Pérez", "carlos@test.com", "123", productos);

        assertEquals(1L, c.getId());
        assertEquals("Carlos", c.getNombre());
        assertEquals("Pérez", c.getApellido());
        assertEquals("carlos@test.com", c.getEmail());
        assertEquals("123", c.getTelefono());
        assertEquals(2, c.getProductIds().size());
        assertTrue(c.getProductIds().containsAll(List.of(10L, 20L)));
    }

    @Test
    @DisplayName("Constructor completo - Acepta productIds null")
    void testConstructorCompleto_ProductIdsNull() {
        Customer c = new Customer(1L, "Carlos", "Pérez", "carlos@test.com", "123", null);

        assertNotNull(c);
        assertNull(c.getProductIds());
    }

    // ==========================================
    // 3. Getters y Setters
    // ==========================================

    @Test
    @DisplayName("Setters - Modifican correctamente todos los campos")
    void testSetters() {
        Customer c = new Customer();

        c.setId(99L);
        c.setNombre("Ana");
        c.setApellido("López");
        c.setEmail("ana@test.com");
        c.setTelefono("456");
        c.setProductIds(new ArrayList<>(List.of(1L, 2L, 3L)));

        assertEquals(99L, c.getId());
        assertEquals("Ana", c.getNombre());
        assertEquals("López", c.getApellido());
        assertEquals("ana@test.com", c.getEmail());
        assertEquals("456", c.getTelefono());
        assertEquals(3, c.getProductIds().size());
    }

    // ==========================================
    // 4. Comportamiento de productIds (clave para el servicio)
    // ==========================================

    @Test
    @DisplayName("productIds - Es mutable (permite add/remove del servicio)")
    void testProductIds_Mutable() {
        Customer c = new Customer();

        c.getProductIds().add(100L);
        c.getProductIds().add(200L);

        assertEquals(2, c.getProductIds().size());
        assertTrue(c.getProductIds().contains(100L));

        c.getProductIds().remove(100L);

        assertEquals(1, c.getProductIds().size());
        assertFalse(c.getProductIds().contains(100L));
    }

    @Test
    @DisplayName("productIds - No es nulo al crear con constructor vacío")
    void testProductIds_NoEsNulo() {
        Customer c = new Customer();

        assertDoesNotThrow(() -> c.getProductIds().add(1L));
    }

    // ==========================================
    // 5. equals / hashCode (Lombok @Data)
    // ==========================================

    @Test
    @DisplayName("equals/hashCode - Dos clientes con mismos datos son iguales")
    void testEqualsHashCode_Iguales() {
        Customer c1 = new Customer(1L, "Carlos", "Pérez", "c@t.com", "123", new ArrayList<>(List.of(10L)));
        Customer c2 = new Customer(1L, "Carlos", "Pérez", "c@t.com", "123", new ArrayList<>(List.of(10L)));

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    @DisplayName("equals/hashCode - Dos clientes con distinto id no son iguales")
    void testEqualsHashCode_Distintos() {
        Customer c1 = new Customer(1L, "Carlos", "Pérez", "c@t.com", "123", new ArrayList<>());
        Customer c2 = new Customer(2L, "Carlos", "Pérez", "c@t.com", "123", new ArrayList<>());

        assertNotEquals(c1, c2);
    }

    @Test
    @DisplayName("equals - Comparación consigo mismo y con null")
    void testEquals_ReflexivoYNull() {
        Customer c = new Customer(1L, "Carlos", "Pérez", "c@t.com", "123", new ArrayList<>());

        assertEquals(c, c);
        assertNotEquals(c, null);
        assertNotEquals(c, "otro tipo");
    }

    // ==========================================
    // 6. toString (Lombok @Data)
    // ==========================================

    @Test
    @DisplayName("toString - Contiene los valores de los campos")
    void testToString() {
        Customer c = new Customer(1L, "Carlos", "Pérez", "c@t.com", "123", new ArrayList<>(List.of(10L)));

        String str = c.toString();

        assertNotNull(str);
        assertTrue(str.contains("Carlos"));
        assertTrue(str.contains("Pérez"));
        assertTrue(str.contains("c@t.com"));
        assertTrue(str.contains("1"));
    }
}