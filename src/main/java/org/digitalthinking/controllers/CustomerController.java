package org.digitalthinking.controllers;

import org.digitalthinking.entities.Customer;
import org.digitalthinking.services.CustomerService;
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
    public List<Customer> list() {
        return customerService.list();
    }

    @GET
    @Path("/{id}")
    public Customer getById(@PathParam("id") Long id) {
        return customerService.getById(id);
    }

    @POST
    public Response add(Customer customer) {
        Customer nuevo = customerService.add(customer);
        return Response.status(Response.Status.CREATED).entity(nuevo).build();
    }

    @PUT
    @Path("/{id}")
    public Customer update(@PathParam("id") Long id, Customer detalles) {
        return customerService.update(id, detalles);
    }

    @DELETE
    @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        customerService.delete(id);
        return Response.noContent().build();
    }

}
