package org.digitalthinking.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import org.digitalthinking.entities.Product;

@EntityView(Product.class)
public interface ProductView {

    @IdMapping
    Long getId();

    Long getProduct();

    String getName();
}
