package org.digitalthinking.repositories;

import org.digitalthinking.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerSpringRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCode(String code);

    List<Customer> findBySurnameIgnoreCase(String surname);

    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT c FROM Customer c WHERE LOWER(c.names) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY c.id")
    List<Customer> searchByName(@Param("name") String name);
}
