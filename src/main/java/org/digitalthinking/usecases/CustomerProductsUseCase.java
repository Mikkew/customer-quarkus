package org.digitalthinking.usecases;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.services.CustomerService;

@ApplicationScoped
public class CustomerProductsUseCase {

    private final CustomerService customerService;

    @Inject
    public CustomerProductsUseCase(CustomerService customerService) {
        this.customerService = customerService;
    }

    public Uni<Customer> getByIdWithProducts(Long id) {
        return Uni.combine()
                .all()
                .unis(customerService.getCustomerReactive(id), customerService.getAllProducts())
                .with((customer, products) -> {
                    customer.getProducts().forEach(product -> {
                        products.forEach(p -> {
                            if (product.getId().equals(p.getId())) {
                                product.setName(p.getName());
                                product.setDescription(p.getDescription());
                            }
                        });
                    });
                    return customer;
                });
    }
}
