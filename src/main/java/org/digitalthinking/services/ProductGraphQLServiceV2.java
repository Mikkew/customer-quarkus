package org.digitalthinking.services;

import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.digitalthinking.entities.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.digitalthinking.constants.Constants.*;

/**
 * Consume el servicio GraphQL de productos usando directamente el {@link DynamicGraphQLClient}
 * de SmallRye (cliente "product-api") y mapea las respuestas a {@link Product}.
 *
 * <h3>Diferencias con {@link ProductGraphQLServiceV1}</h3>
 * <ul>
 *   <li><b>Cliente:</b> inyecta el cliente declarativo de Quarkus ({@code @GraphQLClient("product-api")},
 *       configurado en {@code application.properties}). V1 pasa por {@link GraphQLClientService},
 *       que construye el cliente a mano con {@code graphql.client.url}.</li>
 *   <li><b>Documentos GraphQL:</b> las queries/mutations se arman inline con {@code PRODUCT_FIELDS}
 *       y variables {@code $id}/{@code $p}. V1 usa constantes de {@code Constants}.</li>
 *   <li><b>Errores:</b> no hay manejo propio; los fallos de red y los errores GraphQL
 *       ({@code response.hasError()}) no se transforman en 502 como en V1, y {@code getObject}
 *       puede devolver null si la respuesta trae errores.</li>
 *   <li><b>Ciclo de vida:</b> el cierre del cliente lo gestiona Quarkus; no hay {@code @PreDestroy}.</li>
 * </ul>
 */
@ApplicationScoped
public class ProductGraphQLServiceV2 {

    private final DynamicGraphQLClient client;

    @Inject
    public ProductGraphQLServiceV2(@GraphQLClient("product-api") DynamicGraphQLClient client) {
        this.client = client;
    }

    public Uni<List<Product>> list() {
        String query = "{ allProducts { " + PRODUCT_FIELDS + " } }";
        return client.executeAsync(query)
                .map(r -> r.getList(Product.class, "allProducts"));
    }

    public Uni<Product> getById(Long id) {
        String query = "query($id: BigInteger) { productById(productId: $id) { " + PRODUCT_FIELDS + " } }";
        return client.executeAsync(query, Map.of("id", id))
                .map(r -> r.getObject(Product.class, "productById"));
    }

    public Uni<Product> create(Product product) {
        Map<String, Object> input = toInput(product);
        input.remove("id");
        String mutation = "mutation($p: ProductInput) { createProduct(product: $p) { " + PRODUCT_FIELDS + " } }";
        return client.executeAsync(mutation, Map.of("p", input))
                .map(r -> r.getObject(Product.class, "createProduct"));
    }

    public Uni<Product> update(Long id, Product product) {
        Map<String, Object> input = toInput(product);
        input.put("id", id);
        String mutation = "mutation($p: ProductInput) { updateProduct(product: $p) { " + PRODUCT_FIELDS + " } }";
        return client.executeAsync(mutation, Map.of("p", input))
                .map(r -> r.getObject(Product.class, "updateProduct"));
    }

    public Uni<Product> delete(Long id) {
        // "deletProduct" (sin 'e') es el nombre real en el schema remoto.
        String mutation = "mutation($id: BigInteger) { deletProduct(productId: $id) { " + PRODUCT_FIELDS + " } }";
        return client.executeAsync(mutation, Map.of("id", id))
                .map(r -> r.getObject(Product.class, "deletProduct"));
    }

    /** Arma el {@code ProductInput} remoto (id, code, name, description) evitando serializar la relación con Customer. */
    private Map<String, Object> toInput(Product product) {
        Map<String, Object> input = new HashMap<>();
        input.put("id", product.getId());
        input.put("code", product.getCode());
        input.put("name", product.getName());
        input.put("description", product.getDescription());
        return input;
    }
}
