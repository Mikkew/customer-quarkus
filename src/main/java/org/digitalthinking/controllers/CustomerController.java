package org.digitalthinking.controllers;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.services.CustomerService;
import org.digitalthinking.views.CustomerView;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerController {

    private final CustomerService customerService;

    @Inject
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GET
    @Blocking
    public List<Customer> list() {
        return customerService.list();
    }

    @GET
    @Path("/view")
    @Blocking
    public List<CustomerView> listViews() {
        return customerService.listViews();
    }

    @GET
    @Path("/view/{id}")
    @Blocking
    public CustomerView getViewById(@PathParam("id") Long id) {
        return customerService.getViewById(id);
    }

    @GET
    @Path("/{id}")
    @Blocking
    public Customer getById(@PathParam("id") Long id) {
        return customerService.getById(id);
    }

    @POST
    @Blocking
    public Response add(Customer customer) {
        Customer nuevo = customerService.add(customer);
        return Response.status(Response.Status.CREATED).entity(nuevo).build();
    }

    @PUT
    @Path("/{id}")
    @Blocking
    public Customer update(@PathParam("id") Long id, Customer detalles) {
        return customerService.update(id, detalles);
    }

    @DELETE
    @Path("/{id}")
    @Blocking
    public Response delete(@PathParam("id") Long id) {
        customerService.delete(id);
        return Response.noContent().build();
    }

    @GET
    @Path("{id}/product")
    @Blocking
    public Uni<Customer> getByIdProduct(@PathParam("id") Long id) {
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
