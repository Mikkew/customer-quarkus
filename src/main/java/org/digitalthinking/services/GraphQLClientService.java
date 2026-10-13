package org.digitalthinking.services;

import io.smallrye.graphql.client.Response;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.graphql.client.vertx.dynamic.VertxDynamicGraphQLClientBuilder;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;

/**
 * Cliente genérico para consumir un endpoint GraphQL remoto.
 * <p>
 * La URL se configura con {@code graphql.client.url}. Los métodos {@code query*}
 * reciben el documento GraphQL (query o mutation), sus variables y el nombre del
 * campo raíz a deserializar, y devuelven {@link Uni} para no bloquear el event loop.
 * <pre>{@code
 * Uni<Customer> c = client.queryObject(
 *         "query($id: BigInteger) { customer(customerId: $id) { id names surname } }",
 *         Map.of("id", 1), "customer", Customer.class);
 * }</pre>
 */
@Slf4j
@ApplicationScoped
public class GraphQLClientService {

    private final DynamicGraphQLClient client;

    @Inject
    public GraphQLClientService(@ConfigProperty(name = "graphql.client.url") String url) {
        this(new VertxDynamicGraphQLClientBuilder().url(url).build());
    }

    GraphQLClientService(DynamicGraphQLClient client) {
        this.client = client;
    }

    /** Ejecuta un documento GraphQL y devuelve la respuesta cruda (data + errors). */
    public Uni<Response> execute(String document, Map<String, Object> variables) {
        return client.executeAsync(document, variables == null ? Map.of() : variables)
                .onFailure().invoke(ex -> log.error("Error consumiendo servicio GraphQL", ex))
                .onFailure().transform(ex -> new WebApplicationException(
                        "Servicio GraphQL no disponible", ex, Status.BAD_GATEWAY))
                .onItem().invoke(this::failOnErrors);
    }

    /** Ejecuta el documento y deserializa el campo raíz {@code field} como un objeto. */
    public <T> Uni<T> queryObject(String document, Map<String, Object> variables, String field, Class<T> type) {
        return execute(document, variables).map(response -> response.getObject(type, field));
    }

    /** Ejecuta el documento y deserializa el campo raíz {@code field} como una lista. */
    public <T> Uni<List<T>> queryList(String document, Map<String, Object> variables, String field, Class<T> type) {
        return execute(document, variables).map(response -> response.getList(type, field));
    }

    private void failOnErrors(Response response) {
        if (response.hasError()) {
            log.error("El servicio GraphQL respondio con errores: {}", response.getErrors());
            throw new WebApplicationException("El servicio GraphQL respondio con errores", Status.BAD_GATEWAY);
        }
    }

    @PreDestroy
    void close() {
        try {
            client.close();
        } catch (Exception ex) {
            log.warn("No se pudo cerrar el cliente GraphQL", ex);
        }
    }
}
