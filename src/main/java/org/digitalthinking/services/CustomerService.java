package org.digitalthinking.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.repositories.CustomerRepository;

import java.util.List;

@ApplicationScoped
public class CustomerService {

    @Inject
    private CustomerRepository customerRepository;

    public List<Customer> listarTodos() {
        return customerRepository.listAll();
    }

    @Transactional
    public Customer obtenerPorId(Long id) {
        Customer c = customerRepository.findByIdOptional(id)
            .orElseThrow(() -> new WebApplicationException("Cliente no encontrado con ID: " + id, Response.Status.NOT_FOUND));
    c.getProductIds().size(); // inicializa el lazy
    return c;
    }

    @Transactional
    public Customer crearCliente(Customer customer) {
        if (customer.getId() != null) {
            throw new WebApplicationException("El ID no debe enviarse al crear un nuevo cliente", Response.Status.BAD_REQUEST);
        }

        // Regla de negocio: Verificar email único
        if (customer.getEmail() != null && customerRepository.findByEmail(customer.getEmail()).isPresent()) {
            throw new WebApplicationException("Ya existe un cliente registrado con ese email", Response.Status.CONFLICT);
        }

        customerRepository.persist(customer);
        return customer;
    }

    @Transactional
    public Customer actualizarCliente(Long id, Customer detalles) {
        Customer clienteExistente = obtenerPorId(id);

        clienteExistente.setNombre(detalles.getNombre());
        clienteExistente.setApellido(detalles.getApellido());
        clienteExistente.setEmail(detalles.getEmail());
        clienteExistente.setTelefono(detalles.getTelefono());

         clienteExistente.getProductIds().size();

        return clienteExistente;
    }

    @Transactional
    public void eliminarCliente(Long id) {
        boolean eliminado = customerRepository.deleteById(id);
        if (!eliminado) {
            throw new WebApplicationException("Cliente no encontrado con ID: " + id, Response.Status.NOT_FOUND);
        }
    }

    public List<Long> obtenerProductIdsPorCliente(Long customerId) {
        Customer customer = customerRepository.findByIdOptional(customerId)
                .orElseThrow(() -> new WebApplicationException("Cliente no encontrado", Response.Status.NOT_FOUND));
        return customer.getProductIds();
    }

    @Transactional
    public Customer agregarProductoACliente(Long customerId, Long productId) {
        Customer customer = customerRepository.findByIdOptional(customerId)
                .orElseThrow(() -> new WebApplicationException("Cliente no encontrado", Response.Status.NOT_FOUND));

        if (!customer.getProductIds().contains(productId)) {
            customer.getProductIds().add(productId);
        }
        return customer;
    }

    @Transactional
    public Customer removerProductoDeCliente(Long customerId, Long productId) {
        Customer customer = customerRepository.findByIdOptional(customerId)
                .orElseThrow(() -> new WebApplicationException("Cliente no encontrado", Response.Status.NOT_FOUND));

        customer.getProductIds().remove(productId);
        return customer;
    }
}
