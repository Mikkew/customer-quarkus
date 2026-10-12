package org.digitalthinking.graphql;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.services.CustomerService;
import org.digitalthinking.usecases.CustomerProductsUseCase;
import org.eclipse.microprofile.graphql.*;

import java.util.List;

@GraphQLApi
public class CustomerResources {

    private CustomerService customerService;
    private CustomerProductsUseCase customerProductsUseCase;

    public CustomerResources(CustomerService customerService,
                             CustomerProductsUseCase customerProductsUseCase) {
        this.customerService = customerService;
        this.customerProductsUseCase = customerProductsUseCase;
    }

    @Query("allCustomer")
    @Description("Get all customer from a database")
    public List<Customer> getAllCustomer() {
        return customerService.list();
    }

    @Query
    @Description("Get a customer form database")
    public Customer getCustomer(@Name("customerId") Long id) {
        return customerService.getById(id);
    }


    @Mutation
    public Customer addCustomer(Customer customer) {
        return customerService.add(customer);
    }



    @Mutation
    public Customer deleteCustomer(@Name("customerId") Long id) {
        return customerService.delete(id);
    }

}
