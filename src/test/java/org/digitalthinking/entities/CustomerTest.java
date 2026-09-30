package org.digitalthinking.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Customer build(Long id, List<Product> products) {
        return new Customer(id, "CUST-001", "ACC-1001", "Juan", "Pérez", "555-0101", "Av. Reforma 123", products);
    }

    @Test
    @DisplayName("Constructor vacío - Todos los campos son null")
    void testConstructorVacio() {
        Customer c = new Customer();

        assertNull(c.getId());
        assertNull(c.getCode());
        assertNull(c.getAccountNumber());
        assertNull(c.getNames());
        assertNull(c.getSurname());
        assertNull(c.getPhone());
        assertNull(c.getAddress());
        assertNull(c.getProducts());
    }

    @Test
    @DisplayName("Constructor completo - Asigna todos los campos")
    void testConstructorCompleto() {
        Product p = new Product(1L, null, 101L, "Producto", null, null);
        Customer c = build(1L, new ArrayList<>(List.of(p)));

        assertEquals(1L, c.getId());
        assertEquals("CUST-001", c.getCode());
        assertEquals("ACC-1001", c.getAccountNumber());
        assertEquals("Juan", c.getNames());
        assertEquals("Pérez", c.getSurname());
        assertEquals("555-0101", c.getPhone());
        assertEquals("Av. Reforma 123", c.getAddress());
        assertEquals(1, c.getProducts().size());
    }

    @Test
    @DisplayName("Setters - Modifican todos los campos")
    void testSetters() {
        Customer c = new Customer();

        c.setId(99L);
        c.setCode("C");
        c.setAccountNumber("A");
        c.setNames("Ana");
        c.setSurname("López");
        c.setPhone("456");
        c.setAddress("Calle");
        c.setProducts(new ArrayList<>());

        assertEquals(99L, c.getId());
        assertEquals("C", c.getCode());
        assertEquals("A", c.getAccountNumber());
        assertEquals("Ana", c.getNames());
        assertEquals("López", c.getSurname());
        assertEquals("456", c.getPhone());
        assertEquals("Calle", c.getAddress());
        assertTrue(c.getProducts().isEmpty());
    }

    @Test
    @DisplayName("equals/hashCode - Mismos datos son iguales")
    void testEqualsHashCode_Iguales() {
        Customer c1 = build(1L, new ArrayList<>());
        Customer c2 = build(1L, new ArrayList<>());

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    @DisplayName("equals - Distinto id no son iguales")
    void testEquals_Distintos() {
        assertNotEquals(build(1L, new ArrayList<>()), build(2L, new ArrayList<>()));
    }

    @Test
    @DisplayName("equals - Consigo mismo, null y otro tipo")
    void testEquals_ReflexivoYNull() {
        Customer c = build(1L, new ArrayList<>());

        assertEquals(c, c);
        assertNotEquals(null, c);
        assertNotEquals("otro tipo", c);
    }

    @Test
    @DisplayName("Relación bidireccional - hashCode y toString no recursan")
    void testRelacionBidireccional() {
        Customer c = build(1L, new ArrayList<>());
        Product p = new Product(1L, c, 101L, null, null, null);
        c.getProducts().add(p);

        assertDoesNotThrow(c::hashCode);
        assertDoesNotThrow(c::toString);
        assertDoesNotThrow(p::hashCode);
        assertDoesNotThrow(p::toString);
    }

    @Test
    @DisplayName("toString - Contiene valores de los campos")
    void testToString() {
        String str = build(1L, new ArrayList<>()).toString();

        assertTrue(str.contains("Juan"));
        assertTrue(str.contains("Pérez"));
        assertTrue(str.contains("CUST-001"));
    }
}
