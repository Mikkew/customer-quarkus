package org.digitalthinking.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.entities.Product;
import org.digitalthinking.exceptions.NotFoundException;
import org.digitalthinking.repositories.CustomerRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ApplicationScoped
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final Vertx vertx;
    private WebClient webClient;

    @Inject
    public CustomerService(CustomerRepository customerRepository, Vertx vertx) {
        this.customerRepository = customerRepository;
        this.vertx = vertx;
    }

    @PostConstruct
    private void initialize() {
        this.webClient = WebClient.create(vertx,
                new WebClientOptions().setDefaultHost("localhost")
                        .setDefaultPort(8081).setSsl(false).setTrustAll(true));
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
    public void delete(Long id) {
        boolean eliminado = customerRepository.deleteById(id);
        if (!eliminado) {
            throw new NotFoundException("Cliente no encontrado con ID: " + id);
        }
    }

    public Uni<Customer> getCustomerReactive(Long id) {
        Customer customer = customerRepository.findByIdOptional(id)
            .orElseThrow(() -> new NotFoundException("Cliente no encontrado con ID: " + id));

        Uni<Customer> item = Uni.createFrom().item(customer);
        return item;
    }

    public Uni<List<Product>> getAllProducts() {
        return webClient.get(8081, "localhost", "/product")
                .send()
                .onFailure()
                .invoke(res -> log.error("Error recuperando productos ", res))
                .onItem()
                .transform(response -> {
                    List<Product> lista = new ArrayList<>();
                    JsonArray objects = response.bodyAsJsonArray();

                    objects.forEach(p -> {
                        log.info("See Objects: {}", p);
                        ObjectMapper objectMapper = new ObjectMapper();

                        Product product = null;

                        try {
                            product = objectMapper.readValue(p.toString(), Product.class);
                        } catch (JsonProcessingException ex) {
                            ex.printStackTrace();
                        }

                        lista.add(product);
                    });
                    return lista;
                });
    }
}
