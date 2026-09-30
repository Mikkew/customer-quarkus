package org.digitalthinking.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.exceptions.NotFoundException;
import org.digitalthinking.repositories.CustomerRepository;

import java.util.ArrayList;
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
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado con ID: " + id));
        if (c.getProducts() != null) {
            c.getProducts().size();
        }
        return c;
    }

    @Transactional
    public Customer add(Customer customer) {
        customer.setId(null);
        if (customer.getProducts() != null) {
            customer.getProducts().forEach(product -> {
                product.setId(null);
                product.setCustomer(customer);
            });
        }
        customerRepository.persist(customer);
        return customer;
    }

    @Transactional
    public Customer update(Long id, Customer detalles) {
        Customer customer = customerRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado con ID: " + id));

        customer.setCode(detalles.getCode());
        customer.setAccountNumber(detalles.getAccountNumber());
        customer.setNames(detalles.getNames());
        customer.setSurname(detalles.getSurname());
        customer.setPhone(detalles.getPhone());
        customer.setAddress(detalles.getAddress());
        if (detalles.getProducts() != null) {
            if (customer.getProducts() == null) {
                customer.setProducts(new ArrayList<>());
            }
            customer.getProducts().clear();
            detalles.getProducts().forEach(product -> {
                product.setId(null);
                product.setCustomer(customer);
                customer.getProducts().add(product);
            });
        }
        customerRepository.persist(customer);

        return customer;
    }

    @Transactional
    public void delete(Long id) {
        boolean eliminado = customerRepository.deleteById(id);
        if (!eliminado) {
            throw new NotFoundException("Cliente no encontrado con ID: " + id);
        }
    }
}
