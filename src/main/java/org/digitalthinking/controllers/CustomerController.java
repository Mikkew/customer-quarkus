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

    @Inject
    private CustomerService customerService;

    @GET
    public List<Customer> listarTodos() {
        return customerService.listarTodos();
    }

    @GET
    @Path("/{id}")
    public Customer obtenerPorId(@PathParam("id") Long id) {
        return customerService.obtenerPorId(id);
    }

    @POST
    public Response crear(Customer customer) {
        Customer nuevo = customerService.crearCliente(customer);
        return Response.status(Response.Status.CREATED).entity(nuevo).build();
    }

    @PUT
    @Path("/{id}")
    public Customer actualizar(@PathParam("id") Long id, Customer detalles) {
        return customerService.actualizarCliente(id, detalles);
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") Long id) {
        customerService.eliminarCliente(id);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/products")
    public List<Long> obtenerProductosDeCliente(@PathParam("id") Long customerId) {
        return customerService.obtenerProductIdsPorCliente(customerId);
    }

    @POST
    @Path("/{id}/products/{productId}")
    public Response agregarProducto(@PathParam("id") Long customerId, @PathParam("productId") Long productId) {
        Customer clienteActualizado = customerService.agregarProductoACliente(customerId, productId);
        return Response.ok(clienteActualizado).build();
    }

    @DELETE
    @Path("/{id}/products/{productId}")
    public Response removerProducto(@PathParam("id") Long customerId, @PathParam("productId") Long productId) {
        Customer clienteActualizado = customerService.removerProductoDeCliente(customerId, productId);
        return Response.ok(clienteActualizado).build();
    }
}
