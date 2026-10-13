package org.digitalthinking.controllers;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.services.CustomerService;
import org.digitalthinking.services.CustomerSpringService;
import org.digitalthinking.services.CustomerViewService;
import org.digitalthinking.usecases.CustomerProductsUseCase;

import java.util.List;

@Path("/customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerViewService customerViewService;
    private final CustomerSpringService customerSpringService;
    private final CustomerProductsUseCase customerProductsUseCase;

    @Inject
    public CustomerController(CustomerService customerService, CustomerViewService customerViewService,
                              CustomerSpringService customerSpringService,
                              CustomerProductsUseCase customerProductsUseCase) {
        this.customerService = customerService;
        this.customerViewService = customerViewService;
        this.customerSpringService = customerSpringService;
        this.customerProductsUseCase = customerProductsUseCase;
    }

    @GET
    @Blocking
    public List<Customer> list() {
        return customerService.list();
    }

    @GET
    @Path("/view")
    @Blocking
    public Response listViews() {
        return Response.ok(customerViewService.list()).build();
    }

    @GET
    @Path("/view/{id}")
    @Blocking
    public Response getViewById(@PathParam("id") Long id) {
        return Response.ok(customerViewService.getById(id)).build();
    }

    @GET
    @Path("/code/{code}")
    @Blocking
    public Response getByCode(@PathParam("code") String code) {
        return Response.ok(customerSpringService.getByCode(code)).build();
    }

    @GET
    @Path("/search")
    @Blocking
    public Response search(@QueryParam("name") String name, @QueryParam("surname") String surname) {
        if (surname != null) {
            return Response.ok(customerSpringService.findBySurname(surname)).build();
        }
        return Response.ok(customerSpringService.searchByName(name == null ? "" : name)).build();
    }

    @GET
    @Path("/{id}")
    @Blocking
    public Response getById(@PathParam("id") Long id) {
        return Response.ok(customerService.getById(id)).build();
    }

    @POST
    @Blocking
    public Response add(Customer customer) {
        Customer nuevo = customerService.add(customer);
        return Response.status(Status.CREATED).entity(nuevo).build();
    }

    @PUT
    @Path("/{id}")
    @Blocking
    public Response update(@PathParam("id") Long id, Customer detalles) {
        return Response.accepted(customerService.update(id, detalles)).build();
    }

    @DELETE
    @Path("/{id}")
    @Blocking
    public Response delete(@PathParam("id") Long id) {
        return Response.accepted(customerService.delete(id)).build();
    }

    @GET
    @Path("{id}/product")
    @Blocking
    public Uni<Customer> getByIdProduct(@PathParam("id") Long id) {
        return customerProductsUseCase.getByIdWithProducts(id);
    }

}
