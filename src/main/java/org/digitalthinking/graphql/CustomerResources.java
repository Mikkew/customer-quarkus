package org.digitalthinking.graphql;

import org.digitalthinking.entities.Customer;
import org.digitalthinking.services.CustomerService;
import org.eclipse.microprofile.graphql.*;

import java.util.List;

@GraphQLApi
public class CustomerResources {

    private final CustomerService customerService;

    public CustomerResources(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Query("allCustomer")
    @Description("Get all customer from a database")
    public List<Customer> getAllCustomer() {
        return customerService.list();
    }

    @Query
    @Description("Get a customer from database")
    public Customer getCustomer(@Name("customerId") Long id) {
        return customerService.getById(id);
    }


    @Mutation
    public Customer addCustomer(Customer customer) {
        return customerService.add(customer);
    }

    @Mutation
    public Customer updateCustomer(Customer customer) {
        return customerService.update(customer.getId(), customer);
    }

    @Mutation
    public Customer deleteCustomer(@Name("customerId") Long id) {
        return customerService.delete(id);
    }


}
