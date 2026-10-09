package org.digitalthinking.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.exceptions.NotFoundException;
import org.digitalthinking.repositories.CustomerSpringRepository;

import java.util.List;

@ApplicationScoped
public class CustomerSpringService {

    private final CustomerSpringRepository customerSpringRepository;

    @Inject
    public CustomerSpringService(CustomerSpringRepository customerSpringRepository) {
        this.customerSpringRepository = customerSpringRepository;
    }

    public Customer getByCode(String code) {
        return customerSpringRepository.findByCode(code)
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado con código: " + code));
    }

    public List<Customer> findBySurname(String surname) {
        return customerSpringRepository.findBySurnameIgnoreCase(surname);
    }

    public List<Customer> searchByName(String name) {
        return customerSpringRepository.searchByName(name);
    }

    public boolean existsByAccountNumber(String accountNumber) {
        return customerSpringRepository.existsByAccountNumber(accountNumber);
    }
}
