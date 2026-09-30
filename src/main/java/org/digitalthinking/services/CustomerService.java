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

    public List<Customer> list() {
        return customerRepository.listAll();
    }

    @Transactional
    public Customer getById(Long id) {
        Customer c = customerRepository.findByIdOptional(id)
            .orElseThrow(() -> new WebApplicationException("Cliente no encontrado con ID: " + id, Response.Status.NOT_FOUND));
        if (c.getProducts() != null) {
            c.getProducts().size();
        }
        return c;
    }

    @Transactional
    public Customer add(Customer customer) {
        if (customer.getProducts() != null) {
            customer.getProducts().forEach(product -> product.setCustomer(customer));
        }
        customerRepository.persist(customer);
        return customer;
    }

    @Transactional
    public Customer update(Long id, Customer detalles) {
        Customer customer = getById(id);

        customer.setCode(detalles.getCode());
        customer.setAccountNumber(detalles.getAccountNumber());
        customer.setNames(detalles.getNames());
        customer.setSurname(detalles.getSurname());
        customer.setPhone(detalles.getPhone());
        customer.setAddress(detalles.getAddress());
        customer.setProducts(detalles.getProducts());
        customerRepository.persist(customer);

        return customer;
    }

    @Transactional
    public void delete(Long id) {
        boolean eliminado = customerRepository.deleteById(id);
        if (!eliminado) {
            throw new WebApplicationException("Cliente no encontrado con ID: " + id, Response.Status.NOT_FOUND);
        }
    }
}
