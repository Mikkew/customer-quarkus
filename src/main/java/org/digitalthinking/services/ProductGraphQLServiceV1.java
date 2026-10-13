package org.digitalthinking.services;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.digitalthinking.entities.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.digitalthinking.constants.Constants.*;

/**
 * Consume el servicio GraphQL de productos (http://localhost:8081/graphql) a través de
 * {@link GraphQLClientService} y mapea las respuestas a {@link Product}.
 *
 * <h3>Diferencias con {@link ProductGraphQLServiceV2}</h3>
 * <ul>
 *   <li><b>Cliente:</b> delega en {@link GraphQLClientService}, un cliente propio construido
 *       con {@code VertxDynamicGraphQLClientBuilder} y la URL de {@code graphql.client.url}.
 *       V2 usa el cliente declarativo de Quarkus ({@code @GraphQLClient("product-api")}).</li>
 *   <li><b>Documentos GraphQL:</b> las queries/mutations viven como constantes en
 *       {@code Constants} (ALL_PRODUCTS, CREATE_PRODUCT, ...). V2 las arma inline con
 *       {@code PRODUCT_FIELDS}.</li>
 *   <li><b>Variables:</b> usa los nombres de argumento del schema ({@code productId}, {@code product}).
 *       V2 usa variables propias ({@code $id}, {@code $p}) declaradas en cada documento.</li>
 *   <li><b>Errores:</b> {@link GraphQLClientService} loguea y transforma fallos de red y errores
 *       GraphQL en {@code WebApplicationException} 502 (BAD_GATEWAY). V2 no los trata y los
 *       propaga tal cual.</li>
 *   <li><b>Ciclo de vida:</b> {@link GraphQLClientService} cierra el cliente en {@code @PreDestroy}.
 *       En V2 lo administra Quarkus.</li>
 * </ul>
 */
@ApplicationScoped
public class ProductGraphQLServiceV1 {

    private final GraphQLClientService graphQLClient;

    @Inject
    public ProductGraphQLServiceV1(GraphQLClientService graphQLClient) {
        this.graphQLClient = graphQLClient;
    }

    public Uni<List<Product>> list() {
        return graphQLClient.queryList(ALL_PRODUCTS, null, "allProducts", Product.class);
    }

    public Uni<Product> getById(Long id) {
        return graphQLClient.queryObject(PRODUCT_BY_ID, Map.of("productId", id), "productById", Product.class);
    }

    public Uni<Product> create(Product product) {
        Map<String, Object> input = toInput(product);
        input.remove("id");
        return graphQLClient.queryObject(CREATE_PRODUCT, Map.of("product", input), "createProduct", Product.class);
    }

    public Uni<Product> update(Long id, Product product) {
        Map<String, Object> input = toInput(product);
        input.put("id", id);
        return graphQLClient.queryObject(UPDATE_PRODUCT, Map.of("product", input), "updateProduct", Product.class);
    }

    public Uni<Product> delete(Long id) {
        return graphQLClient.queryObject(DELETE_PRODUCT, Map.of("productId", id), "deletProduct", Product.class);
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
