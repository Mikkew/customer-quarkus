package org.digitalthinking.controllers;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.digitalthinking.entities.Product;
import org.digitalthinking.services.ProductGraphQLServiceV1;

/**
 * Expone por REST los productos que se consumen vía GraphQL del servicio de productos.
 */
@Path("/v1/product")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductGraphQLControllerV1 {

    private final ProductGraphQLServiceV1 productService;

    @Inject
    public ProductGraphQLControllerV1(ProductGraphQLServiceV1 productService) {
        this.productService = productService;
    }

    @GET
    public Uni<Response> list() {
        return productService.list().map(products -> Response.ok(products).build());
    }

    @GET
    @Path("/{id}")
    public Uni<Response> getById(@PathParam("id") Long id) {
        return productService.getById(id).map(product -> product == null
                ? Response.status(Status.NOT_FOUND).build()
                : Response.ok(product).build());
    }

    @POST
    public Uni<Response> create(Product product) {
        return productService.create(product)
                .map(created -> Response.status(Status.CREATED).entity(created).build());
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(@PathParam("id") Long id, Product product) {
        return productService.update(id, product)
                .map(updated -> Response.accepted(updated).build());
    }

    @DELETE
    @Path("/{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        return productService.delete(id)
                .map(deleted -> Response.accepted(deleted).build());
    }
}
