package org.digitalthinking.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.digitalthinking.exceptions.NotFoundException;
import org.digitalthinking.repositories.CustomerViewRepository;
import org.digitalthinking.views.CustomerView;

import java.util.List;

@ApplicationScoped
public class CustomerViewService {

    private final CustomerViewRepository customerViewRepository;

    @Inject
    public CustomerViewService(CustomerViewRepository customerViewRepository) {
        this.customerViewRepository = customerViewRepository;
    }

    public List<CustomerView> list() {
        return customerViewRepository.findAll();
    }

    public CustomerView getById(Long id) {
        return customerViewRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado con ID: " + id));
    }
}
