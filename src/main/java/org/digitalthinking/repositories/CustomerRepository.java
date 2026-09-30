package org.digitalthinking.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.digitalthinking.entities.Customer;

@ApplicationScoped
public class CustomerRepository implements PanacheRepository<Customer> {

}
