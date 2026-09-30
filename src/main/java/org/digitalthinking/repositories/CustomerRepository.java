package org.digitalthinking.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.digitalthinking.entities.Customer;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CustomerRepository implements PanacheRepository<Customer> {

    public Optional<Customer> findByCode(String code) {
        return find("code", code).firstResultOptional();
    }

    public List<Customer> findByNames(String patron) {
        return list("LOWER(names) LIKE LOWER(?1)", "%" + patron + "%");
    }
}
