package org.digitalthinking.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.entities.Product;
import org.digitalthinking.exceptions.NotFoundException;
import org.digitalthinking.repositories.CustomerRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@ApplicationScoped
public class CustomerService {

    private static final String PRODUCT_HOST = "localhost";
    private static final int PRODUCT_PORT = 8081;
    private static final String PRODUCT_PATH = "/product";
    private static final Duration PRODUCT_TIMEOUT = Duration.ofSeconds(5);

    private final CustomerRepository customerRepository;
    private final Vertx vertx;
    private final ObjectMapper objectMapper;
    private WebClient webClient;

    @Inject
    public CustomerService(CustomerRepository customerRepository, Vertx vertx, ObjectMapper objectMapper) {
        this.customerRepository = customerRepository;
        this.vertx = vertx;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    private void initialize() {
        this.webClient = WebClient.create(vertx,
                new WebClientOptions().setDefaultHost(PRODUCT_HOST)
                        .setDefaultPort(PRODUCT_PORT).setSsl(false));
    }

    public List<Customer> list() {
        return customerRepository.listAll();
    }

    @Transactional
    public Customer getById(Long id) {
        Customer c = customerRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado con ID: " + id));
        if (c.getProducts() != null) {
            c.getProducts().size();
        }
        return c;
    }

    @Transactional
    public Customer add(Customer customer) {
        customer.setId(null);
        if (customer.getProducts() != null) {
            customer.getProducts().forEach(product -> {
                product.setId(null);
                product.setCustomer(customer);
            });
        }
        customerRepository.persist(customer);
        return customer;
    }

    @Transactional
    public Customer update(Long id, Customer detalles) {
        Customer customer = customerRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado con ID: " + id));

        customer.setCode(detalles.getCode());
        customer.setAccountNumber(detalles.getAccountNumber());
        customer.setNames(detalles.getNames());
        customer.setSurname(detalles.getSurname());
        customer.setPhone(detalles.getPhone());
        customer.setAddress(detalles.getAddress());
        if (detalles.getProducts() != null) {
            if (customer.getProducts() == null) {
                customer.setProducts(new ArrayList<>());
            }
            customer.getProducts().clear();
            detalles.getProducts().forEach(product -> {
                product.setId(null);
                product.setCustomer(customer);
                customer.getProducts().add(product);
            });
        }
        customerRepository.persist(customer);

        return customer;
    }

    @Transactional
    public Customer delete(Long id) {
        final Customer customer = getById(id);
        boolean eliminado = customerRepository.deleteById(id);
        if (!eliminado) {
            throw new NotFoundException("Cliente no encontrado con ID: " + id);
        }
        return customer;
    }

    public Uni<Customer> getCustomerReactive(Long id) {
        Customer customer = customerRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado con ID: " + id));

        Uni<Customer> item = Uni.createFrom().item(customer);
        return item;
    }

    public Uni<List<Product>> getAllProducts() {
        return webClient.get(PRODUCT_PATH)
                .timeout(PRODUCT_TIMEOUT.toMillis())
                .send()
                .onFailure()
                .invoke(ex -> log.error("Error recuperando productos", ex))
                .onItem()
                .transform(this::toProducts);
    }

    private List<Product> toProducts(HttpResponse<Buffer> response) {
        if (response.statusCode() != 200) {
            log.error("Servicio de productos respondio con estado {}", response.statusCode());
            throw new WebApplicationException("Servicio de productos no disponible", Response.Status.BAD_GATEWAY);
        }
        try {
            return objectMapper.readValue(response.bodyAsString(), new TypeReference<List<Product>>() {
            });
        } catch (JsonProcessingException ex) {
            log.error("Respuesta invalida del servicio de productos", ex);
            throw new WebApplicationException("Respuesta invalida del servicio de productos", ex,
                    Response.Status.BAD_GATEWAY);
        }
    }
}
