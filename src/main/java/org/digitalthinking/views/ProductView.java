package org.digitalthinking.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import org.digitalthinking.entities.Product;

/**
 * Proyección de solo lectura de {@link Product} (Blaze Persistence Entity View).
 * <p>
 * Se usa como subvista anidada en {@link CustomerView#getProducts()}; expone solo
 * el id, el identificador del producto externo y su nombre, sin la referencia de
 * vuelta al cliente.
 */
@EntityView(Product.class)
public interface ProductView {

    @IdMapping
    Long getId();

    Long getProduct();

    String getName();
}
