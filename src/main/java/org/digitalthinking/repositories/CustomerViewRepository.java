package org.digitalthinking.repositories;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.digitalthinking.entities.Customer;
import org.digitalthinking.views.CustomerView;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class CustomerViewRepository {

    private final EntityManager em;
    private final CriteriaBuilderFactory cbf;
    private final EntityViewManager evm;

    @Inject
    public CustomerViewRepository(EntityManager em, CriteriaBuilderFactory cbf, EntityViewManager evm) {
        this.em = em;
        this.cbf = cbf;
        this.evm = evm;
    }

    public List<CustomerView> findAll() {
        return evm.applySetting(EntityViewSetting.create(CustomerView.class),
                cbf.create(em, Customer.class).orderByAsc("id")).getResultList();
    }

    public Optional<CustomerView> findById(Long id) {
        return Optional.ofNullable(evm.find(em, CustomerView.class, id));
    }
}
