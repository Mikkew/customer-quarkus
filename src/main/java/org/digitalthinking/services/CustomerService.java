package org.digitalthinking.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.entities.Product;
import org.digitalthinking.repositories.CustomerRepository;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CustomerService {

    @Inject
    private CustomerRepository customerRepository;

    public List<Customer> listarTodos() {
        return customerRepository.listAll();
    }

    public Customer obtenerPorId(Long id) {
        return customerRepository.findByIdOptional(id)
            .orElseThrow(() -> new WebApplicationException("Cliente no encontrado con ID: " + id, Response.Status.NOT_FOUND));
    }

    @Transactional
    public Customer crearCliente(Customer customer) {
        if (customer.getId() != null) {
            throw new WebApplicationException("El ID no debe enviarse al crear un nuevo cliente", Response.Status.BAD_REQUEST);
        }

        // Regla de negocio: Verificar código único
        if (customer.getCode() != null && customerRepository.find("code", customer.getCode()).firstResultOptional().isPresent()) {
            throw new WebApplicationException("Ya existe un cliente registrado con ese código", Response.Status.CONFLICT);
        }

        if (customer.getProducts() == null) {
            customer.setProducts(new ArrayList<>());
        }
        customer.getProducts().forEach(p -> p.setCustomer(customer));

        customerRepository.persist(customer);
        return customer;
    }

    @Transactional
    public Customer actualizarCliente(Long id, Customer detalles) {
        Customer clienteExistente = obtenerPorId(id);

        clienteExistente.setCode(detalles.getCode());
        clienteExistente.setAccountNumber(detalles.getAccountNumber());
        clienteExistente.setNames(detalles.getNames());
        clienteExistente.setSurname(detalles.getSurname());
        clienteExistente.setPhone(detalles.getPhone());
        clienteExistente.setAddress(detalles.getAddress());

        return clienteExistente;
    }

    @Transactional
    public void eliminarCliente(Long id) {
        boolean eliminado = customerRepository.deleteById(id);
        if (!eliminado) {
            throw new WebApplicationException("Cliente no encontrado con ID: " + id, Response.Status.NOT_FOUND);
        }
    }

//    public List<Long> obtenerProductIdsPorCliente(Long customerId) {
//        return obtenerPorId(customerId).getProducts().stream()
//                .map(Product::getProduct)
//                .toList();
//    }
//
//    @Transactional
//    public Customer agregarProductoACliente(Long customerId, Long productId) {
//        Customer customer = obtenerPorId(customerId);
//
//        boolean yaAsociado = customer.getProducts().stream()
//                .anyMatch(p -> productId.equals(p.getProduct()));
//
//        if (!yaAsociado) {
//            Product producto = new Product();
//            producto.setCustomer(customer);
//            producto.setProduct(productId);
//            customer.getProducts().add(producto);
//        }
//        return customer;
//    }
//
//    @Transactional
//    public Customer removerProductoDeCliente(Long customerId, Long productId) {
//        Customer customer = obtenerPorId(customerId);
//
//        customer.getProducts().removeIf(p -> productId.equals(p.getProduct()));
//        return customer;
//    }
}
