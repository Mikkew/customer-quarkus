package org.digitalthinking.views;

import com.blazebit.persistence.view.EntityView;
import com.blazebit.persistence.view.IdMapping;
import org.digitalthinking.entities.Customer;

import java.util.Set;

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

    Set<ProductView> getProducts();
}
