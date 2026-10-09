package org.digitalthinking.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import org.digitalthinking.entities.Customer;

import java.util.Set;

/**
 * Proyección de solo lectura de {@link Customer} (Blaze Persistence Entity View).
 * <p>
 * Blaze genera la implementación y arma una consulta que selecciona únicamente los
 * atributos declarados aquí, en lugar de cargar la entidad completa. Se obtiene a
 * través de {@code CustomerViewRepository} y se expone en los endpoints
 * {@code GET /customer/view} y {@code GET /customer/view/{id}}.
 */
@EntityView(Customer.class)
public interface CustomerView {

    @IdMapping
    Long getId();

    String getCode();

    String getAccountNumber();

    String getNames();

    String getSurname();

    String getPhone();

    String getAddress();

    /** Productos del cliente, proyectados como {@link ProductView}. */
    Set<ProductView> getProducts();
}
