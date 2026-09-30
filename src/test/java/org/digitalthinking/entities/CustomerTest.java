package org.digitalthinking.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Product producto(Long productId) {
        Product p = new Product();
        p.setProduct(productId);
        return p;
    }

    private Customer cliente(Long id, String code, List<Product> products) {
        return new Customer(id, code, "ACC-" + code, "Carlos", "Pérez", "123", "Calle 1", products);
    }

    // ==========================================
    // 1. Constructor sin argumentos
    // ==========================================

    @Test
    @DisplayName("Constructor vacío - Crea instancia con todos los campos nulos")
    void testConstructorVacio() {
        Customer c = new Customer();

        assertNotNull(c);
        assertNull(c.getId());
        assertNull(c.getCode());
        assertNull(c.getAccountNumber());
        assertNull(c.getNames());
        assertNull(c.getSurname());
        assertNull(c.getPhone());
        assertNull(c.getAddress());
        assertNull(c.getProducts());
    }

    // ==========================================
    // 2. Constructor con todos los argumentos
    // ==========================================

    @Test
    @DisplayName("Constructor completo - Asigna todos los campos")
    void testConstructorCompleto() {
        List<Product> productos = new ArrayList<>(List.of(producto(10L), producto(20L)));

        Customer c = new Customer(1L, "C001", "ACC-001", "Carlos", "Pérez", "123", "Calle 1", productos);

        assertEquals(1L, c.getId());
        assertEquals("C001", c.getCode());
        assertEquals("ACC-001", c.getAccountNumber());
        assertEquals("Carlos", c.getNames());
        assertEquals("Pérez", c.getSurname());
        assertEquals("123", c.getPhone());
        assertEquals("Calle 1", c.getAddress());
        assertEquals(2, c.getProducts().size());
        assertEquals(10L, c.getProducts().get(0).getProduct());
        assertEquals(20L, c.getProducts().get(1).getProduct());
    }

    @Test
    @DisplayName("Constructor completo - Acepta products null")
    void testConstructorCompleto_ProductsNull() {
        Customer c = cliente(1L, "C001", null);

        assertNotNull(c);
        assertNull(c.getProducts());
    }

    // ==========================================
    // 3. Getters y Setters
    // ==========================================

    @Test
    @DisplayName("Setters - Modifican correctamente todos los campos")
    void testSetters() {
        Customer c = new Customer();

        c.setId(99L);
        c.setCode("C099");
        c.setAccountNumber("ACC-099");
        c.setNames("Ana");
        c.setSurname("López");
        c.setPhone("456");
        c.setAddress("Avenida 2");
        c.setProducts(new ArrayList<>(List.of(producto(1L), producto(2L), producto(3L))));

        assertEquals(99L, c.getId());
        assertEquals("C099", c.getCode());
        assertEquals("ACC-099", c.getAccountNumber());
        assertEquals("Ana", c.getNames());
        assertEquals("López", c.getSurname());
        assertEquals("456", c.getPhone());
        assertEquals("Avenida 2", c.getAddress());
        assertEquals(3, c.getProducts().size());
    }

    // ==========================================
    // 4. Relación con Product
    // ==========================================

    @Test
    @DisplayName("products - Es mutable (permite add/remove del servicio)")
    void testProducts_Mutable() {
        Customer c = cliente(1L, "C001", new ArrayList<>());

        c.getProducts().add(producto(100L));
        c.getProducts().add(producto(200L));

        assertEquals(2, c.getProducts().size());

        c.getProducts().removeIf(p -> Long.valueOf(100L).equals(p.getProduct()));

        assertEquals(1, c.getProducts().size());
        assertEquals(200L, c.getProducts().get(0).getProduct());
    }

    // ==========================================
    // 5. equals / hashCode (Lombok @Data)
    // ==========================================

    @Test
    @DisplayName("equals/hashCode - Dos clientes con mismos datos son iguales")
    void testEqualsHashCode_Iguales() {
        Customer c1 = cliente(1L, "C001", new ArrayList<>(List.of(producto(10L))));
        Customer c2 = cliente(1L, "C001", new ArrayList<>(List.of(producto(10L))));

        assertEquals(c1, c2);
        assertEquals(c1.hashCode(), c2.hashCode());
    }

    @Test
    @DisplayName("equals/hashCode - Dos clientes con distinto id no son iguales")
    void testEqualsHashCode_Distintos() {
        Customer c1 = cliente(1L, "C001", new ArrayList<>());
        Customer c2 = cliente(2L, "C001", new ArrayList<>());

        assertNotEquals(c1, c2);
    }

    @Test
    @DisplayName("equals - Comparación consigo mismo y con null")
    void testEquals_ReflexivoYNull() {
        Customer c = cliente(1L, "C001", new ArrayList<>());

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
        Customer c = cliente(1L, "C001", new ArrayList<>(List.of(producto(10L))));

        String str = c.toString();

        assertNotNull(str);
        assertTrue(str.contains("Carlos"));
        assertTrue(str.contains("Pérez"));
        assertTrue(str.contains("C001"));
        assertTrue(str.contains("Calle 1"));
    }
}
