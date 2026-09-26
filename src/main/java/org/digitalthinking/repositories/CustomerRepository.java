package org.digitalthinking.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.digitalthinking.entities.Customer;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CustomerRepository implements PanacheRepository<Customer> {

    public Optional<Customer> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public List<Customer> findByNombre(String patron) {
        return list("LOWER(nombre) LIKE LOWER(?1)", "%" + patron + "%");
    }
}
