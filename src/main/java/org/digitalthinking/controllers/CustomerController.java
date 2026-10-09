package org.digitalthinking.controllers;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.services.CustomerService;
import org.digitalthinking.services.CustomerSpringService;
import org.digitalthinking.services.CustomerViewService;
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
    private final CustomerViewService customerViewService;
    private final CustomerSpringService customerSpringService;

    @Inject
    public CustomerController(CustomerService customerService, CustomerViewService customerViewService,
                              CustomerSpringService customerSpringService) {
        this.customerService = customerService;
        this.customerViewService = customerViewService;
        this.customerSpringService = customerSpringService;
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
        return customerViewService.list();
    }

    @GET
    @Path("/view/{id}")
    @Blocking
    public CustomerView getViewById(@PathParam("id") Long id) {
        return customerViewService.getById(id);
    }

    @GET
    @Path("/code/{code}")
    @Blocking
    public Customer getByCode(@PathParam("code") String code) {
        return customerSpringService.getByCode(code);
    }

    @GET
    @Path("/search")
    @Blocking
    public List<Customer> search(@QueryParam("name") String name, @QueryParam("surname") String surname) {
        if (surname != null) {
            return customerSpringService.findBySurname(surname);
        }
        return customerSpringService.searchByName(name == null ? "" : name);
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
